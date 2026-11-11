package com.ecoembes.externo;

import com.ecoembes.dominio.CapacidadDiariaPlanta;
import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import com.ecoembes.excepcion.ExcepcionPeticionInvalida;
import com.ecoembes.repositorio.RepositorioPlantas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PasarelaContSocket implements PasarelaPlanta {

    private static final Logger log = LoggerFactory.getLogger(PasarelaContSocket.class);
    public static final String ID_PLANTA = "CONTSOCKET";
    public static final String NOMBRE_PLANTA = "ContSocket Ltd.";

    private final RepositorioPlantas repositorioPlantas;

    public PasarelaContSocket(RepositorioPlantas repositorioPlantas) {
        this.repositorioPlantas = repositorioPlantas;
    }

    @Override
    public String obtenerIdPlanta() {
        return ID_PLANTA;
    }

    @Override
    public String obtenerNombrePlanta() {
        return NOMBRE_PLANTA;
    }

    @Override
    public double consultarCapacidad(LocalDate fecha) {
        PlantaReciclaje planta = repositorioPlantas.buscarPorId(ID_PLANTA)
                .orElseThrow(() -> new ExcepcionNoEncontrado("Planta no encontrada: " + ID_PLANTA));
        return planta.obtenerCapacidadEnFecha(fecha)
                .map(CapacidadDiariaPlanta::getToneladasDisponibles)
                .orElseThrow(() -> new ExcepcionPeticionInvalida(
                        "La planta " + NOMBRE_PLANTA + " no tiene capacidad registrada para la fecha " + fecha));
    }

    @Override
    public void reducirCapacidad(LocalDate fecha, double toneladas) {
        PlantaReciclaje planta = repositorioPlantas.buscarPorId(ID_PLANTA)
                .orElseThrow(() -> new ExcepcionNoEncontrado("Planta no encontrada: " + ID_PLANTA));
        CapacidadDiariaPlanta capacidad = planta.obtenerCapacidadEnFecha(fecha)
                .orElseThrow(() -> new ExcepcionPeticionInvalida(
                        "La planta " + NOMBRE_PLANTA + " no tiene capacidad registrada para la fecha " + fecha));
        capacidad.reducirToneladasDisponibles(toneladas);
    }

    @Override
    public void notificarAsignacion(LocalDate fecha, int totalContenedores, int totalEnvases, double toneladasEstimadas) {
        log.info("[ContSocket Ltd.] Notificación recibida para {}: {} contenedores, {} envases ({} t)",
                fecha, totalContenedores, totalEnvases, toneladasEstimadas);
    }
}
