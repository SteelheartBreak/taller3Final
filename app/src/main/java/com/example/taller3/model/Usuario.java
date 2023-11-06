package com.example.taller3.model;

import com.parse.ParseObject;

public class Usuario extends ParseObject {
    private String nombre;
    private String usuario;
    private String contrasena;
    private String urlImagen;
    private String descripcion;
    private String personalizable1;
    private String personalizable2;

    // Constructor completo
    public Usuario(String nombre, String usuario, String contrasena, String urlImagen, String descripcion, String personalizable1, String personalizable2) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.urlImagen = urlImagen;
        this.descripcion = descripcion;
        this.personalizable1 = personalizable1;
        this.personalizable2 = personalizable2;
    }

    // Constructor sin urlImagen, descripcion y personalizable1/2
    public Usuario(String nombre, String usuario, String contrasena) {
        this.nombre = nombre;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }
}
