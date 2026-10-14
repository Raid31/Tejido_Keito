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

public class ProgresoActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private ProgresoAdapter adapter;
    private TextView tvVacio;
    private RecyclerView rvProgreso;
    private long usuarioId;

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

    // Se recarga cada vez que la pantalla vuelve a verse (por ejemplo, al volver del contador)
    @Override
    protected void onResume() {
        super.onResume();
        cargarProgreso();
    }

    private void cargarProgreso() {
        Map<Integer, Integer> progresos = dbHelper.obtenerProgresoUsuario(usuarioId);
        List<Patron> todos = dbHelper.obtenerPatrones();

        List<Patron> empezados = new ArrayList<>();
        for (Patron p : todos) {
            if (progresos.containsKey(p.getId())) {
                empezados.add(p);
            }
        }

        adapter.actualizar(empezados, progresos);

        boolean vacio = empezados.isEmpty();
        tvVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        rvProgreso.setVisibility(vacio ? View.GONE : View.VISIBLE);
    }

    private void abrirContador(Patron patron) {
        Intent intent = new Intent(this, ContadorActivity.class);
        intent.putExtra("id", patron.getId());
        intent.putExtra("nombre", patron.getNombre());
        intent.putExtra("totalVueltas", patron.getTotalVueltas());
        intent.putExtra("abreviaciones", patron.getAbreviaciones());
        intent.putExtra("instrucciones", patron.getInstrucciones());
        startActivity(intent);
    }

    private void confirmarQuitar(Patron patron) {
        new AlertDialog.Builder(this)
                .setTitle("Quitar de mi progreso")
                .setMessage("Se borrará tu avance en «" + patron.getNombre() + "». ¿Continuar?")
                .setPositiveButton("Quitar", (dialog, which) -> {
                    dbHelper.eliminarProgreso(usuarioId, patron.getId());
                    Toast.makeText(this, "Progreso eliminado", Toast.LENGTH_SHORT).show();
                    cargarProgreso();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}