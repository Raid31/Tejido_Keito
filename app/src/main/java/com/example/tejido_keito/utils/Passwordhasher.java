package com.example.tejido_keito.utils;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Se encarga de proteger las contraseñas. En la base de datos no se guarda la
 * contraseña real sino un hash (PBKDF2 con SHA-256) junto con una sal al azar,
 * así dos usuarios con la misma contraseña no quedan con el mismo valor guardado
 * y no se puede "desencriptar" para ver la contraseña original.
 * <p>
 * El texto que se guarda tiene el formato {@code sal:hash} (los dos en Base64).
 */
public final class Passwordhasher {

    private static final int ITERACIONES = 120000;
    private static final int LARGO_HASH_BITS = 256;
    private static final int LARGO_SAL_BYTES = 16;

    private Passwordhasher() {}

    /**
     * Convierte una contraseña en el texto que se guarda en la base de datos.
     * Cada vez que se llama genera una sal distinta.
     *
     * @param password contraseña que escribió el usuario
     * @return texto con formato sal:hash
     * @throws IllegalStateException si el teléfono no soporta el algoritmo
     */
    public static String hashear(String password) {
        byte[] sal = new byte[LARGO_SAL_BYTES];
        new SecureRandom().nextBytes(sal);
        byte[] hash = derivar(password, sal);
        return Base64.getEncoder().encodeToString(sal) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Revisa si una contraseña corresponde al texto guardado.
     *
     * @param password contraseña que escribió el usuario
     * @param guardado texto guardado en la base (sal:hash)
     * @return true si coincide; false si no coincide o si el texto guardado no tiene el formato esperado
     * @throws IllegalStateException si el teléfono no soporta el algoritmo
     */
    public static boolean verificar(String password, String guardado) {
        if (guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 2) {
            return false;   // por ejemplo, una contraseña antigua guardada sin hash
        }
        try {
            byte[] sal = Base64.getDecoder().decode(partes[0]);
            byte[] esperado = Base64.getDecoder().decode(partes[1]);
            byte[] calculado = derivar(password, sal);
            // Compara sin cortarse en la primera diferencia
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String password, byte[] sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), sal, ITERACIONES, LARGO_HASH_BITS);
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return fabrica.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }
}
