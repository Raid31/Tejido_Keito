package com.example.tejido_keito.database;

import android.provider.BaseColumns;

/**
 *  contrato de la base de datos. Aqui estan los nombres de las tablas y columnas.
 *  <p>
 *      cada tabla es una clase interna que implementa {@link BaseColumns}, que
 *      agrega la columna {@code _id}. La clase es final y tiene el constructor
 *      privado porque solo guarda constantes y no se debe instanciar.
 *  </p>
 */

public final class KeitoContract {

    private KeitoContract() {}

    /** Tabla de usuarios */
    public static class UsuarioEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD = "password";
    }

    /** tabla videos tutoriales */
    public static class TutorialEntry implements BaseColumns {
        public static final String TABLE_NAME = "tutoriales";
        public static final String COLUMN_TITULO = "titulo";
        public static final String COLUMN_DESCRIPCION = "descripcion";
        public static final String COLUMN_VIDEO = "video";   // nombre del archivo en res/raw
    }

    /** Tabla de patrones */
    public static class PatronEntry implements BaseColumns {
        public static final String TABLE_NAME = "patrones";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_DIFICULTAD = "dificultad";
        public static final String COLUMN_TOTAL_VUELTAS = "total_vueltas";
        public static final String COLUMN_IMAGEN = "imagen";
        public static final String COLUMN_ABREVIACIONES = "abreviaciones";
        public static final String COLUMN_INSTRUCCIONES = "instrucciones";
    }

    /** Tabla de progreso (en cada patron)*/
    public static class ProgresoEntry implements BaseColumns {
        public static final String TABLE_NAME = "progreso_patron";
        public static final String COLUMN_USUARIO_ID = "usuario_id";
        public static final String COLUMN_PATRON_ID = "patron_id";
        public static final String COLUMN_VUELTA_ACTUAL = "vuelta_actual";
        public static final String COLUMN_ULTIMA_ACTUALIZACION = "ultima_actualizacion";
    }

    /** Tabla de tiendas */
    public static class TiendaEntry implements BaseColumns{
        public static final String TABLE_NAME = "tiendas";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_LATITUD = "latitud";
        public static final String COLUMN_LONGITUD = "longitud";
    }

    /** Tabla de favoritos */
    public  static  class  FavoritoEntry implements  BaseColumns{
        public static  final  String TABLE_NAME = "favoritos";
        public static  final  String COLUMN_USUARIO_ID = "usuario_id";
        public static  final  String COLUMN_PATRON_ID = "patron_id";
    }
}