package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tejido_keito.R;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.utils.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etContrasena;
    private KeitoDbHelper dbHelper;
    private SessionManager sesion;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new KeitoDbHelper(this);
        sesion = new SessionManager(this);

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        Button btnEntrar = findViewById(R.id.btnEntrar);
        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);

        // Si venimos del registro, el correo ya aparece escrito
        String correoRegistrado = getIntent().getStringExtra("correo");
        if (correoRegistrado != null) {
            etCorreo.setText(correoRegistrado);
            etContrasena.requestFocus();
        }

        btnEntrar.setOnClickListener(v -> {
            String correo = etCorreo.getText().toString().trim().toLowerCase();
            String contrasena = etContrasena.getText().toString().trim();

            if (correo.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            // Evita que se toque dos veces mientras se consulta
            btnEntrar.setEnabled(false);

            ejecutor.execute(() -> {
                // --- Hilo secundario: consultar en SQLite (devuelve el id del usuario o -1) ---
                long usuarioId = dbHelper.validarLogin(correo, contrasena);

                // --- Hilo principal: reaccionar al resultado ---
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (usuarioId == -1) {
                        btnEntrar.setEnabled(true);
                        Toast.makeText(this, "Correo o contraseña incorrectos",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Guardar la sesión para que siga abierta al cerrar la app
                    sesion.guardarSesion(usuarioId, correo);

                    // Entrar a la Biblioteca y borrar las pantallas anteriores:
                    // así el botón Atrás no vuelve al Login
                    Intent intent = new Intent(LoginActivity.this, BibliotecaActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            });
        });

        tvIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });
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