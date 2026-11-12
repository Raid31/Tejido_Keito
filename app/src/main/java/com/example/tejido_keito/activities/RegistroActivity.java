package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tejido_keito.R;
import com.example.tejido_keito.database.KeitoDbHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pantalla de registro de usuario. Vlida los datos dy guarda el usuario en SQLite (hilo secundario)
 * Cuando se crea la cuenta vuelve al login
 */
public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etCorreoRegistro;
    private EditText etContrasenaRegistro;
    private EditText etConfirmarContrasena;
    private KeitoDbHelper dbHelper;

    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    /**
     * Arma el formulario de registro, revisa que no hayan capos vacios, que el correo sea valido, que la contraseña
     * tenga al menos 6 caracteres y que las dos coincidan y despues guarda el usuario
     *
     * @param savedInstanceState estado anterior de la pantalla, si lo hay (por ejemplo al girar el teléfono)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,  // Evita capturas de pantalla y que se vea el contenido en la lista de apps recientes
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_registro);

        dbHelper = new KeitoDbHelper(this);

        etNombre = findViewById(R.id.etNombre);
        etCorreoRegistro = findViewById(R.id.etCorreoRegistro);
        etContrasenaRegistro = findViewById(R.id.etContrasenaRegistro);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        Button btnCrearCuenta = findViewById(R.id.btnCrearCuenta);
        TextView tvIrLogin = findViewById(R.id.tvIrLogin);

        btnCrearCuenta.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String correo = etCorreoRegistro.getText().toString().trim().toLowerCase();
            String contrasena = etContrasenaRegistro.getText().toString().trim();
            String confirmar = etConfirmarContrasena.getText().toString().trim();

            if (nombre.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmar.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (contrasena.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!contrasena.equals(confirmar)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Evita crear la cuenta dos veces si se toca el botón rápido
            btnCrearCuenta.setEnabled(false);

            ejecutor.execute(() -> {
                //Hilo secundario: guardar en SQLite (devuelve -1 si el correo ya existe)
                long resultado = dbHelper.insertarUsuario(nombre, correo, contrasena);

                //Hilo principal: reaccionar al resultado
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (resultado == -1) {
                        btnCrearCuenta.setEnabled(true);
                        Toast.makeText(this, "Ese correo ya está registrado",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "Cuenta creada, inicia sesión", Toast.LENGTH_SHORT).show();
                    irALogin(correo);
                });
            });
        });

        tvIrLogin.setOnClickListener(v -> irALogin(null));
    }

    /**
     * vuelve al login sin pantallas repetidas
     *
     * @param correo    correo reciem registrado (se manda como extra "correo" para dejarlo escrito en el login),
     *                  o null si no hay
     */
    private void irALogin(String correo) {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (correo != null) {
            intent.putExtra("correo", correo);
        }
        startActivity(intent);
        finish();
    }

    /**
     * Apaga el hilo secundario y cierra la base de datos
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