package com.ecoembes.excepcion;

public class ExcepcionNoEncontrado extends RuntimeException {
    public ExcepcionNoEncontrado(String mensaje) {
        super(mensaje);
    }
}
