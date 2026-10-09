package com.example.tejido_keito.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.adapters.PatronAdapter;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.models.Patron;

import java.util.ArrayList;
import java.util.List;

public class BibliotecaActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private List<Patron> listaPatrones;
    private PatronAdapter adapter;
    private EditText etBuscar;
    private RadioGroup rgDificultad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca);

        // 1. Leer los patrones desde SQLite
        dbHelper = new KeitoDbHelper(this);
        listaPatrones = dbHelper.obtenerPatrones();

        // 2. RecyclerView con un solo adaptador
        RecyclerView rvPatrones = findViewById(R.id.rvPatrones);
        rvPatrones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatronAdapter(listaPatrones);
        rvPatrones.setAdapter(adapter);

        etBuscar = findViewById(R.id.etBuscar);
        rgDificultad = findViewById(R.id.rgDificultad);

        // 3. Cada vez que cambia el texto o el radio, se vuelve a filtrar
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrar();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgDificultad.setOnCheckedChangeListener((group, checkedId) -> filtrar());
    }

    // Aplica buscador + dificultad al mismo tiempo
    private void filtrar() {
        String texto = etBuscar.getText().toString().toLowerCase().trim();

        String dificultad = "";   // vacío = Todos
        int seleccionado = rgDificultad.getCheckedRadioButtonId();
        if (seleccionado == R.id.rbPrincipiante) {
            dificultad = "Principiante";
        } else if (seleccionado == R.id.rbIntermedio) {
            dificultad = "Intermedio";
        } else if (seleccionado == R.id.rbAvanzado) {
            dificultad = "Avanzado";
        }

        List<Patron> listaFiltrada = new ArrayList<>();
        for (Patron patron : listaPatrones) {
            boolean coincideNombre = patron.getNombre().toLowerCase().contains(texto);
            boolean coincideDificultad = dificultad.isEmpty()
                    || patron.getDificultad().equals(dificultad);

            if (coincideNombre && coincideDificultad) {
                listaFiltrada.add(patron);
            }
        }
        adapter.actualizarLista(listaFiltrada);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}