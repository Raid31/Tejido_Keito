package com.example.tejido_keito.models;

public class Tienda {
    private final int id;
    private final String nombre;
    private final double latitud;
    private final double longitud;

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
