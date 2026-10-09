package com.example.tejido_keito.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
        tvAbreviaciones.setText(formatearAbreviaciones(abreviaciones));
        tvInstrucciones.setText(formatearInstrucciones(instrucciones));

        btnEmpezarTejer.setOnClickListener(v -> {
            // Por ahora no hace nada; aquí conectaremos el Contador de vueltas más adelante
        });
    }

    // Abreviaciones: deja en negrita la sigla (lo que va antes del " = ")
    private CharSequence formatearAbreviaciones(String texto) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (texto == null) return sb;

        String[] lineas = texto.split("\n");
        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty()) continue;

            int inicio = sb.length();
            sb.append(l);

            int igual = l.indexOf(" = ");
            if (igual > 0) {
                sb.setSpan(new StyleSpan(Typeface.BOLD), inicio, inicio + igual,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            sb.append("\n");
        }
        quitarUltimoSalto(sb);
        return sb;
    }

    // Instrucciones: títulos de pieza destacados y etiquetas "V1:" en negrita
    private CharSequence formatearInstrucciones(String texto) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (texto == null) return sb;

        int colorTitulo = ContextCompat.getColor(this, R.color.keito_morado);
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty()) continue;

            if (esTitulo(l)) {
                // Espacio extra antes de cada título (menos el primero)
                if (sb.length() > 0) {
                    int s = sb.length();
                    sb.append("\n");
                    sb.setSpan(new RelativeSizeSpan(0.6f), s, sb.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                int inicio = sb.length();
                sb.append(l);
                int fin = sb.length();
                sb.setSpan(new StyleSpan(Typeface.BOLD), inicio, fin,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(colorTitulo), inicio, fin,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new RelativeSizeSpan(1.15f), inicio, fin,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.append("\n");
            } else {
                int inicio = sb.length();
                sb.append(l);

                // Etiqueta antes de los dos puntos en negrita ("V1:", "Ojos:", etc.)
                int dosPuntos = l.indexOf(':');
                if (dosPuntos > 0 && dosPuntos <= 30 && !l.matches("^\\d+\\..*")) {
                    sb.setSpan(new StyleSpan(Typeface.BOLD), inicio, inicio + dosPuntos + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                sb.append("\n");
            }
        }
        quitarUltimoSalto(sb);
        return sb;
    }

    // Un título es una línea en MAYÚSCULAS, sin ":" (ej: "CABEZA (verde)", "ARMADO")
    private boolean esTitulo(String l) {
        if (l.contains(":")) return false;
        int parentesis = l.indexOf('(');
        String base = (parentesis >= 0 ? l.substring(0, parentesis) : l).trim();
        if (base.length() < 3) return false;
        return base.equals(base.toUpperCase()) && base.matches(".*[A-ZÁÉÍÓÚÑ].*");
    }

    private void quitarUltimoSalto(SpannableStringBuilder sb) {
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
    }
}