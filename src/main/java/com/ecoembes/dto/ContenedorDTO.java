package com.ecoembes.dto;

import com.ecoembes.dominio.NivelLlenado;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información general de un contenedor de reciclaje")
public record ContenedorDTO(
        @Schema(description = "Identificador del contenedor", example = "CONT-001")
        String id,

        @Schema(description = "Dirección del contenedor", example = "Plaza Circular 1, Abando")
        String direccion,

        @Schema(description = "Código postal", example = "48001")
        String codigoPostal,

        @Schema(description = "Capacidad máxima en toneladas", example = "0.1")
        double capacidadInicial,

        @Schema(description = "Cantidad actual en toneladas", example = "0.0325")
        double toneladasActuales,

        @Schema(description = "Nivel de llenado actual", example = "VERDE")
        NivelLlenado nivelLlenado
) {
}
