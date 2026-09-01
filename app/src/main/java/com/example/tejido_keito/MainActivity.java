package com.example.tejido_keito;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnComenzar = findViewById(R.id.btnComenzar);

        btnComenzar.setOnClickListener(v -> {
            Toast.makeText(this, "¡Bienvenida a Keito!", Toast.LENGTH_SHORT).show();
        });
    }
}