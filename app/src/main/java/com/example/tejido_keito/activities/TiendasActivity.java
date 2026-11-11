package com.example.tejido_keito.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.adapters.TiendaAdapter;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.models.Tienda;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mapa (osmdroid con OpenTopoMap) con las tiendas de lanas de Iquique y una lista ordenada por cercanía.
 * Usa el GPS (FusedLocationProviderClient) para calcular la distancia,
 * y el botón "Cómo llegar" abre una app de mapas con un Intent implícito.
 *
 */
public class TiendasActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private MapView mapa;
    private TiendaAdapter adapter;
    private Marker marcadorUsuario;

    private List<Tienda> tiendas = new ArrayList<>();
    private Location ubicacionActual;

    private FusedLocationProviderClient fusedClient;
    private boolean actualizandoUbicacion = false;
    private boolean centradoInicial = false;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    /**
     * Pide el permiso de ubicación en tiempo de ejecución.
     */
    private final ActivityResultLauncher<String> permisoUbicacion =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    iniciarActualizacionesUbicacion();
                } else {
                    Toast.makeText(this,
                            "Sin permiso de ubicación no puedo calcular distancias, pero verás las tiendas.",
                            Toast.LENGTH_LONG).show();
                }
            });

    /**
     *     Recibe las posiciones del GPS
      */
    private final LocationCallback callbackUbicacion = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult resultado) {
            Location ubicacion = resultado.getLastLocation();
            if (ubicacion != null) {
                nuevaUbicacion(ubicacion);
            }
        }
    };


    /**
     *Configura osmdroid, el mapa, la lista de tiendas y el botón de mi ubicación, carga las tiendas y pide el permiso si falta.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de osmdroid (debe ir antes de cargar el layout)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("KeitoApp/1.0 (app academica de tejido; Android)");
        File base = new File(getCacheDir(), "osmdroid_keito");
        Configuration.getInstance().setOsmdroidBasePath(base);
        Configuration.getInstance().setOsmdroidTileCache(new File(base, "tiles"));

        setContentView(R.layout.activity_tiendas);

        dbHelper = new KeitoDbHelper(this);
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // ----- Mapa -----
        mapa = findViewById(R.id.map);
        mapa.setTileSource(TileSourceFactory.OpenTopo);       mapa.setMultiTouchControls(true);
        mapa.getController().setZoom(16.0);
        mapa.getController().setCenter(new GeoPoint(-20.2133, -70.1503));   // Iquique, por defecto


        // ----- Lista de tiendas -----
        RecyclerView rvTiendas = findViewById(R.id.rvTiendas);
        rvTiendas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TiendaAdapter(new TiendaAdapter.OnTiendaListener() {
            @Override
            public void onVerEnMapa(Tienda tienda) {
                mapa.getController().setZoom(18.0);
                mapa.getController().animateTo(new GeoPoint(tienda.getLatitud(), tienda.getLongitud()));
            }

            @Override
            public void onComoLlegar(Tienda tienda) {
                comoLlegar(tienda);
            }
        });
        rvTiendas.setAdapter(adapter);

        // ----- Botón "mi ubicación" -----
        FloatingActionButton fabMiUbicacion = findViewById(R.id.fabMiUbicacion);
        fabMiUbicacion.setOnClickListener(v -> irAMiUbicacion());

        cargarTiendas();

        if (!tienePermisoUbicacion()) {
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // ---------------- Tiendas (SQLite) ----------------

    /**
     * Lee las tiendas en un hilo secundario y las muestra en la lista y en el mapa al terminar.
     */
    private void cargarTiendas() {
        ejecutor.execute(() -> {
            List<Tienda> lista = dbHelper.obtenerTiendas();

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                tiendas = lista;
                mostrarTiendas();
                redibujarMapa();

                // La primera vez, el mapa se centra en las tiendas
                if (!centradoInicial) {
                    centrarEnTiendas();
                    centradoInicial = true;
                }
            });
        });
    }

    /**
     *     Ordena por cercanía (si hay ubicación) y actualiza la lista
      */
    private void mostrarTiendas() {
        if (ubicacionActual != null) {
            Collections.sort(tiendas, (a, b) -> Float.compare(distanciaMetros(a), distanciaMetros(b)));
        }
        adapter.actualizar(tiendas, ubicacionActual);
    }

    /**
     *  Calcula la distancia entre la ubicación actual y una tienda. Solo se llama cuando ya hay ubicación.
     *
     * @param tienda    tienda a la que se mide la distancia
     * @return distancia en metros
     */
    private float distanciaMetros(Tienda tienda) {
        float[] resultado = new float[1];
        Location.distanceBetween(ubicacionActual.getLatitude(), ubicacionActual.getLongitude(),
                tienda.getLatitud(), tienda.getLongitud(), resultado);
        return resultado[0];
    }

    // ---------------- Mapa ----------------

    /**
     * Borra y vuelve a poner los marcadores de las tiendas y el del usuario.
     */
    private void redibujarMapa() {
        mapa.getOverlays().clear();
        for (Tienda tienda : tiendas) {
            Marker marcador = new Marker(mapa);
            marcador.setPosition(new GeoPoint(tienda.getLatitud(), tienda.getLongitud()));
            marcador.setTitle(tienda.getNombre());
            marcador.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_marcador_tienda));
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapa.getOverlays().add(marcador);
        }

        if (marcadorUsuario != null) {
            mapa.getOverlays().add(marcadorUsuario);
        }
        mapa.invalidate();
    }

    /**
     * Centra el mapa en el promedio de las coordenadas de las tiendas.
     */
    private void centrarEnTiendas() {
        if (tiendas.isEmpty()) {
            return;
        }
        double lat = 0;
        double lng = 0;
        for (Tienda tienda : tiendas) {
            lat += tienda.getLatitud();
            lng += tienda.getLongitud();
        }
        mapa.getController().setZoom(16.5);
        mapa.getController().setCenter(new GeoPoint(lat / tiendas.size(), lng / tiendas.size()));
    }

    /**
     * Mueve el mapa a la ubicación del usuario. Si falta el permiso lo pide, y si aún no hay ubicación avisa y empieza a buscarla
     */
    private void irAMiUbicacion() {
        if (!tienePermisoUbicacion()) {
            permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        if (ubicacionActual == null) {
            Toast.makeText(this, "Buscando tu ubicación… revisa que el GPS esté activado",
                    Toast.LENGTH_SHORT).show();
            iniciarActualizacionesUbicacion();
            return;
        }
        mapa.getController().setZoom(17.0);
        mapa.getController().animateTo(
                new GeoPoint(ubicacionActual.getLatitude(), ubicacionActual.getLongitude()));
    }

    // ---------------- Ubicación (FusedLocationProviderClient) ----------------

    /**
     *  Revisa si el usuario ya dio el permiso de ubicación.
     *
     * @return true si el permiso está concedido
     */
    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    /**
     * Pide la última ubicación conocida y luego actualizaciones periódicas del GPS (cada 10 segundos o 10 metros).
     */
    private void iniciarActualizacionesUbicacion() {
        if (actualizandoUbicacion || !tienePermisoUbicacion()) {
            return;
        }
        actualizandoUbicacion = true;

        // Primero la última ubicación conocida (suele ser casi instantánea)
        fusedClient.getLastLocation().addOnSuccessListener(this, ubicacion -> {
            if (ubicacion != null && ubicacionActual == null) {
                nuevaUbicacion(ubicacion);
            }
        });

        // Luego, actualizaciones periódicas
        LocationRequest pedido = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
                .setMinUpdateDistanceMeters(10f)
                .build();
        fusedClient.requestLocationUpdates(pedido, callbackUbicacion, Looper.getMainLooper());
    }

    /**
     * Deja de pedir ubicación al GPS para no gastar batería.
     */
    private void detenerActualizacionesUbicacion() {
        if (actualizandoUbicacion) {
            fusedClient.removeLocationUpdates(callbackUbicacion);
            actualizandoUbicacion = false;
        }
    }

    /**
     * Guarda la ubicación nueva, mueve el marcador del usuario y recalcula distancias y orden.
     *
     * @param ubicacion última posición recibida del GPS
     */
    private void nuevaUbicacion(Location ubicacion) {
        ubicacionActual = ubicacion;

        if (marcadorUsuario == null) {
            marcadorUsuario = new Marker(mapa);
            marcadorUsuario.setTitle("Estás aquí");
            marcadorUsuario.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_mi_ubicacion));            marcadorUsuario.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            marcadorUsuario.setPosition(new GeoPoint(ubicacion.getLatitude(), ubicacion.getLongitude()));
            redibujarMapa();
        } else {
            marcadorUsuario.setPosition(new GeoPoint(ubicacion.getLatitude(), ubicacion.getLongitude()));
            mapa.invalidate();
        }

        mostrarTiendas();   // recalcula distancias y orden
    }

    // ---------------- Cómo llegar (Intent implícito) ----------------

    /**
     * Abre la ruta a la tienda con un Intent implícito: primero en Google Maps y, si no está, en cualquier app que entienda "geo:".
     * Si no hay ninguna app de mapas, avisa con un Toast.
     *
     * @param tienda    tienda a la que se quiere llegar
     */
    private void comoLlegar(Tienda tienda) {
        String destino = tienda.getLatitud() + "," + tienda.getLongitud();
        try {
            // Abre la ruta directamente en Google Maps
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + destino)));
        } catch (ActivityNotFoundException e) {
            try {
                // Si no está Google Maps, cualquier app de mapas que entienda "geo:"
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "geo:" + destino + "?q=" + destino + "(" + Uri.encode(tienda.getNombre()) + ")")));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, "No hay una app de mapas instalada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ---------------- Ciclo de vida ----------------

    /**
     * Reactiva el mapa y el GPS al volver a la pantalla.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapa.onResume();
        if (tienePermisoUbicacion()) {
            iniciarActualizacionesUbicacion();
        }
    }

    /**
     * al minimizar o salir, pausa el mapa y suelta el GPS para no gastar batería.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapa.onPause();
        detenerActualizacionesUbicacion();
    }

    /**
     * apaga el hilo secundario, cierra la base de datos y libera el mapa
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ejecutor.shutdown();
        if (dbHelper != null) {
            dbHelper.close();
        }
        mapa.onDetach();
    }
}