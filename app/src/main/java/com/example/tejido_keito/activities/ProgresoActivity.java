package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.adapters.ProgresoAdapter;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.models.Patron;
import com.example.tejido_keito.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Muestra los patrones que el usuario ya empezó, con la vuelta en que va.
 * Desde aquí se puede continuar en el contador o quitar el avance.
 */
public class ProgresoActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private ProgresoAdapter adapter;
    private TextView tvVacio;
    private RecyclerView rvProgreso;
    private long usuarioId;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    /**
     *  arma la lista y define que pasa al abrir o quitar un patron
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        dbHelper = new KeitoDbHelper(this);
        usuarioId = new SessionManager(this).getUsuarioId();

        tvVacio = findViewById(R.id.tvVacio);
        rvProgreso = findViewById(R.id.rvProgreso);
        rvProgreso.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProgresoAdapter(new ProgresoAdapter.OnProgresoListener() {
            @Override
            public void onAbrir(Patron patron) {
                abrirContador(patron);
            }

            @Override
            public void onQuitar(Patron patron) {
                confirmarQuitar(patron);
            }
        });
        rvProgreso.setAdapter(adapter);
    }

    /**
     * Lee el progreso del usuario y los patrones en un hilo secundario, deja solo
     * los que ya empezó y los muestra al terminar.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarProgreso();
    }

    private void cargarProgreso() {
        ejecutor.execute(() -> {
            Map<Integer, Integer> progresos = dbHelper.obtenerProgresoUsuario(usuarioId);
            List<Patron> todos = dbHelper.obtenerPatrones();

            List<Patron> empezados = new ArrayList<>();
            for (Patron p : todos) {
                if (progresos.containsKey(p.getId())) {
                    empezados.add(p);
                }
            }

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                adapter.actualizar(empezados, progresos);

                boolean vacio = empezados.isEmpty();
                tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
                rvProgreso.setVisibility(vacio ? View.GONE : View.VISIBLE);
            });
        });
    }

    /**
     *  Abre el contador del patrón, mandando por Intent id, nombre, totalVueltas, abreviaciones e instrucciones.
     *
     * @param patron    patrón que se quiere continuar
     */
    private void abrirContador(Patron patron) {
        Intent intent = new Intent(this, ContadorActivity.class);
        intent.putExtra("id", patron.getId());
        intent.putExtra("nombre", patron.getNombre());
        intent.putExtra("totalVueltas", patron.getTotalVueltas());
        intent.putExtra("abreviaciones", patron.getAbreviaciones());
        intent.putExtra("instrucciones", patron.getInstrucciones());
        startActivity(intent);
    }

    /**
     * Pregunta si de verdad se quiere borrar el avance y, si dice que sí, lo elimina en un
     * hilo secundario y recarga la lista.
     *
     * @param patron patrón al que se le borra el avance
     */
    private void confirmarQuitar(Patron patron) {
        new AlertDialog.Builder(this)
                .setTitle("Quitar de mi progreso")
                .setMessage("Se borrará tu avance en «" + patron.getNombre() + "». ¿Continuar?")
                .setPositiveButton("Quitar", (dialog, which) -> ejecutor.execute(() -> {
                    dbHelper.eliminarProgreso(usuarioId, patron.getId());
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        Toast.makeText(this, "Progreso eliminado", Toast.LENGTH_SHORT).show();
                        cargarProgreso();
                    });
                }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Apaga el hilo secundario y cierra la base de datos.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ejecutor.shutdown();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}