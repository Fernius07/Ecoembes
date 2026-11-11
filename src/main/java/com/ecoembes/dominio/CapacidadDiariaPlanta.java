package com.ecoembes.dominio;

import java.time.LocalDate;

public class CapacidadDiariaPlanta {

    private LocalDate fecha;
    private double toneladasMaximas;
    private double toneladasDisponibles;

    public CapacidadDiariaPlanta(LocalDate fecha, double toneladasMaximas, double toneladasDisponibles) {
        this.fecha = fecha;
        this.toneladasMaximas = toneladasMaximas;
        this.toneladasDisponibles = toneladasDisponibles;
    }

    public synchronized void reducirToneladasDisponibles(double toneladas) {
        double actualizado = Math.round((this.toneladasDisponibles - toneladas) * 100000.0) / 100000.0;
        this.toneladasDisponibles = Math.max(0.0, actualizado);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getToneladasMaximas() {
        return toneladasMaximas;
    }

    public double getToneladasDisponibles() {
        return toneladasDisponibles;
    }
}
