package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.adapters.TutorialAdapter;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.models.Tutorial;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lista de tutoriales en video. Al tocar uno abre VideoActivity.
 */
public class TutorialesActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private TutorialAdapter adapter;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    /**
     * Arma la lista y carga los tutoriales de la base en un hilo secundario
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutoriales);

        dbHelper = new KeitoDbHelper(this);

        RecyclerView rvTutoriales = findViewById(R.id.rvTutoriales);
        rvTutoriales.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TutorialAdapter(this::abrirVideo);
        rvTutoriales.setAdapter(adapter);

        // Cargar la lista en segundo plano
        ejecutor.execute(() -> {
            List<Tutorial> lista = dbHelper.obtenerTutoriales();
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                adapter.actualizar(lista);
            });
        });
    }

    /**
     * Revisa que el video exista en res/raw y abre VideoActivity mandando por Intent titulo,
     * descripcion y video.
     *
     * @param tutorial  tutorial que se tocó
     */
    private void abrirVideo(Tutorial tutorial) {
        int recurso = getResources().getIdentifier(tutorial.getVideo(), "raw", getPackageName());
        if (recurso == 0) {
            Toast.makeText(this, "Este video todavía no está disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("titulo", tutorial.getTitulo());
        intent.putExtra("descripcion", tutorial.getDescripcion());
        intent.putExtra("video", tutorial.getVideo());
        startActivity(intent);
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