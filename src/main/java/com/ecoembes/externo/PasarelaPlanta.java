package com.ecoembes.externo;

import java.time.LocalDate;

public interface PasarelaPlanta {

    String obtenerIdPlanta();

    String obtenerNombrePlanta();

    double consultarCapacidad(LocalDate fecha);

    void reducirCapacidad(LocalDate fecha, double toneladas);

    void notificarAsignacion(LocalDate fecha, int totalContenedores, int totalEnvases, double toneladasEstimadas);
}
