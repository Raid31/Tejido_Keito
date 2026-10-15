package com.example.tejido_keito.models;

public class Tutorial {

    private final int id;
    private final String titulo;
    private final String descripcion;
    private final String video;   // nombre del archivo en res/raw (sin extensión)

    public Tutorial(int id, String titulo, String descripcion, String video) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.video = video;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getVideo() { return video; }
}