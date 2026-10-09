package com.example.tejido_keito.database;

import android.provider.BaseColumns;

public final class KeitoContract {

    private KeitoContract() {}

    // Tabla de usuarios
    public static class UsuarioEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
    }

    // Tabla de patrones
    public static class PatronEntry implements BaseColumns {
        public static final String TABLE_NAME = "patrones";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_DIFICULTAD = "dificultad";
        public static final String COLUMN_TOTAL_VUELTAS = "total_vueltas";
        public static final String COLUMN_IMAGEN = "imagen";
        public static final String COLUMN_ABREVIACIONES = "abreviaciones";
        public static final String COLUMN_INSTRUCCIONES = "instrucciones";
    }

    // Tabla de progreso
    public static class ProgresoEntry implements BaseColumns {
        public static final String TABLE_NAME = "progreso_patron";
        public static final String COLUMN_USUARIO_ID = "usuario_id";
        public static final String COLUMN_PATRON_ID = "patron_id";
        public static final String COLUMN_VUELTA_ACTUAL = "vuelta_actual";
        public static final String COLUMN_ULTIMA_ACTUALIZACION = "ultima_actualizacion";
    }
}