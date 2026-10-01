package com.example.tejido_keito.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.models.Patron;
import com.example.tejido_keito.adapters.PatronAdapter;
import com.example.tejido_keito.R;

import java.util.ArrayList;
import java.util.List;

public class BibliotecaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca);

        List<Patron> listaPatrones = new ArrayList<>();
        listaPatrones.add(new Patron(
                "Conejo amigurumi",
                "Principiante",
                30,
                R.drawable.placeholder_patron,
                "pb = punto bajo\nag = anillo mágico\ninc = incremento\ndec = disminución",
                "1. Empezar con un anillo mágico de 6 pb.\n2. Vuelta 2: incrementar en cada punto (12 pb).\n3. Vuelta 3: alternar pb e incremento (18 pb).\n4. Continuar tejiendo el cuerpo según el patrón de la cabeza.\n5. Rellenar y cerrar con disminuciones."
        ));
        listaPatrones.add(new Patron(
                "Ranita amigurumi",
                "Intermedio",
                45,
                R.drawable.placeholder_patron,
                "pb = punto bajo\nag = anillo mágico\ninc = incremento\ndec = disminución\npa = punto alto",
                "1. Anillo mágico de 6 pb para la cabeza.\n2. Aumentar progresivamente hasta 24 pb.\n3. Tejer el cuerpo alternando pb y disminuciones.\n4. Confeccionar las patas por separado y coser."
        ));
        listaPatrones.add(new Patron(
                "Osito amigurumi",
                "Avanzado",
                70,
                R.drawable.placeholder_patron,
                "pb = punto bajo\nag = anillo mágico\ninc = incremento\ndec = disminución\npa = punto alto\nppa = punto alto doble",
                "1. Anillo mágico de 6 pb.\n2. Aumentar hasta 36 pb para la cabeza.\n3. Tejer orejas por separado (2 piezas de 8 pb).\n4. Unir orejas a la cabeza.\n5. Tejer cuerpo, brazos y piernas, rellenar y ensamblar."
        ));

        RecyclerView rvPatrones = findViewById(R.id.rvPatrones);
        rvPatrones.setLayoutManager(new LinearLayoutManager(this));
        rvPatrones.setAdapter(new PatronAdapter(listaPatrones));


        EditText etBuscar = findViewById(R.id.etBuscar);
        PatronAdapter adapter = new PatronAdapter(listaPatrones);
        rvPatrones.setAdapter(adapter);

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Patron> listaFiltrada = new ArrayList<>();
                for (Patron patron : listaPatrones) {
                    if (patron.getNombre().toLowerCase().contains(s.toString().toLowerCase())) {
                        listaFiltrada.add(patron);
                    }
                }
                adapter.actualizarLista(listaFiltrada);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}