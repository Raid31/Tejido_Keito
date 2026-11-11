package com.example.tejido_keito.models;

/**
 * Modelo de una tienda de lanas con su ubicación. Es lo que devuelve KeitoDbHelper.obtenerTiendas().
 */
public class Tienda {
    private final int id;
    private final String nombre;
    private final double latitud;
    private final double longitud;

    /**
     * Crea una tienda
     *
     * @param id        id en la base de datos
     * @param nombre    nombre de la tienda
     * @param latitud   latitud GPS
     * @param longitud  longitud GPS
     */
    public Tienda(int id, String nombre, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getId() {return id; }
    public String getNombre() {return nombre; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }

}
