package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Datos de una asignación de contenedores registrada")
public record AsignacionDTO(
        @Schema(description = "Identificador de la asignación", example = "ASIG-001")
        String id,

        @Schema(description = "Identificador de la planta", example = "PLASSB")
        String idPlanta,

        @Schema(description = "Nombre de la planta", example = "PlasSB Ltd.")
        String nombrePlanta,

        @Schema(description = "Correo del empleado que realizó la asignación", example = "ian.anaya@ecoembes.com")
        String emailPersonal,

        @Schema(description = "Fecha de asignación (yyyy-MM-dd)", example = "2026-11-11")
        LocalDate fecha,

        @Schema(description = "Identificadores de los contenedores asignados", example = "[\"CONT-001\", \"CONT-004\"]")
        List<String> idsContenedores,

        @Schema(description = "Número total de contenedores asignados", example = "2")
        int totalContenedores,

        @Schema(description = "Suma total de envases", example = "1450")
        int totalEnvases,

        @Schema(description = "Peso estimado en toneladas", example = "0.0725")
        double toneladasEstimadas
) {
}
