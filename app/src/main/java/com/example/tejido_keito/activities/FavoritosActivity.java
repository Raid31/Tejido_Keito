package com.example.tejido_keito.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.adapters.PatronAdapter;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.models.Patron;
import com.example.tejido_keito.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Muestra solo los patrones que el usuario marco con un corazon, si quita uno desaparece de la lista
 * Las consultas se hacen en un hilo secundario
 */
public class FavoritosActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private SessionManager sesion;
    private PatronAdapter adapter;
    private TextView tvVacio;
    private RecyclerView rvFavoritos;

    private final List<Patron> patronesFavoritos = new ArrayList<>();

    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    /**
     * Arma la lista y el corazon (aqui quita el patron de favoritos)
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        dbHelper = new KeitoDbHelper(this);
        sesion = new SessionManager(this);

        tvVacio = findViewById(R.id.tvVacioFavoritos);
        rvFavoritos = findViewById(R.id.rvFavoritos);
        rvFavoritos.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PatronAdapter(new ArrayList<>());
        rvFavoritos.setAdapter(adapter);

        // Al tocar el corazón aquí se quita el patrón de favoritos y desaparece de la lista
        adapter.setOnFavoritoListener((patron, esFavorito) -> ejecutor.execute(() -> {
            if (esFavorito) {
                dbHelper.agregarFavorito(sesion.getUsuarioId(), patron.getId());
            } else {
                dbHelper.quitarFavorito(sesion.getUsuarioId(), patron.getId());
            }
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (!esFavorito) {
                    patronesFavoritos.removeIf(p -> p.getId() == patron.getId());
                    mostrarLista();
                    Toast.makeText(this, "Quitado de favoritos", Toast.LENGTH_SHORT).show();
                }
            });
        }));
    }

    /**
     *  Recarga los favoritos cada vez que la pantalla vuelva a verse
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    /**
     * lee los favoritos del usuario y los patrones en un hilo secundario, deja solo los favortios y
     * los muestra al terminar
     */
    private void cargarFavoritos() {
        ejecutor.execute(() -> {
            Set<Integer> favoritos = dbHelper.obtenerFavoritosUsuario(sesion.getUsuarioId());
            List<Patron> todos = dbHelper.obtenerPatrones();

            List<Patron> soloFavoritos = new ArrayList<>();
            for (Patron p : todos) {
                if (favoritos.contains(p.getId())) {
                    soloFavoritos.add(p);
                }
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                patronesFavoritos.clear();
                patronesFavoritos.addAll(soloFavoritos);
                adapter.setFavoritos(favoritos);
                mostrarLista();
            });
        });
    }

    /**
     *     Muestra la lista, o el mensaje si no hay favoritos
      */
    private void mostrarLista() {
        adapter.actualizarLista(new ArrayList<>(patronesFavoritos));

        boolean vacio = patronesFavoritos.isEmpty();
        tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        rvFavoritos.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }

    /**
     * Cierra la base de datos en el mismo hilo secundario, después de las consultas pendientes,
     * y apaga el executor.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ejecutor.execute(() -> {
            if (dbHelper != null) {
                dbHelper.close();
            }
        });
        ejecutor.shutdown();
    }
}