package com.ecoembes.servicio;

import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.dto.CapacidadPlantaDTO;
import com.ecoembes.dto.PlantaDTO;
import com.ecoembes.ensamblador.EnsambladorDTO;
import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import com.ecoembes.excepcion.ExcepcionPeticionInvalida;
import com.ecoembes.externo.PasarelaPlanta;
import com.ecoembes.externo.RegistroPasarelasPlanta;
import com.ecoembes.repositorio.RepositorioPlantas;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioPlantas {

    private final RepositorioPlantas repositorioPlantas;
    private final RegistroPasarelasPlanta registroPasarelas;
    private final EnsambladorDTO ensamblador;

    public ServicioPlantas(RepositorioPlantas repositorioPlantas,
                           RegistroPasarelasPlanta registroPasarelas,
                           EnsambladorDTO ensamblador) {
        this.repositorioPlantas = repositorioPlantas;
        this.registroPasarelas = registroPasarelas;
        this.ensamblador = ensamblador;
    }

    public List<PlantaDTO> obtenerPlantas() {
        return repositorioPlantas.buscarTodas().stream()
                .map(ensamblador::aPlantaDTO)
                .collect(Collectors.toList());
    }

    public CapacidadPlantaDTO obtenerCapacidadPlanta(String idPlanta, LocalDate fecha) {
        if (fecha == null) {
            throw new ExcepcionPeticionInvalida("La fecha de consulta es obligatoria");
        }
        PlantaReciclaje planta = repositorioPlantas.buscarPorId(idPlanta)
                .orElseThrow(() -> new ExcepcionNoEncontrado("No existe la planta con identificador: " + idPlanta));

        PasarelaPlanta pasarela = registroPasarelas.obtenerPasarela(planta.getId());
        double toneladasDisponibles = pasarela.consultarCapacidad(fecha);
        return ensamblador.aCapacidadPlantaDTO(planta, fecha, toneladasDisponibles);
    }
}
