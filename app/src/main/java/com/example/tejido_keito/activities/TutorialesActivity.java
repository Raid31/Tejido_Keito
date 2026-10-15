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

public class TutorialesActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private TutorialAdapter adapter;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

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

    private void abrirVideo(Tutorial tutorial) {
        // Comprobar que el archivo existe en res/raw
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ejecutor.shutdown();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}