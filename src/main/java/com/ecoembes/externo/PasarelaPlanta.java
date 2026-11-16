package com.ecoembes.externo;

import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import com.ecoembes.repositorio.RepositorioPlantas;

import java.time.LocalDate;

public interface PasarelaPlanta {

    String obtenerIdPlanta();

    String obtenerNombrePlanta();

    RepositorioPlantas obtenerRepositorioPlantas();

    default PlantaReciclaje obtenerPlanta() {
        String idPlanta = obtenerIdPlanta();
        return obtenerRepositorioPlantas().buscarPorId(idPlanta)
                .orElseThrow(() -> new ExcepcionNoEncontrado("Planta no encontrada: " + idPlanta));
    }

    default double consultarCapacidad(LocalDate fecha) {
        return obtenerPlanta().consultarCapacidadDisponible(fecha);
    }

    default void reducirCapacidad(LocalDate fecha, double toneladas) {
        obtenerPlanta().reducirCapacidad(fecha, toneladas);
    }

    void notificarAsignacion(LocalDate fecha, int totalContenedores, double toneladasEstimadas);
}
