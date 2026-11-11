package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una planta de reciclaje")
public record PlantaDTO(
        @Schema(description = "Identificador de la planta", example = "PLASSB")
        String id,

        @Schema(description = "Nombre de la planta", example = "PlasSB Ltd.")
        String nombre,

        @Schema(description = "Ubicación de la planta", example = "Polígono Industrial Asua-Erandio, Bizkaia")
        String ubicacion
) {
}
