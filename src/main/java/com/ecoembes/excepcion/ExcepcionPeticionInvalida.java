package com.ecoembes.excepcion;

public class ExcepcionPeticionInvalida extends RuntimeException {
    public ExcepcionPeticionInvalida(String mensaje) {
        super(mensaje);
    }
}
