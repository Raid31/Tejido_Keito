package com.example.tejido_keito.models;

/**
 * Modelo de un patrón de tejido. Es lo que devuelve KeitoDbHelper.obtenerPatrones().
 * La imagen se guarda como id de recurso (imagenResId) y no como nombre.
 */
public class Patron {

    private int id;
    private String nombre;
    private String dificultad;
    private int totalVueltas;
    private int imagenResId;
    private String abreviaciones;
    private String instrucciones;

    /**
     * Crea un patrón
     *
     * @param id                id en la base de datos
     * @param nombre            nombre del patrón
     * @param dificultad        Principiante, Intermedio o Avanzado
     * @param totalVueltas      cantidad de vueltas del patrón
     * @param imagenResId       id del recurso de la imagen en drawable
     * @param abreviaciones     significado de las abreviaciones
     * @param instrucciones     paso a paso del patrón
     */
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