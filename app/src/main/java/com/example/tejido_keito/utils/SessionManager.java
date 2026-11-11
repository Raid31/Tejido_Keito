package com.example.tejido_keito.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda la sesión del usuario en el teléfono (SharedPreferences) para que siga abierta aunque se cierre la app.
 * Guarda el id y el correo del usuario que inicio sesión.
 *
 */

public class SessionManager {

    private static final String PREFS_NAME = "keito_sesion";
    private static final String KEY_USUARIO_ID = "usuario_id";
    private static final String KEY_CORREO = "correo";

    private final SharedPreferences prefs;

    /**
     * abre el archivo de preferencas donde se guarda la sesion
     *
     * @param context contexto de la activity que lo usa
     */
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * guarda la sesion cuando el login es correcto
     *
     * @param usuarioId     id del usuario en la base de datos
     * @param correo        correo con el qie se incio sesion
     */
    public void guardarSesion(long usuarioId, String correo) {
        prefs.edit()
                .putLong(KEY_USUARIO_ID, usuarioId)
                .putString(KEY_CORREO, correo)
                .apply();
    }

    /**
     * revisa si hay una sesion abierta
     *
     * @return true si hay una sesion guardada
     */
    public boolean estaLogueado() {
        return prefs.getLong(KEY_USUARIO_ID, -1) != -1;
    }

    /**
     * devuelve el id del usuario con la sesion abierta
     *
     * @return id del usuario o -1 si no hay sesion
     */
    public long getUsuarioId() {
        return prefs.getLong(KEY_USUARIO_ID, -1);
    }

    /**
     * devuelve el correo del ususrio con la sesion abierta
     *
     * @return correo guardado, o texto vacío si no hay sesión
     */
    public String getCorreo() {
        return prefs.getString(KEY_CORREO, "");
    }

    /**
     * borra la sesion guardada
     */
    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}
