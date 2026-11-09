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

public class FavoritosActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private SessionManager sesion;
    private PatronAdapter adapter;
    private TextView tvVacio;
    private RecyclerView rvFavoritos;

    private final List<Patron> patronesFavoritos = new ArrayList<>();

    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

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

    // Se recarga cada vez que la pantalla vuelve a verse
    @Override
    protected void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    private void cargarFavoritos() {
        ejecutor.execute(() -> {
            // --- Hilo secundario: consultas a la base ---
            Set<Integer> favoritos = dbHelper.obtenerFavoritosUsuario(sesion.getUsuarioId());
            List<Patron> todos = dbHelper.obtenerPatrones();

            List<Patron> soloFavoritos = new ArrayList<>();
            for (Patron p : todos) {
                if (favoritos.contains(p.getId())) {
                    soloFavoritos.add(p);
                }
            }

            // --- Hilo principal: actualizar la pantalla ---
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

    // Muestra la lista, o el mensaje si no hay favoritos
    private void mostrarLista() {
        adapter.actualizarLista(new ArrayList<>(patronesFavoritos));

        boolean vacio = patronesFavoritos.isEmpty();
        tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        rvFavoritos.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cierra la base en el mismo hilo, después de las consultas pendientes
        ejecutor.execute(() -> {
            if (dbHelper != null) {
                dbHelper.close();
            }
        });
        ejecutor.shutdown();
    }
}