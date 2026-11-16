package com.ecoembes.dominio;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nivel de llenado del contenedor: VERDE (0-60%), NARANJA (60-90%) o ROJO (90-100%)")
public enum NivelLlenado {
    VERDE(0.0, 60.0),
    NARANJA(60.0, 90.0),
    ROJO(90.0, 100.0);

    private final double porcentajeMinimo;
    private final double porcentajeMaximo;

    NivelLlenado(double porcentajeMinimo, double porcentajeMaximo) {
        this.porcentajeMinimo = porcentajeMinimo;
        this.porcentajeMaximo = porcentajeMaximo;
    }

    public double getPorcentajeMinimo() {
        return porcentajeMinimo;
    }

    public double getPorcentajeMaximo() {
        return porcentajeMaximo;
    }

    public static NivelLlenado calcularSegunPorcentaje(double porcentaje) {
        if (porcentaje < NARANJA.porcentajeMinimo) {
            return VERDE;
        } else if (porcentaje <= NARANJA.porcentajeMaximo) {
            return NARANJA;
        } else {
            return ROJO;
        }
    }
}
