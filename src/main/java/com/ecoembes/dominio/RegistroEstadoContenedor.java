package com.ecoembes.dominio;

import java.time.LocalDate;

public class RegistroEstadoContenedor {

    private LocalDate fecha;
    private double toneladas;
    private NivelLlenado nivelLlenado;

    public RegistroEstadoContenedor(LocalDate fecha, double toneladas, NivelLlenado nivelLlenado) {
        this.fecha = fecha;
        this.toneladas = toneladas;
        this.nivelLlenado = nivelLlenado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getToneladas() {
        return toneladas;
    }

    public NivelLlenado getNivelLlenado() {
        return nivelLlenado;
    }
}
