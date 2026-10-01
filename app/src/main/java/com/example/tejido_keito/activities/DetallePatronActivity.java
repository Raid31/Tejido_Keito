package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.tejido_keito.R;

public class DetallePatronActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_patron);

        ImageView ivDetalle = findViewById(R.id.ivDetalle);
        TextView tvNombreDetalle = findViewById(R.id.tvNombreDetalle);
        TextView tvDificultadDetalle = findViewById(R.id.tvDificultadDetalle);
        TextView tvVueltasDetalle = findViewById(R.id.tvVueltasDetalle);
        TextView tvAbreviaciones = findViewById(R.id.tvAbreviaciones);
        TextView tvInstrucciones = findViewById(R.id.tvInstrucciones);
        Button btnEmpezarTejer = findViewById(R.id.btnEmpezarTejer);

        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String dificultad = intent.getStringExtra("dificultad");
        int totalVueltas = intent.getIntExtra("totalVueltas", 0);
        int imagenResId = intent.getIntExtra("imagenResId", R.drawable.placeholder_patron);
        String abreviaciones = intent.getStringExtra("abreviaciones");
        String instrucciones = intent.getStringExtra("instrucciones");

        ivDetalle.setImageResource(imagenResId);
        tvNombreDetalle.setText(nombre);
        tvDificultadDetalle.setText("Dificultad: " + dificultad);
        tvVueltasDetalle.setText("Total de vueltas: " + totalVueltas);
        tvAbreviaciones.setText(abreviaciones);
        tvInstrucciones.setText(instrucciones);

        btnEmpezarTejer.setOnClickListener(v -> {
            // Por ahora no hace nada; aquí conectaremos el Contador de vueltas más adelante
        });
    }
}