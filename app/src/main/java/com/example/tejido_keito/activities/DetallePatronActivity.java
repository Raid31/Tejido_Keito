package com.example.tejido_keito.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tejido_keito.R;
import com.example.tejido_keito.utils.FormatoTexto;

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
        int patronId = intent.getIntExtra("id", -1);

        ivDetalle.setImageResource(imagenResId);
        tvNombreDetalle.setText(nombre);
        tvDificultadDetalle.setText("Dificultad: " + dificultad);
        tvVueltasDetalle.setText("Total de vueltas: " + totalVueltas);
        tvAbreviaciones.setText(FormatoTexto.abreviaciones(abreviaciones));
        tvInstrucciones.setText(FormatoTexto.instrucciones(this, instrucciones));

        // Secciones que se despliegan al tocar el encabezado
        configurarDesplegable(
                findViewById(R.id.headerAbreviaciones),
                tvAbreviaciones,
                findViewById(R.id.tvFlechaAbreviaciones));

        configurarDesplegable(
                findViewById(R.id.headerInstrucciones),
                tvInstrucciones,
                findViewById(R.id.tvFlechaInstrucciones));

        btnEmpezarTejer.setOnClickListener(v -> {
            Intent intentContador = new Intent(DetallePatronActivity.this, ContadorActivity.class);
            intentContador.putExtra("id", patronId);
            intentContador.putExtra("nombre", nombre);
            intentContador.putExtra("totalVueltas", totalVueltas);
            intentContador.putExtra("abreviaciones", abreviaciones);
            intentContador.putExtra("instrucciones", instrucciones);
            startActivity(intentContador);
        });
    }

    // Al tocar el encabezado: muestra u oculta el contenido y cambia la flecha
    private void configurarDesplegable(View encabezado, TextView contenido, TextView flecha) {
        encabezado.setOnClickListener(v -> {
            boolean estabaVisible = contenido.getVisibility() == View.VISIBLE;
            contenido.setVisibility(estabaVisible ? View.GONE : View.VISIBLE);
            flecha.setText(estabaVisible ? "▼" : "▲");
        });
    }
}