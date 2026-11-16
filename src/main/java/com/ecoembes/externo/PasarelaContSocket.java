package com.ecoembes.externo;

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
    public RepositorioPlantas obtenerRepositorioPlantas() {
        return repositorioPlantas;
    }

    @Override
    public double consultarCapacidad(LocalDate fecha) {
        return PasarelaPlanta.super.consultarCapacidad(fecha);
    }

    @Override
    public void reducirCapacidad(LocalDate fecha, double toneladas) {
        PasarelaPlanta.super.reducirCapacidad(fecha, toneladas);
    }

    @Override
    public void notificarAsignacion(LocalDate fecha, int totalContenedores, double toneladasEstimadas) {
        log.info("[ContSocket Ltd.] Notificación recibida para {}: {} contenedores ({} t)",
                fecha, totalContenedores, toneladasEstimadas);
    }
}
