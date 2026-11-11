package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Capacidad disponible en toneladas de una planta para una fecha")
public record CapacidadPlantaDTO(
        @Schema(description = "Identificador de la planta", example = "PLASSB")
        String idPlanta,

        @Schema(description = "Nombre de la planta", example = "PlasSB Ltd.")
        String nombre,

        @Schema(description = "Fecha consultada (yyyy-MM-dd)", example = "2026-11-11")
        LocalDate fecha,

        @Schema(description = "Toneladas disponibles para esa fecha", example = "1.5")
        double toneladasDisponibles
) {
}
