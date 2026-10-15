package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tejido_keito.R;
import com.example.tejido_keito.adapters.PatronAdapter;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.models.Patron;
import com.example.tejido_keito.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BibliotecaActivity extends AppCompatActivity {

    private KeitoDbHelper dbHelper;
    private SessionManager sesion;
    private List<Patron> listaPatrones = new ArrayList<>();
    private PatronAdapter adapter;
    private EditText etBuscar;
    private RadioGroup rgDificultad;
    private DrawerLayout drawerLayout;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca);

        dbHelper = new KeitoDbHelper(this);
        sesion = new SessionManager(this);

        RecyclerView rvPatrones = findViewById(R.id.rvPatrones);
        rvPatrones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PatronAdapter(new ArrayList<>());
        rvPatrones.setAdapter(adapter);

        // Lista de patrones desde SQLite (en segundo plano)
        cargarPatrones();

        //Buscador + filtro por dificultad
        etBuscar = findViewById(R.id.etBuscar);
        rgDificultad = findViewById(R.id.rgDificultad);

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

        // Menú lateral
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageButton btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Mostrar el correo de la sesion en la cabecera del menu
        View cabecera = navigationView.getHeaderView(0);
        TextView tvCorreoMenu = cabecera.findViewById(R.id.tvCorreoMenu);
        tvCorreoMenu.setText(sesion.getCorreo());

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_progreso) {
                startActivity(new Intent(BibliotecaActivity.this, ProgresoActivity.class));
            } else if (id == R.id.menu_tutoriales) {
                startActivity(new Intent(BibliotecaActivity.this, TutorialesActivity.class));
            } else if (id == R.id.menu_favoritos) {
                Toast.makeText(this, "Favoritos: próximamente", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_cerrar_sesion) {
                cerrarSesion();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // ---------- Botón Atrás: si el menú está abierto, lo cierra primero ----------
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // Lee los patrones en un hilo secundario y los muestra al terminar
    private void cargarPatrones() {
        ejecutor.execute(() -> {
            List<Patron> patrones = dbHelper.obtenerPatrones();

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                listaPatrones = patrones;
                filtrar();   // aplica lo que haya escrito/seleccionado (por defecto, todos)
            });
        });
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

    // Borra la sesión guardada y vuelve a la pantalla de bienvenida
    private void cerrarSesion() {
        sesion.cerrarSesion();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(BibliotecaActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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