package com.example.tejido_keito.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.HapticFeedbackConstants;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.tejido_keito.R;
import com.example.tejido_keito.database.KeitoDbHelper;
import com.example.tejido_keito.utils.FormatoTexto;
import com.example.tejido_keito.utils.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * El contado de vueltas de un patron. Se maneja con botones o por voz(ej: siguiente/atras) y guarda
 * el progreso del usuario en SQLite
 * <p>
 *     recibe por intent : id, nombre, totalVueltas, abreviaciones e instrucciones
 * </p>
 */
public class ContadorActivity extends AppCompatActivity {

    // Palabras que el contador entiende
    private static final List<String> PALABRAS_SUMAR = Arrays.asList(
            "siguiente", "listo", "lista", "sumar", "suma", "más", "mas", "uno", "ya");
    private static final List<String> PALABRAS_RESTAR = Arrays.asList(
            "atrás", "atras", "anterior", "menos", "restar", "resta", "deshacer", "borrar");

    private static final int[] STREAMS_PITIDO = {
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_NOTIFICATION
    };
    private final boolean[] silenciadoPorApp = new boolean[STREAMS_PITIDO.length];

    private int vueltaActual = 0;
    private int totalVueltas;

    // Para guardar el progreso en SQLite
    private KeitoDbHelper dbHelper;
    private int patronId;
    private long usuarioId;
    // Hilo secundario para las consultas a la base de datos
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    private TextView tvVueltaActual;
    private TextView tvDeTotal;
    private TextView tvEstadoVoz;
    private ProgressBar progressVueltas;
    private Button btnVoz;

    private SpeechRecognizer recognizer;
    private boolean escuchando = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Pide el permiso del microfono y si lo dan, comienza a escuchar
     */
    private final ActivityResultLauncher<String> permisoMicrofono =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    iniciarEscucha();
                } else {
                    Toast.makeText(this,
                            "Sin permiso de micrófono no puedo escuchar. Puedes usar los botones.",
                            Toast.LENGTH_LONG).show();
                }
            });

    /**
     * Lee los extras del patron, recupera la vuelta y deja listo los botones
     *
     * @param savedInstanceState estado anterior de la pantalla, si lo hay (por ejemplo al girar el teléfono)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contador);

        // Mantener la pantalla encendida mientras tejes
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView tvNombreContador = findViewById(R.id.tvNombreContador);
        TextView tvInstruccionesContador = findViewById(R.id.tvInstruccionesContador);
        tvVueltaActual = findViewById(R.id.tvVueltaActual);
        tvDeTotal = findViewById(R.id.tvDeTotal);
        tvEstadoVoz = findViewById(R.id.tvEstadoVoz);
        progressVueltas = findViewById(R.id.progressVueltas);
        btnVoz = findViewById(R.id.btnVoz);
        Button btnMas = findViewById(R.id.btnMas);
        Button btnMenos = findViewById(R.id.btnMenos);
        Button btnReiniciar = findViewById(R.id.btnReiniciar);
        Button btnAbreviaciones = findViewById(R.id.btnAbreviaciones);

        // Datos que llegan desde el detalle del patrón
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String abreviaciones = intent.getStringExtra("abreviaciones");
        String instrucciones = intent.getStringExtra("instrucciones");
        patronId = intent.getIntExtra("id", -1);
        totalVueltas = intent.getIntExtra("totalVueltas", 1);
        if (totalVueltas <= 0) {
            totalVueltas = 1;
        }

        // Usuario con la sesión abierta
        dbHelper = new KeitoDbHelper(this);
        usuarioId = new SessionManager(this).getUsuarioId();

        tvNombreContador.setText(nombre);
        tvInstruccionesContador.setText(FormatoTexto.instrucciones(this, instrucciones));
        progressVueltas.setMax(totalVueltas);

        // Recuperar la vuelta, si se giro la pantalla, del estado; si no, de la base de datos
        if (savedInstanceState != null) {
            vueltaActual = savedInstanceState.getInt("vuelta", 0);
        } else if (puedeGuardar()) {
            cargarProgresoGuardado();
        }
        actualizarUi();

        btnMas.setOnClickListener(v -> sumar());
        btnMenos.setOnClickListener(v -> restar());

        btnVoz.setOnClickListener(v -> {
            if (escuchando) {
                detenerEscucha();
            } else {
                pedirPermisoYEscuchar();
            }
        });

        btnAbreviaciones.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Abreviaciones")
                        .setMessage(FormatoTexto.abreviaciones(abreviaciones))
                        .setPositiveButton("Cerrar", null)
                        .show());

        btnReiniciar.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Reiniciar contador")
                        .setMessage("¿Volver a la vuelta 0?")
                        .setPositiveButton("Reiniciar", (dialog, which) -> {
                            vueltaActual = 0;
                            actualizarUi();
                            guardarProgreso();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show());
    }

    // ---------------- Contador ----------------

    /**
     * Suma una vuelta y guarda el progreso. Si llega al final de vueltas, deja de escuchar y
     * avisa que se termino el patron.
     */
    private void sumar() {
        if (vueltaActual < totalVueltas) {
            vueltaActual++;
            actualizarUi();
            guardarProgreso();
            tvVueltaActual.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

            if (vueltaActual == totalVueltas) {
                detenerEscucha();
                tvEstadoVoz.setText("¡Terminaste el patrón!");
                Toast.makeText(this, "¡Terminaste el patrón!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Resta una vuelta y guarda el progreso
     */
    private void restar() {
        if (vueltaActual > 0) {
            vueltaActual--;
            actualizarUi();
            guardarProgreso();
            tvVueltaActual.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }
    }

    /**
     * muestra en pantalla la vuelta actuaal, el total y la barra de progreso
     */
    private void actualizarUi() {
        tvVueltaActual.setText(String.valueOf(vueltaActual));
        tvDeTotal.setText("de " + totalVueltas + " vueltas");
        progressVueltas.setProgress(vueltaActual);
    }

    // ---------------- Progreso (SQLite) ----------------

    /**
     * Revisa que haya un patron y un usuario validos para guardar el progreso
     * @return  true si el id del patron y del usuario son validos
     */
    private boolean puedeGuardar() {
        return patronId != -1 && usuarioId != -1;
    }

    /**
     * Lee la vuelta guardada en un hilo secundario y la muestra al terminar.
     */
    private void cargarProgresoGuardado() {
        ejecutor.execute(() -> {
            int guardada = Math.min(dbHelper.obtenerProgreso(usuarioId, patronId), totalVueltas);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || guardada <= 0 || vueltaActual != 0) {
                    return;
                }
                vueltaActual = guardada;
                actualizarUi();
                Toast.makeText(this, "Retomando en la vuelta " + vueltaActual,
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Guarda la vuelta actual en un hilo secundario. Como el executor es de un solo hilo,
     * se respeta el orden de los guardados.
     */
    private void guardarProgreso() {
        if (puedeGuardar()) {
            final int vuelta = vueltaActual;
            ejecutor.execute(() -> dbHelper.guardarProgreso(usuarioId, patronId, vuelta));
        }
    }

    // ---------------- Pitidos ----------------

    /**
     *     Silencia los volúmenes por donde suena el pitido del reconocedor
     */
    private void silenciarPitidos() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audio == null) {
            return;
        }
        for (int i = 0; i < STREAMS_PITIDO.length; i++) {
            try {
                if (!audio.isStreamMute(STREAMS_PITIDO[i])) {
                    audio.adjustStreamVolume(STREAMS_PITIDO[i], AudioManager.ADJUST_MUTE, 0);
                    silenciadoPorApp[i] = true;
                }
            } catch (SecurityException e) {
                silenciadoPorApp[i] = false;   // el modo No molestar no lo permite
            }
        }
    }

    /**
     *     Devuelve el sonido solo en lo que la app silenció
     */
    private void restaurarSonido() {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audio == null) {
            return;
        }
        for (int i = 0; i < STREAMS_PITIDO.length; i++) {
            if (silenciadoPorApp[i]) {
                try {
                    audio.adjustStreamVolume(STREAMS_PITIDO[i], AudioManager.ADJUST_UNMUTE, 0);
                } catch (SecurityException ignored) {
                }
                silenciadoPorApp[i] = false;
            }
        }
    }

    // ---------------- Voz ----------------

    /**
     * Si ya tiene el permiso del microfono empieza a escuchar, si no, lo pide.
     */
    private void pedirPermisoYEscuchar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarEscucha();
        } else {
            permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    /**
     * Prepara el reconocedor de voz y empieza a escuchar.
     */
    private void iniciarEscucha() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this,
                    "Tu teléfono no tiene reconocimiento de voz disponible",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(listener);
        }

        escuchando = true;
        silenciarPitidos();
        btnVoz.setText("Pausar voz");
        tvEstadoVoz.setText("Escuchando... di «siguiente» o «atrás»");
        escucharUnaVez();
    }

    /**
     *  escucha un frase. el reconocedor se detiene al terminar, por eso se vuelve a llamar cada vez
     */
    private void escucharUnaVez() {
        if (!escuchando || recognizer == null) {
            return;
        }
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CL");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizer.startListening(intent);
    }

    /**
     * deja de escuchar y devuelve el sonido. cambia los textos a "voz en pausa"
     */
    private void detenerEscucha() {
        escuchando = false;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            recognizer.stopListening();
            recognizer.cancel();
        }
        restaurarSonido();
        btnVoz.setText("Activar voz");
        tvEstadoVoz.setText("Voz en pausa");
    }

    /**
     * busca una palabra de comadno dentro de lo que esucho y suna o resta una vuelta.
     *
     * @param frases    posibles frases que entendio el reconocedor
     */
    private void procesarComando(ArrayList<String> frases) {
        if (frases == null || frases.isEmpty()) {
            return;
        }

        for (String frase : frases) {
            List<String> palabras = Arrays.asList(frase.toLowerCase().trim().split("\\s+"));

            for (String palabra : palabras) {
                if (PALABRAS_RESTAR.contains(palabra)) {
                    tvEstadoVoz.setText("Escuché: «" + frases.get(0) + "»");
                    restar();
                    return;
                }
            }
            for (String palabra : palabras) {
                if (PALABRAS_SUMAR.contains(palabra)) {
                    tvEstadoVoz.setText("Escuché: «" + frases.get(0) + "»");
                    sumar();
                    return;
                }
            }
        }
        tvEstadoVoz.setText("No entendí: «" + frases.get(0) + "»");
    }

    /**
     * recive los resultados y los errores del reconocedor de voz
     */
    private final RecognitionListener listener = new RecognitionListener() {
        /**
         * Procesa lo que escucho y si sigue activa la voz vuelve a escuchar.
         *
         * @param results resultado del reconocimiento (lista de frases posibles)
         */
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> frases =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            procesarComando(frases);

            if (escuchando) {
                handler.postDelayed(ContadorActivity.this::escucharUnaVez, 300);
            }
        }

        /**
         * segun sea el error detiene la voz o vuelve a escuchar
         *
         * @param error código de error de SpeechRecognizer
         */
        @Override
        public void onError(int error) {
            if (!escuchando) {
                return;
            }

            switch (error) {
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    detenerEscucha();
                    Toast.makeText(ContadorActivity.this,
                            "Falta el permiso del micrófono", Toast.LENGTH_LONG).show();
                    break;

                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                case SpeechRecognizer.ERROR_SERVER:
                    detenerEscucha();
                    Toast.makeText(ContadorActivity.this,
                            "El reconocimiento de voz necesita internet. Revisa tu conexión.",
                            Toast.LENGTH_LONG).show();
                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    recognizer.cancel();
                    handler.postDelayed(ContadorActivity.this::escucharUnaVez, 600);
                    break;

                default:
                    // Silencio o frase no reconocida: volver a escuchar
                    handler.postDelayed(ContadorActivity.this::escucharUnaVez, 400);
                    break;
            }
        }

        @Override public void onReadyForSpeech(Bundle params) {}
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() {}
        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
    };

    // ---------------- Ciclo de vida ----------------

    /**
     * Guarda la vuelta actual para no perderla si se gira la pantalla
     *
     * @param outState Bundle donde se guarda el estado
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("vuelta", vueltaActual);
    }

    /**
     * Si la app pasa a segundo plano suelta el microfono y devuelve el sonido
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (escuchando) {
            detenerEscucha();
        }
    }

    /**
     * libera el reconocedor, devuelve el sonido, apaga el hilo secundario y cierra la base de datos
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        restaurarSonido();
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        ejecutor.shutdown();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}