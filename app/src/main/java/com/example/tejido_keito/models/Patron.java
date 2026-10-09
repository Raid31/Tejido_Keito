package com.example.tejido_keito.models;

public class Patron {

    private int id;
    private String nombre;
    private String dificultad;
    private int totalVueltas;
    private int imagenResId;
    private String abreviaciones;
    private String instrucciones;

    public Patron(int id, String nombre, String dificultad, int totalVueltas,
                  int imagenResId, String abreviaciones, String instrucciones) {
        this.id = id;
        this.nombre = nombre;
        this.dificultad = dificultad;
        this.totalVueltas = totalVueltas;
        this.imagenResId = imagenResId;
        this.abreviaciones = abreviaciones;
        this.instrucciones = instrucciones;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDificultad() { return dificultad; }
    public int getTotalVueltas() { return totalVueltas; }
    public int getImagenResId() { return imagenResId; }
    public String getAbreviaciones() { return abreviaciones; }
    public String getInstrucciones() { return instrucciones; }
}