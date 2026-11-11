package com.ecoembes.ensamblador;

import com.ecoembes.dominio.Asignacion;
import com.ecoembes.dominio.Contenedor;
import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.dominio.RegistroEstadoContenedor;
import com.ecoembes.dto.AsignacionDTO;
import com.ecoembes.dto.CapacidadPlantaDTO;
import com.ecoembes.dto.ContenedorDTO;
import com.ecoembes.dto.EstadoContenedorDTO;
import com.ecoembes.dto.PlantaDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EnsambladorDTO {

    public ContenedorDTO aContenedorDTO(Contenedor contenedor) {
        return new ContenedorDTO(
                contenedor.getId(),
                contenedor.getDireccion(),
                contenedor.getCodigoPostal(),
                contenedor.getCapacidadInicial(),
                contenedor.getEnvasesActuales(),
                contenedor.getNivelLlenado()
        );
    }

    public EstadoContenedorDTO aEstadoContenedorDTO(Contenedor contenedor, RegistroEstadoContenedor registro) {
        return new EstadoContenedorDTO(
                contenedor.getId(),
                contenedor.getDireccion(),
                contenedor.getCodigoPostal(),
                registro.getFecha(),
                registro.getEnvases(),
                registro.getNivelLlenado()
        );
    }

    public PlantaDTO aPlantaDTO(PlantaReciclaje planta) {
        return new PlantaDTO(
                planta.getId(),
                planta.getNombre(),
                planta.getUbicacion()
        );
    }

    public CapacidadPlantaDTO aCapacidadPlantaDTO(PlantaReciclaje planta, LocalDate fecha, double toneladasDisponibles) {
        return new CapacidadPlantaDTO(
                planta.getId(),
                planta.getNombre(),
                fecha,
                toneladasDisponibles
        );
    }

    public AsignacionDTO aAsignacionDTO(Asignacion asignacion) {
        List<String> ids = asignacion.getContenedores().stream()
                .map(Contenedor::getId)
                .collect(Collectors.toList());

        return new AsignacionDTO(
                asignacion.getId(),
                asignacion.getPlanta().getId(),
                asignacion.getPlanta().getNombre(),
                asignacion.getPersonal().getEmail(),
                asignacion.getFecha(),
                ids,
                ids.size(),
                asignacion.getTotalEnvases(),
                asignacion.getToneladasEstimadas()
        );
    }
}
