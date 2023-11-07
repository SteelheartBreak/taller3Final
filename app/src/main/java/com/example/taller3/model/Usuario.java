package com.example.taller3.model;


public class Usuario {

    private String usuario;
    private String urlImagen;
    private String objectID;

    public Usuario(String usuario, String urlImagen, String objectID) {
        this.usuario = usuario;
        this.urlImagen = urlImagen;
        this.objectID = objectID;

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

    public String getObjectID() {
        return objectID;
    }

    public void setObjectId(String objectId) {
        this.objectID = objectId;
    }

    private long userID;

}
