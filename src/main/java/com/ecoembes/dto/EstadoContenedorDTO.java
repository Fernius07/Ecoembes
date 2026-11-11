package com.ecoembes.dto;

import com.ecoembes.dominio.NivelLlenado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Estado de un contenedor en una fecha concreta")
public record EstadoContenedorDTO(
        @Schema(description = "Identificador del contenedor", example = "CONT-001")
        String idContenedor,

        @Schema(description = "Dirección del contenedor", example = "Plaza Circular 1, Abando")
        String direccion,

        @Schema(description = "Código postal", example = "48001")
        String codigoPostal,

        @Schema(description = "Fecha del registro (yyyy-MM-dd)", example = "2026-11-11")
        LocalDate fecha,

        @Schema(description = "Número estimado de envases", example = "650")
        int envases,

        @Schema(description = "Nivel de llenado", example = "VERDE")
        NivelLlenado nivelLlenado
) {
}
