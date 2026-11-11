package com.example.tejido_keito.activities;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tejido_keito.R;

/**
 * Reproduce un tutorial en video guardado en res/raw, dentro de la app.
 * Recibe por Intent: titulo, descripcion y video (nombre del archivo).
 */
public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private int posicion = 0;   // milisegundos donde quedó el video


    /**
     * Busca el video, prepara los controles y lo reproduce
     * (retomando donde iba si se giró la pantalla).
     * Si el video no existe, avisa y cierra la pantalla.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        TextView tvTitulo = findViewById(R.id.tvTituloVideo);
        TextView tvDescripcion = findViewById(R.id.tvDescripcionVideo);
        videoView = findViewById(R.id.videoView);

        // Datos que llegan desde la lista de tutoriales
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        String video = getIntent().getStringExtra("video");

        tvTitulo.setText(titulo);
        tvDescripcion.setText(descripcion);

        int recurso = getResources().getIdentifier(video, "raw", getPackageName());
        if (recurso == 0) {
            Toast.makeText(this, "No se encontró el video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Si se giró la pantalla, retomar donde iba
        if (savedInstanceState != null) {
            posicion = savedInstanceState.getInt("posicion", 0);
        }

        // Controles de reproducción (play, pausa, avance)
        MediaController controles = new MediaController(this);
        controles.setAnchorView(videoView);
        videoView.setMediaController(controles);

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + recurso));
        videoView.setOnPreparedListener(mp -> {
            // Ajustar el reproductor a la proporción real de este video
            int ancho = mp.getVideoWidth();
            int alto = mp.getVideoHeight();
            if (ancho > 0 && alto > 0) {
                ConstraintLayout.LayoutParams params =
                        (ConstraintLayout.LayoutParams) videoView.getLayoutParams();
                // Horizontal: el alto se calcula desde el ancho. Vertical: al revés.
                params.dimensionRatio = (ancho >= alto ? "H," : "W,") + ancho + ":" + alto;
                videoView.setLayoutParams(params);
            }

            if (posicion > 0) {
                videoView.seekTo(posicion);
            }
            videoView.start();
        });
    }

    /**
     *  Al minimizar la app, pausa el video y recuerda dónde iba.
     */
    @Override
    protected void onPause() {
        super.onPause();
        posicion = videoView.getCurrentPosition();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    /**
     * Guarda la posición del video para retomarlo si se gira la pantalla.
     *
     * @param outState Bundle in which to place your saved state.
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("posicion", videoView.getCurrentPosition());
    }
}