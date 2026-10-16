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

    // Pide el permiso de ubicación en tiempo de ejecución
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

    // Recibe las posiciones del GPS
    private final LocationCallback callbackUbicacion = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult resultado) {
            Location ubicacion = resultado.getLastLocation();
            if (ubicacion != null) {
                nuevaUbicacion(ubicacion);
            }
        }
    };

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

    // Ordena por cercanía (si hay ubicación) y actualiza la lista
    private void mostrarTiendas() {
        if (ubicacionActual != null) {
            Collections.sort(tiendas, (a, b) -> Float.compare(distanciaMetros(a), distanciaMetros(b)));
        }
        adapter.actualizar(tiendas, ubicacionActual);
    }

    private float distanciaMetros(Tienda tienda) {
        float[] resultado = new float[1];
        Location.distanceBetween(ubicacionActual.getLatitude(), ubicacionActual.getLongitude(),
                tienda.getLatitud(), tienda.getLongitud(), resultado);
        return resultado[0];
    }

    // ---------------- Mapa ----------------

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

    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
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

    private void detenerActualizacionesUbicacion() {
        if (actualizandoUbicacion) {
            fusedClient.removeLocationUpdates(callbackUbicacion);
            actualizandoUbicacion = false;
        }
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        mapa.onResume();
        if (tienePermisoUbicacion()) {
            iniciarActualizacionesUbicacion();
        }
    }

    // Al minimizar o salir: soltar el GPS para no gastar batería
    @Override
    protected void onPause() {
        super.onPause();
        mapa.onPause();
        detenerActualizacionesUbicacion();
    }

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