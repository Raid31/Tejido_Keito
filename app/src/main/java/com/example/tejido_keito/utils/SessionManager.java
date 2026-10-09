package com.example.tejido_keito.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "keito_sesion";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_CORREO = "correo";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Se llama cuando el login es correcto
    public void guardarSesion(long usuarioId, String correo) {
        prefs.edit()
                .putLong(KEY_USUARIO_ID, usuarioId)
                .putString(KEY_CORREO, correo)
                .apply();
    }

    // true si hay una sesión abierta
    public boolean estaLogueado() {
        return prefs.getLong(KEY_USUARIO_ID, -1) != -1;
    }

    public long getUsuarioId() {
        return prefs.getLong(KEY_USUARIO_ID, -1);
    }

    public String getCorreo() {
        return prefs.getString(KEY_CORREO, "");
    }

    // Se llama al cerrar sesión
    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}
