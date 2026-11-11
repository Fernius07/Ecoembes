package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Datos para registrar un nuevo contenedor")
public record CrearContenedorDTO(
        @Schema(description = "Identificador único del contenedor", example = "CONT-010")
        @NotBlank(message = "El identificador es obligatorio")
        String id,

        @Schema(description = "Dirección postal del contenedor", example = "Gran Vía 45, Bilbao")
        @NotBlank(message = "La dirección es obligatoria")
        String direccion,

        @Schema(description = "Código postal de la zona", example = "48009")
        @NotBlank(message = "El código postal es obligatorio")
        String codigoPostal,

        @Schema(description = "Capacidad máxima en número de envases", example = "2500")
        @NotNull(message = "La capacidad inicial es obligatoria")
        @Positive(message = "La capacidad inicial debe ser mayor que cero")
        Integer capacidadInicial
) {
}
