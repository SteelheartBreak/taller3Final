package com.example.taller3.model;


public class Usuario {

    private String usuario;
    private String urlImagen;

    public Usuario(String usuario, String urlImagen) {
        this.usuario = usuario;
        this.urlImagen = urlImagen;

    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    private long userID;

}
