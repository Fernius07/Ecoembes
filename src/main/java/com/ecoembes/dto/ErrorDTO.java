package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Respuesta de error del servidor")
public record ErrorDTO(
        @Schema(description = "Fecha y hora del error", example = "2026-11-11T12:30:00")
        LocalDateTime fechaHora,

        @Schema(description = "Código de estado HTTP", example = "400")
        int estado,

        @Schema(description = "Tipo de error", example = "Petición inválida")
        String error,

        @Schema(description = "Descripción del error", example = "Capacidad disponible insuficiente en la planta")
        String mensaje,

        @Schema(description = "Ruta invocada", example = "/api/asignaciones")
        String ruta
) {
}
