package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatDelegate;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tejido_keito.R;
import com.example.tejido_keito.utils.SessionManager;

/**
 * Pantalla de inicio. Si ya hay una sesion guardada entra directo a la biblioteca
 * si no, musetra los botones para iniciar sesiom o registrarse
 */

public class MainActivity extends AppCompatActivity {

    /**
     * revisa la sesion con sessionManager. si no hay arma la pantalla de bienbenida,
     * tambien mantiene la app en modo claro
     *
     * @param savedInstanceState estado anterior de la pantalla, si lo hay (por ejemplo al girar el teléfono)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        if (session.estaLogueado()){
            startActivity(new Intent(MainActivity.this, BibliotecaActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Button btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        Button btnRegistrarse = findViewById(R.id.btnRegistrarse);

        btnIniciarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
    }
}