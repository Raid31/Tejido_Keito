package com.example.tejido_keito.models;

public class Patron {

    private String nombre;
    private String dificultad;
    private int totalVueltas;

    public Patron(String nombre, String dificultad, int totalVueltas){
        this.nombre = nombre;
        this.dificultad = dificultad;
        this.totalVueltas= totalVueltas;
    }

    public int getTotalVueltas() {
        return totalVueltas;
    }

    public void setTotalVueltas(int totalVueltas) {
        this.totalVueltas = totalVueltas;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
