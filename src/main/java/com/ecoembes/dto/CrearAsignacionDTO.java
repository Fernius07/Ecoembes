package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Solicitud de asignación de contenedores a una planta de reciclaje")
public record CrearAsignacionDTO(
        @Schema(description = "Identificador de la planta destino", example = "PLASSB")
        @NotBlank(message = "El identificador de la planta es obligatorio")
        String idPlanta,

        @Schema(description = "Lista de identificadores de contenedores a asignar", example = "[\"CONT-001\", \"CONT-004\"]")
        @NotEmpty(message = "Debe incluir al menos un contenedor")
        List<String> idsContenedores,

        @Schema(description = "Fecha de asignación (yyyy-MM-dd)", example = "2026-11-11")
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha
) {
}
