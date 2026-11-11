package com.ecoembes.dominio;

public class PersonalEcoembes {

    private String id;
    private String nombre;
    private String email;
    private String contrasena;

    public PersonalEcoembes(String id, String nombre, String email, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getContrasena() {
        return contrasena;
    }
}
