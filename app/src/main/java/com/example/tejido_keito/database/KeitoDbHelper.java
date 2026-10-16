package com.example.tejido_keito.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.tejido_keito.database.KeitoContract.PatronEntry;
import com.example.tejido_keito.database.KeitoContract.ProgresoEntry;
import com.example.tejido_keito.database.KeitoContract.UsuarioEntry;
import com.example.tejido_keito.models.Patron;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import com.example.tejido_keito.database.KeitoContract.TutorialEntry;
import com.example.tejido_keito.models.Tutorial;
import com.example.tejido_keito.database.KeitoContract.TiendaEntry;
import com.example.tejido_keito.models.Tienda;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeitoDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Keito.db";

    private final Context appContext;

    private static final String SQL_CREATE_TUTORIALES =
            "CREATE TABLE " + TutorialEntry.TABLE_NAME + " (" +
                    TutorialEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TutorialEntry.COLUMN_TITULO + " TEXT NOT NULL, " +
                    TutorialEntry.COLUMN_DESCRIPCION + " TEXT, " +
                    TutorialEntry.COLUMN_VIDEO + " TEXT NOT NULL)";

    private static final String SQL_DELETE_TUTORIALES =
            "DROP TABLE IF EXISTS " + TutorialEntry.TABLE_NAME;

    private static final String SQL_CREATE_USUARIOS =
            "CREATE TABLE " + UsuarioEntry.TABLE_NAME + " (" +
                    UsuarioEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    UsuarioEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    UsuarioEntry.COLUMN_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    UsuarioEntry.COLUMN_PASSWORD + " TEXT NOT NULL);";

    private static final String SQL_CREATE_PATRONES =
            "CREATE TABLE " + PatronEntry.TABLE_NAME + " (" +
                    PatronEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PatronEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    PatronEntry.COLUMN_DIFICULTAD + " TEXT NOT NULL, " +
                    PatronEntry.COLUMN_TOTAL_VUELTAS + " INTEGER NOT NULL, " +
                    PatronEntry.COLUMN_IMAGEN + " TEXT NOT NULL, " +
                    PatronEntry.COLUMN_ABREVIACIONES + " TEXT, " +
                    PatronEntry.COLUMN_INSTRUCCIONES + " TEXT);";

    private static final String SQL_CREATE_PROGRESO =
            "CREATE TABLE " + ProgresoEntry.TABLE_NAME + " (" +
                    ProgresoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProgresoEntry.COLUMN_USUARIO_ID + " INTEGER NOT NULL, " +
                    ProgresoEntry.COLUMN_PATRON_ID + " INTEGER NOT NULL, " +
                    ProgresoEntry.COLUMN_VUELTA_ACTUAL + " INTEGER NOT NULL DEFAULT 0, " +
                    ProgresoEntry.COLUMN_ULTIMA_ACTUALIZACION + " TEXT, " +
                    "FOREIGN KEY (" + ProgresoEntry.COLUMN_USUARIO_ID + ") REFERENCES " +
                    UsuarioEntry.TABLE_NAME + "(" + UsuarioEntry._ID + "), " +
                    "FOREIGN KEY (" + ProgresoEntry.COLUMN_PATRON_ID + ") REFERENCES " +
                    PatronEntry.TABLE_NAME + "(" + PatronEntry._ID + "));";

    private static final String SQL_DELETE_USUARIOS =
            "DROP TABLE IF EXISTS " + UsuarioEntry.TABLE_NAME;
    private static final String SQL_DELETE_PATRONES =
            "DROP TABLE IF EXISTS " + PatronEntry.TABLE_NAME;
    private static final String SQL_DELETE_PROGRESO =
            "DROP TABLE IF EXISTS " + ProgresoEntry.TABLE_NAME;

    public KeitoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    private static final String SQL_CREATE_TIENDAS =
            "CREATE TABLE " + TiendaEntry.TABLE_NAME + " (" +
                    TiendaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TiendaEntry.COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    TiendaEntry.COLUMN_LATITUD + " REAL NOT NULL, " +
                    TiendaEntry.COLUMN_LONGITUD + " REAL NOT NULL)";

    private static final String SQL_DELETE_TIENDAS =
            "DROP TABLE IF EXISTS " + TiendaEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USUARIOS);
        db.execSQL(SQL_CREATE_PATRONES);
        db.execSQL(SQL_CREATE_PROGRESO);
        precargarPatrones(db);
        db.execSQL(SQL_CREATE_TUTORIALES);
        precargarTutoriales(db);
        db.execSQL(SQL_CREATE_TIENDAS);
        precargarTiendas(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PROGRESO);
        db.execSQL(SQL_DELETE_PATRONES);
        db.execSQL(SQL_DELETE_USUARIOS);
        db.execSQL(SQL_DELETE_TUTORIALES);
        db.execSQL(SQL_DELETE_TIENDAS);
        onCreate(db);
    }

    // ---------- Precarga de patrones ----------

    private void precargarPatrones(SQLiteDatabase db) {

        insertarPatron(db, "Conejo", "Principiante", 32, "conejo",
                "V = vuelta\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución (2 pb juntos)\ncad = cadeneta\naum triple = 3 pb en el mismo punto\n( )*6 = repetir lo que va entre paréntesis 6 veces\n(12 pb) = total de puntos al terminar la vuelta",
                "CUERPO\nV1: Anillo mágico, 6 pb\nV2: 6 aum (12 pb)\nV3: (1 pb, aum)*6 (18 pb)\nV4: (2 pb, aum)*6 (24 pb)\nV5: (3 pb, aum)*6 (30 pb)\nV6: 30 pb\nV7: (4 pb, aum)*6 (36 pb)\nV8 - V9: 36 pb\nV10: (5 pb, aum)*6 (42 pb)\nV11 - V13: 42 pb\nV14: (5 pb, dism)*6 (36 pb)\nV15: (4 pb, dism)*6 (30 pb)\nV16: (3 pb, dism)*6 (24 pb)\nV17: (2 pb, dism)*6 (18 pb)\nV18: (1 pb, dism)*6 (12 pb)\n\n" +
                        "PIES (x2)\nV1: Anillo mágico, 8 pb\nV2: 8 pb\n\n" +
                        "BOLSA\nV1: 6 cad, 4 pb, 1 aum triple (tejer 3 pb en el mismo punto), 3 pb, 1 aum (12 pb)\nV2 - V4: 12 pb\n\n" +
                        "OREJAS (x2)\nV1: Anillo mágico, 6 pb\nV2: 6 aum (12 pb)\nV3 - V4: 12 pb\nV5: 3 veces (2 pb, dism) (9 pb)\nV6: 9 pb\n\n" +
                        "BRAZOS (x2)\nV1: Anillo mágico, 7 pb\nV2: 7 pb");

        insertarPatron(db, "Gatito alien", "Principiante", 34, "gatito_alien",
                "V = vuelta\nAM = anillo mágico\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución (se recomienda invisible)\npd = punto deslizado\ncad = cadeneta\n( ) x 6 = repetir lo que va entre paréntesis 6 veces\n(12) = total de puntos al terminar la vuelta",
                "CABEZA (verde)\nV1: AM, 6 pb (6)\nV2: aum x 6 (12)\nV3: (pb, aum) x 6 (18)\nV4: (2 pb, aum) x 6 (24)\nV5: (3 pb, aum) x 6 (30)\nV6 - V9: pb alrededor (30) durante 4 vueltas\nV10: (3 pb, dism) x 6 (24)\nV11: (2 pb, dism) x 6 (18)\nColocar los ojos de seguridad entre V7 y V8, a unos 5-6 puntos de distancia (ajustar según la expresión que prefieras).\nV12: (pb, dism) x 6 (12)\nRellenar la cabeza firmemente.\nV13: dism x 6 (6)\nRematar, cerrar el agujero y esconder el hilo.\n\n" +
                        "CUERPO (verde)\nV1: AM, 6 pb (6)\nV2: aum x 6 (12)\nV3: (pb, aum) x 6 (18)\nV4: (2 pb, aum) x 6 (24)\nV5 - V7: pb alrededor (24) durante 3 vueltas\nV8: (2 pb, dism) x 6 (18)\nRellenar ligeramente y dar forma de 'frijol' redondeado.\nV9: (pb, dism) x 6 (12)\nRellenar un poco más.\nV10: dism x 6 (6)\nRematar, cerrar el agujero y dejar un hilo largo para unir la cabeza.\n\n" +
                        "OREJAS (x2, verde)\nV1: AM, 3 pb (3)\nV2: (pb, aum) x 1, pb (4)\nV3: pb alrededor (4)\nRematar, dejar un hilo para coser.\n(Opcional: pellizcar suavemente en forma de triángulo al coser.)\n\n" +
                        "BRAZOS (x2, verde)\nV1: AM, 6 pb (6)\nV2 - V4: pb alrededor (6) durante 3 vueltas\nRellenar ligeramente solo la parte de abajo (opcional).\nAplanar la parte de arriba y hacer pb a través de 3 puntos para cerrar.\nRematar, dejar un hilo para coser.\n\n" +
                        "PIERNAS (x2, verde)\nV1: AM, 6 pb (6)\nV2: pb alrededor (6)\nV3: (pb, aum) x 3 (9)\nV4: pb alrededor (9)\nRellenar ligeramente la zona del pie.\nAplanar la parte de arriba y hacer pb a través de 4-5 puntos para cerrar.\nRematar, dejar un hilo para coser.\n\n" +
                        "DETALLES DE LA CARA\nUsar hilo negro o hilo de bordar y una aguja lanera.\nBigotes: hacer 3 puntadas rectas cortas a cada lado de la cara (cerca de la mitad inferior de los ojos). Mantenerlas cortas para que no se enganchen en los bolsillos.\nBoca: hacer una 'u' pequeña o una línea simple, centrada bajo los ojos. Opcional: una puntada diminuta de nariz sobre la boca (un triángulo muy pequeño o un punto).\nConsejo: anudar firmemente por dentro de la cabeza y pasar los hilos por el relleno para esconderlos.\n\n" +
                        "MONTAJE\n1. Coser las orejas a la cabeza: colocarlas en los lados superiores (aprox. entre V3 y V6) y coser firmemente alrededor de la base para que queden ligeramente hacia afuera.\n2. Unir la cabeza al cuerpo: centrar la cabeza sobre el cuerpo y, con el hilo largo, coser a través de ambas piezas con puntadas apretadas y parejas. Dar la vuelta al menos dos veces para que resista como llavero.\n3. Coser los brazos: unirlos a los lados del cuerpo, justo debajo de la costura de la cabeza, ligeramente hacia adelante para un look de 'abrazo'.\n4. Coser las piernas: unirlas cerca de la parte inferior delantera del cuerpo, con espacio parejo. Verificar que el gatito quede nivelado.\n5. Revisión final: tirar suavemente de cada parte para confirmar que las costuras están firmes, cortar y esconder todos los hilos.");

        insertarPatron(db, "Ranita", "Intermedio", 9, "ranita",
                "V = vuelta\nAM = anillo mágico\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución\nBO = punto bobble (bolita)\n( ) x 2 = repetir lo que va entre paréntesis 2 veces\n(18) = total de puntos al terminar la vuelta",
                "V1: 6 pb en AM (6)\nV2: 6 aum (12)\nV3: aum, pb, aum, BO, (aum, pb) x 2, aum, BO, aum, pb, aum (18)\nV4 - V6: pb alrededor (3 vueltas) (18)\nColocar los ojos dentro de los puntos bobble.\nV7: 6 pb, BO, 2 pb, BO, pb, BO, 2 pb, BO, 3 pb (18)\nV8: (pb, dism) x 6 (12)\nEmpezar a rellenar.\nV9: 6 dism (6)\nRematar y coser el agujero para cerrarlo.\nBordar una boca entre los ojos y agregar rubor en las mejillas.");

        insertarPatron(db, "Hámster", "Avanzado", 12, "hamster",
                "V = vuelta\nAM = anillo mágico\ncad = cadeneta\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución\nmpa = medio punto alto\npa = punto alto\nBO = punto bobble (bolita)\nsalt = saltar punto\ndism 3 juntos = disminución de 3 puntos juntos\n( ) x 3 = repetir lo que va entre paréntesis 3 veces\n(30) = total de puntos al terminar la vuelta",
                "V1: 8 pb en AM (8)\nV2: aum x 8 (16)\nV3: (pb, aum) x 8 (24)\nV4: (3 pb, aum) x 3, (2 cad, 2 mpa en la 2.ª cad desde el gancho) salt 2, pb, aum, (2 cad, 2 mpa en la 2.ª cad desde el gancho) salt 2, pb, aum, 3 pb, aum (30)\nV5: pb, (pb, aum) x 3, 8 pb, 2 mpa en el punto saltado bajo la oreja, aum, pb, aum, 2 mpa en el punto saltado, 8 pb (35)\nV6: pb alrededor (35)\nV7: 21 pb, mpa, (3 pa), mpa, 11 pb (37)\nV8: 5 pb, BO (5 juntos), 31 pb (37)\nV9: pb, (pb, dism) x 3, 10 pb, dism, dism 3 juntos, dism, 10 pb (30)\nV10: (3 pb, dism) x 6 (24)\nRellenar el hámster.\nV11: (pb, dism) x 8 (16)\nV12: dism x 8 (8)\nRematar y coser el agujero para cerrarlo.\n\n" +
                        "TERMINACIONES\nCoser la nariz en la V7, entre las orejas.\nCoser los ojos en la V6, a 3 puntos de distancia.\nUsar sombra de ojos si quieres agregar profundidad.");

        insertarPatron(db, "Mokke (Hanako-kun)", "Principiante", 35, "mokke",
                "V = vuelta\nAM = anillo mágico\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución\n( ) x 6 = repetir lo que va entre paréntesis 6 veces\n(24) = total de puntos al terminar la vuelta",
                "CUERPO\nV1: 6 pb en AM (6)\nV2: aum x 6 (12)\nV3: (pb, aum) x 6 (18)\nV4: (2 pb, aum) x 6 (24)\nV5: 24 pb (24)\nV6: (3 pb, aum) x 6 (30)\nV7: (4 pb, aum) x 6 (36)\nV8 - V9: 36 pb (36)\nV10: (2 pb, dism) x 9 (27)\nV11 - V16: 27 pb (27)\nV17: (pb, dism) x 9 (18)\nV18 - V19: 18 pb (18)\nRellenar y colocar los ojos de seguridad.\nV20: dism x 9 (9)\nRematar dejando un hilo largo para coser y cerrar la abertura.\n\n" +
                        "OREJAS (x2)\nV1: 5 pb en AM (5)\nV2: aum x 5 (10)\nV3: (pb, aum) x 5 (15)\nV4 - V6: 15 pb (15)\nV7: (3 pb, dism) x 3 (12)\nV8 - V10: 12 pb (12)\nV11: (4 pb, dism) x 2 (10)\nV12: 10 pb (10)\nV13: (3 pb, dism) x 2 (8)\nRellenar.\nV14 - V15: 8 pb (8)\nRematar y dejar un hilo largo para coser y unir al cuerpo.\n\n" +
                        "TERMINACIONES\nCoser las orejas al cuerpo.");

        insertarPatron(db, "Maomao (The Apothecary Diaries)", "Avanzado", 17, "maomao",
                "V = vuelta\nAM = anillo mágico\ncad = cadeneta\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución\npa = punto alto\npd = punto deslizado\nBO = punto bubble (5 pa en el mismo punto)\nFLO = solo lazada delantera\nBLO = solo lazada trasera\n( ) x 5 = repetir lo que va entre paréntesis 5 veces\n(10) = total de puntos al terminar la vuelta",
                "Mide aproximadamente 5 cm.\nColores: carne, verde aguacate, vino, negro, verde botella, azul rey y blanco.\n\n" +
                        "CABEZA (color carne)\nV1: 5 pb en AM (5)\nV2: 5 aum (10)\nV3: (pb, aum) x 5 (15)\nV4: (2 pb, aum) x 5 (20)\nV5 - V7: 20 pb, punto sobre punto (20)\nV8: 10 dism (10). Antes de terminar la última disminución, cambiar a verde aguacate y cerrar el punto.\n\n" +
                        "CUERPO (verde aguacate)\nV9 (FLO): pb; aum, y antes de terminar el aumento cambiar a color carne y hacer 1 BO; antes de terminar el 5.º pa cambiar a verde aguacate, jalar, ubicarse en el primer pa y cerrar con verde aguacate. Siguiente punto: aum, pb, aum; antes de terminar este último aumento cambiar a color carne y hacer otro BO, cerrando igual con verde aguacate. Terminar la vuelta con aum, pb, aum (15)\nV10: 15 pb, punto sobre punto (15). Antes de terminar el punto 15, cerrar con color vino.\nV11 (BLO, color vino): 4 pb; antes de terminar el 4.º pb cambiar a negro y hacer 1 BO; antes de terminar el 5.º pa cambiar a vino y cerrar en el primer pa. 3 pb; antes de terminar el 3.º pb cambiar a negro y hacer otro BO, cerrando igual con vino. Terminar con 6 pb (15)\nV12: (pb, dism) x 5 (10). Aquí se puede empezar a rellenar.\nV13: 5 dism (5). 1 pd en el siguiente punto, 1 cad y cortar. Cerrar con aguja lanera.\n\n" +
                        "FALDA (color vino)\nEn las lazadas delanteras de la V11: 2 cad; 1 pa en el mismo punto; 1 pa en el siguiente punto; aum. Seguir toda la vuelta con 2 pb y 1 aum. Al terminar, 1 pd en el primer punto, 1 cad y cortar. Esconder las hebras.\n\n" +
                        "DETALLES\nCon hilo blanco (partido en dos) hacer los detalles de la ropa. Con verde botella hacer el resto de los detalles.\nOjos: entre las vueltas 5 y 6, con 3 puntos de separación entre uno y otro. Bordar con azul rey, de 2 a 3 pasadas.\nOREJAS (x2): 3 cad; volver haciendo 1 pb y 1 pd; 1 cad y cortar. Colocarlas entre las vueltas 4 y 6, a 3 puntos de separación de cada ojo.\n\n" +
                        "CABELLO (verde botella)\nV1: 5 pb en AM (5); 1 pd en el primer punto; 1 cad\nV2: 5 aum (10); 1 pd en el primer punto; 1 cad\nV3 (BLO): (pb, aum) x 5 (15); 1 pd en el primer punto\nMechones 1 a 8: 6 cad, volver con 5 pb, 1 pd en el siguiente punto. Repetir hasta completar 8 mechones.\nMechón 9: 5 cad, volver con 4 pd, 1 pd en el siguiente punto.\nMechones 10 a 13: 3 cad, volver con 2 pd, 1 pd en el siguiente punto (4 mechones).\nMechón 14: 5 cad, volver con 4 pd, 1 pd en el primer punto que se hizo.\nSubir a las lazadas que quedaron libres de la vuelta anterior.\nUbicación: mechones 1 a 8 detrás de la cabeza; mechón 9 frente a la oreja; mechones 10 a 13 sobre los ojos; mechón 14 frente a la oreja. Asegurar las patillas y pegar los extremos.\n\n" +
                        "FLEQUILLO\nEn las lazadas delanteras de la V3 del cabello: 19 cad, volver con 18 pd; 1 pd en el siguiente punto; 4 cad, volver con 3 pd; 1 pd en el siguiente punto; 1 pd; 1 pd. Repetir en espejo: 4 cad y volver con 3 pd, luego 19 cad y volver con 18 pd. Al terminar el mechón 4, 1 cad y cortar. Esconder la hebra.\n\n" +
                        "ACOMODAR EL CABELLO\nLlevar el mechón chico hacia el centro, darle una vuelta al mechón largo, sacar un moñito chico y regresar hacia la frente. Pegar.");

        insertarPatron(db, "Usahana", "Intermedio", 35, "usahana",
                "V = vuelta\nAM = anillo mágico\ncad = cadeneta\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución\n( ) x 6 = repetir lo que va entre paréntesis 6 veces\n(12) = total de puntos al terminar la vuelta",
                "Colores: amarillo claro (cabeza), celeste (cuerpo), rosado y azul (orejas), amarillo y morado (brazos), verde y naranja (pies). Cada pieza se teje por separado y al final se cose.\n\n" +
                        "CABEZA (amarillo claro)\nV1: 6 pb en AM (6)\nV2: 6 aum (12)\nV3: (pb, aum) x 6 (18)\nV4: (2 pb, aum) x 6 (24)\nV5: (3 pb, aum) x 6 (30)\nV6 - V9: 30 pb (4 vueltas) (30)\nV10: (3 pb, dism) x 6 (24)\nV11: (2 pb, dism) x 6 (18)\nV12: (pb, dism) x 6 (12)\nRellenar. Dejar una hebra larga para coser.\n\n" +
                        "OREJAS (x2) (una rosada y una azul)\nV1: 4 pb en AM (4)\nV2: 4 aum (8)\nV3 - V6: 8 pb (4 vueltas) (8)\nDejar una hebra larga para coser.\n\n" +
                        "BRAZOS (x2) (uno amarillo y uno morado)\nV1: 6 pb en AM (6)\nV2: (pb, aum) x 3 (9)\nV3 - V5: 9 pb (3 vueltas) (9)\nDejar una hebra larga para coser.\n\n" +
                        "PIES (x2) (uno verde y uno naranja)\nV1: 6 pb en AM (6)\nV2: 6 aum (12)\nV3 - V4: 12 pb (2 vueltas) (12)\n\n" +
                        "CUERPO (celeste)\nV1: 1 cad en uno de los pies y unir el otro pie con 1 pb, quedando los dos pies unidos en una sola vuelta (24)\nV2: 24 pb (24)\nV3 - V5: 24 pb (3 vueltas) (24)\nV6: (2 pb, dism) x 6 (18)\nV7: 18 pb (18)\nV8: (pb, dism) x 6 (12)\nRellenar.\n\n" +
                        "ARMADO\n1. Coser la cabeza al cuerpo.\n2. Coser los brazos al cuerpo, pasando la aguja solo por 3 puntos del cuerpo.\n3. Coser las orejas empezando en la V4 de la cabeza, con 3 puntos de pegado cada una.\n\n" +
                        "CARA\nOjos: cortar dos óvalos de fieltro azul oscuro y pegarlos en la V6, con 4 puntos de separación entre uno y otro.\nNariz: hacer un nudo con hilo rosado adelgazado (separar el hilo de 5 hebras a 2).\nBoca: bordar con el hilo rosado adelgazado, aflojando un poco el hilo para formar la curva, y pegarlo.\nVersión en fieltro: cortar la boca en fieltro rosado y pegarla entre los ojos.\n\n" +
                        "FLOR (opcional)\nCortar una flor de fieltro rosado y un corazón de fieltro amarillo. Pegar el corazón sobre la flor y ponerla en una oreja.");

        insertarPatron(db, "Porta gafas gato (Van)", "Intermedio", 6, "porta_gafas_gato",
                "V = vuelta\ncad = cadeneta\nmpa = medio punto alto\npa = punto alto\npd = punto deslizado\naum = aumento (2 mpa en el mismo punto)\n( ) x 2 = repetir lo que va entre paréntesis 2 veces\n(32) = total de puntos al terminar la vuelta",
                "Materiales: ganchillo de 4 mm, hilo blanco y naranja (se tejen con dos hebras juntas), marcador de puntos, fieltro y pegamento.\n\n" +
                        "CAMBIO DE COLOR\nTejer el mpa hasta que queden 3 lazadas en el ganchillo. Pasar el hilo del otro color por las 3 lazadas y apretar la lazada anterior. Llevar el hilo que no se usa por detrás, tejiéndolo con los puntos. Mantener el hilo nuevo un poco suelto, del largo del borde de la vuelta anterior, para que no tire hacia adentro.\n\n" +
                        "CUERPO (color blanco)\nV1: nudo corredizo y 17 cad. Desde la 3.ª cadena a partir del ganchillo: 1 mpa (marcar este punto) y 13 mpa, una en cada cadena siguiente (14 mpa). Hacer 3 mpa en la primera cadena que hiciste. Seguir por el otro lado de la cadena con 13 mpa y, en el último punto, 1 aum. Tejer la hebra sobrante mientras avanzas. Cerrar con 1 pd en las 2 primeras cad (32)\n\n" +
                        "V2: 2 cad. 1 aum; 14 mpa; 2 aum, cambiando a naranja en la última lazada de la segunda aum. Con naranja: 1 aum; 2 mpa, volviendo a blanco en la última lazada del segundo mpa. Con blanco: 10 mpa; 1 aum en cada uno de los 2 últimos puntos. Cerrar con 1 pd en las 2 cad (38)\n\n" +
                        "V3: 2 cad. 1 aum en cada uno de los 2 primeros puntos; 15 mpa; 1 aum en cada uno de los 2 puntos siguientes; 1 mpa, cambiando a naranja en su última lazada. Con naranja: 1 aum en cada uno de los 2 puntos siguientes; 4 mpa, volviendo a blanco en la última lazada del cuarto mpa. Con blanco: 9 mpa; 1 aum en cada uno de los 2 puntos siguientes; 1 mpa en el último punto. Cerrar con 1 pd en las 2 cad (46)\n\n" +
                        "V4: 2 cad. (mpa, aum) x 2; 15 mpa; (mpa, aum) x 2; 2 mpa, cambiando a naranja en la última lazada del segundo mpa. Con naranja: 1 aum; (mpa, aum); 6 mpa, volviendo a blanco en la última lazada del sexto mpa. Con blanco: 7 mpa; (mpa, aum) x 2; 1 mpa en el último punto. Cerrar con 1 pd en las 2 cad (54)\n\n" +
                        "V5: 2 cad. (mpa, aum, mpa) x 2; 16 mpa; (mpa, aum, mpa) x 2; 2 mpa, cambiando a naranja en la última lazada del segundo mpa. Con naranja: 1 aum; 2 mpa; 1 aum; 8 mpa, volviendo a blanco en la última lazada del octavo mpa. Con blanco: 6 mpa; (mpa, aum, mpa) x 2. Cerrar con 1 pd en las 2 cad (62)\n\n" +
                        "V6: 2 cad. (3 mpa, aum) x 2; 14 mpa; (3 mpa, aum) x 2; 5 mpa. En este paso el cambio de color se hace un poco distinto (ver nota). 1 mpa; 1 aum; (3 mpa, aum); 9 mpa, cambiando de color en la última lazada del noveno mpa; 2 mpa; (3 mpa, aum) x 2; 2 mpa en los 2 últimos puntos, y en la última lazada cambiar de color para cerrar con 1 pd en las 2 cad (70)\n\n" +
                        "Terminar con 1 cad, dejar una hebra corta (del largo de tres dedos), cortar y tejer la hebra por detrás con una aguja.\n\n" +
                        "OREJAS (x2)\nMarcar con un marcador de puntos el punto de la primera oreja (en un aumento del borde). Marcar la segunda oreja detrás de la primera.\nOreja: insertar el ganchillo en el punto marcado, pasar el hilo por el punto y hacer 1 cad. En el punto siguiente: 1 mpa y 1 pa. 1 pa en cada uno de los 2 puntos siguientes. 3 cad y 1 pd en el punto siguiente. 1 cad, dejar una hebra corta y tejerla por detrás. Repetir para la segunda oreja.\n\n" +
                        "DETALLES\nCortar los detalles de la cara en fieltro (ojos y nariz, como en la foto) y pegarlos con pegamento. Los bigotes van bordados. Agregar un toque de rubor.");

        insertarPatron(db, "Gato con pez (llavero)", "Principiante", 24, "gato_pez",
                "V = vuelta\nAM = anillo mágico\npb = punto bajo\naum = aumento (2 pb en el mismo punto)\ndism = disminución\npd = punto deslizado\n( ) x 6 = repetir lo que va entre paréntesis 6 veces\n(12) = total de puntos al terminar la vuelta",
                "Colores: gris o café para el gato, blanco y negro para los ojos, y café dorado para el pez. Se tejen dos piezas por separado: la cabeza del gato y el pez.\n\n" +
                        "CABEZA DEL GATO\nV1: 6 pb en AM (6)\nV2: 6 aum (12)\nV3: (pb, aum) x 6 (18)\nV4: (2 pb, aum) x 6 (24)\nV5: (3 pb, aum) x 6 (30)\nV6 - V10: 30 pb (5 vueltas) (30)\nV11: (3 pb, dism) x 6 (24)\nV12: (2 pb, dism) x 6 (18)\nV13: (pb, dism) x 6 (12)\nRellenar.\nV14: 6 dism (6)\nCerrar y esconder la hebra.\n\n" +
                        "PEZ\nV1: 6 pb en AM (6)\nV2: 6 aum (12)\nV3: (pb, aum) x 6 (18)\nV4: (4 pb, dism) x 3 (15)\nV5: (3 pb, dism) x 3 (12)\nV6 - V9: 12 pb (4 vueltas) (12)\nV10: (2 pb, dism) x 3 (9)\nRellenar.\n\n" +
                        "COLA DEL PEZ\n(3 aum en el mismo punto, 2 pd) x 2. Terminar con 1 pd.\n\n" +
                        "DETALLES (según la foto)\nOjos grandes blancos con un punto negro en el centro y bigotes bordados en la cabeza del gato. Un lazo con argolla en la parte de arriba para colgarlo, y el pez colgado con una cadenita.");
    }

    private void insertarPatron(SQLiteDatabase db, String nombre, String dificultad,
                                int totalVueltas, String imagen,
                                String abreviaciones, String instrucciones) {
        ContentValues values = new ContentValues();
        values.put(PatronEntry.COLUMN_NOMBRE, nombre);
        values.put(PatronEntry.COLUMN_DIFICULTAD, dificultad);
        values.put(PatronEntry.COLUMN_TOTAL_VUELTAS, totalVueltas);
        values.put(PatronEntry.COLUMN_IMAGEN, imagen);
        values.put(PatronEntry.COLUMN_ABREVIACIONES, abreviaciones);
        values.put(PatronEntry.COLUMN_INSTRUCCIONES, instrucciones);
        db.insert(PatronEntry.TABLE_NAME, null, values);
    }

    // ---------- Usuarios (registro y login) ----------

    // Retorna el ID del usuario nuevo, o -1 si falla (por ejemplo, correo ya registrado)
    public long insertarUsuario(String nombre, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UsuarioEntry.COLUMN_NOMBRE, nombre);
        values.put(UsuarioEntry.COLUMN_EMAIL, email);
        values.put(UsuarioEntry.COLUMN_PASSWORD, password);
        long id = db.insert(UsuarioEntry.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // Retorna el ID del usuario si el correo y la contraseña coinciden, o -1 si no
    public long validarLogin(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { UsuarioEntry._ID };
        String selection = UsuarioEntry.COLUMN_EMAIL + " = ? AND " +
                UsuarioEntry.COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = { email, password };

        long idUsuario = -1;
        Cursor cursor = null;
        try {
            cursor = db.query(UsuarioEntry.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                idUsuario = cursor.getLong(cursor.getColumnIndexOrThrow(UsuarioEntry._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return idUsuario;
    }

    // ---------- Patrones ----------

    public List<Patron> obtenerPatrones() {
        List<Patron> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(PatronEntry.TABLE_NAME, null, null, null, null, null,
                    PatronEntry._ID + " ASC");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(PatronEntry._ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(PatronEntry.COLUMN_NOMBRE));
                String dificultad = cursor.getString(cursor.getColumnIndexOrThrow(PatronEntry.COLUMN_DIFICULTAD));
                int vueltas = cursor.getInt(cursor.getColumnIndexOrThrow(PatronEntry.COLUMN_TOTAL_VUELTAS));
                String nombreImagen = cursor.getString(cursor.getColumnIndexOrThrow(PatronEntry.COLUMN_IMAGEN));
                String abreviaciones = cursor.getString(cursor.getColumnIndexOrThrow(PatronEntry.COLUMN_ABREVIACIONES));
                String instrucciones = cursor.getString(cursor.getColumnIndexOrThrow(PatronEntry.COLUMN_INSTRUCCIONES));

                int imagenResId = appContext.getResources().getIdentifier(
                        nombreImagen, "drawable", appContext.getPackageName());

                lista.add(new Patron(id, nombre, dificultad, vueltas,
                        imagenResId, abreviaciones, instrucciones));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return lista;
    }

    // ---------- Progreso ----------

    // Guarda la vuelta actual de un usuario en un patrón (crea el registro o lo actualiza)
    public void guardarProgreso(long usuarioId, int patronId, int vueltaActual) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(ProgresoEntry.COLUMN_VUELTA_ACTUAL, vueltaActual);
            values.put(ProgresoEntry.COLUMN_ULTIMA_ACTUALIZACION, fechaActual());

            String where = ProgresoEntry.COLUMN_USUARIO_ID + " = ? AND " +
                    ProgresoEntry.COLUMN_PATRON_ID + " = ?";
            String[] args = { String.valueOf(usuarioId), String.valueOf(patronId) };

            // Primero intenta actualizar; si no existía el registro, lo inserta
            int filas = db.update(ProgresoEntry.TABLE_NAME, values, where, args);
            if (filas == 0) {
                values.put(ProgresoEntry.COLUMN_USUARIO_ID, usuarioId);
                values.put(ProgresoEntry.COLUMN_PATRON_ID, patronId);
                db.insert(ProgresoEntry.TABLE_NAME, null, values);
            }
        } finally {
            db.close();
        }
    }

    // Devuelve la vuelta guardada de un usuario en un patrón (0 si nunca empezó)
    public int obtenerProgreso(long usuarioId, int patronId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = { ProgresoEntry.COLUMN_VUELTA_ACTUAL };
        String selection = ProgresoEntry.COLUMN_USUARIO_ID + " = ? AND " +
                ProgresoEntry.COLUMN_PATRON_ID + " = ?";
        String[] selectionArgs = { String.valueOf(usuarioId), String.valueOf(patronId) };

        int vuelta = 0;
        Cursor cursor = null;
        try {
            cursor = db.query(ProgresoEntry.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            if (cursor.moveToFirst()) {
                vuelta = cursor.getInt(
                        cursor.getColumnIndexOrThrow(ProgresoEntry.COLUMN_VUELTA_ACTUAL));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return vuelta;
    }

    private String fechaActual() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    // Devuelve un mapa patronId -> vuelta actual, solo de los patrones que el usuario ya empezo
    public Map<Integer, Integer> obtenerProgresoUsuario(long usuarioId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                ProgresoEntry.COLUMN_PATRON_ID,
                ProgresoEntry.COLUMN_VUELTA_ACTUAL
        };
        String selection = ProgresoEntry.COLUMN_USUARIO_ID + " = ? AND " +
                ProgresoEntry.COLUMN_VUELTA_ACTUAL + " > 0";
        String[] selectionArgs = { String.valueOf(usuarioId) };

        Map<Integer, Integer> progresos = new HashMap<>();
        Cursor cursor = null;
        try {
            cursor = db.query(ProgresoEntry.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                int patronId = cursor.getInt(
                        cursor.getColumnIndexOrThrow(ProgresoEntry.COLUMN_PATRON_ID));
                int vuelta = cursor.getInt(
                        cursor.getColumnIndexOrThrow(ProgresoEntry.COLUMN_VUELTA_ACTUAL));
                progresos.put(patronId, vuelta);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return progresos;
    }

    // Elimina el progreso de un usuario en un patran (devuelve cuántas filas borro)
    public int eliminarProgreso(long usuarioId, int patronId) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            String where = ProgresoEntry.COLUMN_USUARIO_ID + " = ? AND " +
                    ProgresoEntry.COLUMN_PATRON_ID + " = ?";
            String[] args = { String.valueOf(usuarioId), String.valueOf(patronId) };
            return db.delete(ProgresoEntry.TABLE_NAME, where, args);
        } finally {
            db.close();
        }
    }

    // ---------- Tutoriales ----------

    private void precargarTutoriales(SQLiteDatabase db) {
        insertarTutorial(db, "Anillo mágico",
                "Para empezar piezas redondas sin agujero.", "anillo_magico");
        insertarTutorial(db, "Punto deslizado",
                "Sirve para unir y cerrar vueltas.", "punto_deslizado");
        insertarTutorial(db, "Punto bajo",
                "El punto más usado en amigurumi.", "punto_bajo");
        insertarTutorial(db, "Punto medio alto",
                "Un punto un poco más alto que el punto bajo.", "punto_medio_alto");
        insertarTutorial(db, "Punto alto",
                "Para piezas más altas y flexibles.", "punto_alto");
        insertarTutorial(db, "Aumento",
                "Cómo aumentar puntos en una vuelta.", "aumento");
        insertarTutorial(db, "Disminución",
                "Cómo disminuir puntos para cerrar la pieza.", "disminucion");
    }

    private void insertarTutorial(SQLiteDatabase db, String titulo, String descripcion, String video) {
        ContentValues values = new ContentValues();
        values.put(TutorialEntry.COLUMN_TITULO, titulo);
        values.put(TutorialEntry.COLUMN_DESCRIPCION, descripcion);
        values.put(TutorialEntry.COLUMN_VIDEO, video);
        db.insert(TutorialEntry.TABLE_NAME, null, values);
    }

    public List<Tutorial> obtenerTutoriales() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TutorialEntry._ID,
                TutorialEntry.COLUMN_TITULO,
                TutorialEntry.COLUMN_DESCRIPCION,
                TutorialEntry.COLUMN_VIDEO
        };

        List<Tutorial> lista = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(TutorialEntry.TABLE_NAME, projection, null, null,
                    null, null, TutorialEntry._ID + " ASC");
            while (cursor.moveToNext()) {
                lista.add(new Tutorial(
                        cursor.getInt(cursor.getColumnIndexOrThrow(TutorialEntry._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TutorialEntry.COLUMN_TITULO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TutorialEntry.COLUMN_DESCRIPCION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TutorialEntry.COLUMN_VIDEO))));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return lista;
    }

    // ---------- Tiendas ----------

    private void precargarTiendas(SQLiteDatabase db) {
        insertarTienda(db, "Tienda 1", -20.215008808748415, -70.14918328503103);
        insertarTienda(db, "Paquetería Violeta", -20.216533870874848, -70.1485808559228);
        insertarTienda(db, "Casa Lala", -20.216664753105317, -70.14864522893663);
        insertarTienda(db, "Tienda 2", -20.216745295961662, -70.14848429640205);
        insertarTienda(db, "Casa Bianca", -20.2181950602457, -70.14804441414086);
        insertarTienda(db, "Feria Persa", -20.217500383212034, -70.14811951599032);
        insertarTienda(db, "Müna Paquetería", -20.219916637785715, -70.14880616147121);
    }

    private void insertarTienda(SQLiteDatabase db, String nombre, double latitud, double longitud) {
        ContentValues values = new ContentValues();
        values.put(TiendaEntry.COLUMN_NOMBRE, nombre);
        values.put(TiendaEntry.COLUMN_LATITUD, latitud);
        values.put(TiendaEntry.COLUMN_LONGITUD, longitud);
        db.insert(TiendaEntry.TABLE_NAME, null, values);
    }

    // Guarda una tienda nueva (devuelve el id, o -1 si falló)
    public long insertarTienda(String nombre, double latitud, double longitud) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TiendaEntry.COLUMN_NOMBRE, nombre);
            values.put(TiendaEntry.COLUMN_LATITUD, latitud);
            values.put(TiendaEntry.COLUMN_LONGITUD, longitud);
            return db.insert(TiendaEntry.TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    public List<Tienda> obtenerTiendas() {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                TiendaEntry._ID,
                TiendaEntry.COLUMN_NOMBRE,
                TiendaEntry.COLUMN_LATITUD,
                TiendaEntry.COLUMN_LONGITUD
        };

        List<Tienda> lista = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(TiendaEntry.TABLE_NAME, projection, null, null,
                    null, null, TiendaEntry.COLUMN_NOMBRE + " ASC");
            while (cursor.moveToNext()) {
                lista.add(new Tienda(
                        cursor.getInt(cursor.getColumnIndexOrThrow(TiendaEntry._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(TiendaEntry.COLUMN_NOMBRE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(TiendaEntry.COLUMN_LATITUD)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(TiendaEntry.COLUMN_LONGITUD))));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return lista;
    }

    // Elimina una tienda (devuelve cuántas filas borró)
    public int eliminarTienda(int id) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            return db.delete(TiendaEntry.TABLE_NAME,
                    TiendaEntry._ID + " = ?",
                    new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }
}