package com.ecoembes.dominio;

import java.time.LocalDate;

public class RegistroEstadoContenedor {

    private LocalDate fecha;
    private int envases;
    private NivelLlenado nivelLlenado;

    public RegistroEstadoContenedor(LocalDate fecha, int envases, NivelLlenado nivelLlenado) {
        this.fecha = fecha;
        this.envases = envases;
        this.nivelLlenado = nivelLlenado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getEnvases() {
        return envases;
    }

    public NivelLlenado getNivelLlenado() {
        return nivelLlenado;
    }
}
