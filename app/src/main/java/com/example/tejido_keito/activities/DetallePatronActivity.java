package com.example.tejido_keito.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tejido_keito.R;

import java.io.IOException;

public class DetallePatronActivity extends AppCompatActivity {

    // URLs de prueba (audio y video de ejemplo desde internet)
    private static final String URL_AUDIO = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
    private static final String URL_VIDEO = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4";
    private static final int CODIGO_PERMISO_MICROFONO = 100;

    private MediaPlayer mediaPlayer;
    private boolean audioPreparado = false;

    private Button btnAudio;

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
        btnAudio = findViewById(R.id.btnAudio);
        Button btnVideo = findViewById(R.id.btnVideo);
        VideoView vvTutorial = findViewById(R.id.vvTutorial);

        // ----- Datos del patrón -----
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String dificultad = intent.getStringExtra("dificultad");
        int totalVueltas = intent.getIntExtra("totalVueltas", 0);
        int imagenResId = intent.getIntExtra("imagenResId", R.drawable.placeholder_patron);
        String abreviaciones = intent.getStringExtra("abreviaciones");
        String instrucciones = intent.getStringExtra("instrucciones");

        // ----- 1. Imagen -----
        ivDetalle.setImageResource(imagenResId);
        tvNombreDetalle.setText(nombre);
        tvDificultadDetalle.setText("Dificultad: " + dificultad);
        tvVueltasDetalle.setText("Total de vueltas: " + totalVueltas);
        tvAbreviaciones.setText(abreviaciones);
        tvInstrucciones.setText(instrucciones);

        // ----- 3. Animación: la imagen aparece suavemente al abrir la pantalla -----
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivDetalle.startAnimation(fadeIn);

        // ----- 2. Audio desde internet -----
        btnAudio.setOnClickListener(v -> reproducirOPausarAudio());

        // ----- 2. Video desde internet (+ animación al presionar el botón) -----
        MediaController controles = new MediaController(this);
        controles.setAnchorView(vvTutorial);
        vvTutorial.setMediaController(controles);

        btnVideo.setOnClickListener(v -> {
            // 3. Animación al presionar el botón (se anima el botón, no el VideoView)
            btnVideo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

            Toast.makeText(this, "Cargando video...", Toast.LENGTH_SHORT).show();

            // Primero los listeners, después la URL
            vvTutorial.setOnPreparedListener(mp -> {
                Toast.makeText(this, "Reproduciendo video", Toast.LENGTH_SHORT).show();
                vvTutorial.start();
            });
            vvTutorial.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "No se pudo cargar el video. Revisa tu conexión.", Toast.LENGTH_LONG).show();
                return true;
            });

            vvTutorial.setVideoURI(Uri.parse(URL_VIDEO));
            vvTutorial.requestFocus();
        });

        // ----- 4. Permiso de micrófono en tiempo de ejecución -----
        btnEmpezarTejer.setOnClickListener(v -> pedirPermisoMicrofono());
    }

    private void reproducirOPausarAudio() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            try {
                mediaPlayer.setDataSource(URL_AUDIO);
                mediaPlayer.setOnPreparedListener(mp -> {
                    audioPreparado = true;
                    mp.start();
                    btnAudio.setText("Pausar audio");
                });
                mediaPlayer.setOnCompletionListener(mp -> btnAudio.setText("Escuchar audio"));
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(this, "No se pudo cargar el audio. Revisa tu conexión.", Toast.LENGTH_LONG).show();
                    liberarAudio();
                    return true;
                });
                Toast.makeText(this, "Cargando audio...", Toast.LENGTH_SHORT).show();
                mediaPlayer.prepareAsync(); // carga en segundo plano para no congelar la app
            } catch (IOException e) {
                Toast.makeText(this, "Error al cargar el audio", Toast.LENGTH_SHORT).show();
                liberarAudio();
            }
        } else if (audioPreparado) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnAudio.setText("Escuchar audio");
            } else {
                mediaPlayer.start();
                btnAudio.setText("Pausar audio");
            }
        }
    }

    private void pedirPermisoMicrofono() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Micrófono listo: ¡a tejer!", Toast.LENGTH_SHORT).show();
            // Aquí más adelante abriremos el Contador de vueltas por voz
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    CODIGO_PERMISO_MICROFONO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_MICROFONO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido: ya puedes usar el contador por voz", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sin el micrófono no se puede usar el contador por voz", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void liberarAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            audioPreparado = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        liberarAudio(); // al salir de la pantalla se detiene el audio
    }
}