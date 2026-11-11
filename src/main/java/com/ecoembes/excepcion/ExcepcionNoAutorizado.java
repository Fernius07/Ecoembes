package com.ecoembes.excepcion;

public class ExcepcionNoAutorizado extends RuntimeException {
    public ExcepcionNoAutorizado(String mensaje) {
        super(mensaje);
    }
}
