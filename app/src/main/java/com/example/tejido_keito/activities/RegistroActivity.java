package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tejido_keito.R;
import com.example.tejido_keito.database.KeitoDbHelper;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etCorreoRegistro;
    private EditText etContrasenaRegistro;
    private EditText etConfirmarContrasena;
    private KeitoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            // Guardar el usuario en SQLite (devuelve -1 si el correo ya existe)
            long resultado = dbHelper.insertarUsuario(nombre, correo, contrasena);
            if (resultado == -1) {
                Toast.makeText(this, "Ese correo ya está registrado", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Cuenta creada, inicia sesión", Toast.LENGTH_SHORT).show();
            irALogin(correo);
        });

        tvIrLogin.setOnClickListener(v -> irALogin(null));
    }

    // Vuelve al Login sin apilar pantallas repetidas
    private void irALogin(String correo) {
        Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (correo != null) {
            intent.putExtra("correo", correo);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}