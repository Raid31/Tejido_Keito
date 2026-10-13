package com.example.tejido_keito.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import androidx.core.content.ContextCompat;

import com.example.tejido_keito.R;

public class FormatoTexto {

    // Abreviaciones: deja en negrita la sigla (lo que va antes del " = ")
    public static CharSequence abreviaciones(String texto) {
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
    public static CharSequence instrucciones(Context context, String texto) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (texto == null) return sb;

        int colorTitulo = ContextCompat.getColor(context, R.color.keito_morado);
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            String l = linea.trim();
            if (l.isEmpty()) continue;

            if (esTitulo(l)) {
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

    // Un título es una línea en MAYÚSCULAS, sin ":" (ej: "CABEZA", "ARMADO")
    private static boolean esTitulo(String l) {
        if (l.contains(":")) return false;
        int parentesis = l.indexOf('(');
        String base = (parentesis >= 0 ? l.substring(0, parentesis) : l).trim();
        if (base.length() < 3) return false;
        return base.equals(base.toUpperCase()) && base.matches(".*[A-ZÁÉÍÓÚÑ].*");
    }

    private static void quitarUltimoSalto(SpannableStringBuilder sb) {
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
    }
}