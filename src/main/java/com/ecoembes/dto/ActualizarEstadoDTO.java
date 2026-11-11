package com.ecoembes.dto;

import com.ecoembes.dominio.NivelLlenado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos enviados por el sensor RecycloBin UltraEye para actualizar el contenedor")
public record ActualizarEstadoDTO(
        @Schema(description = "Número estimado de envases", example = "1600")
        @NotNull(message = "El número de envases es obligatorio")
        @Min(value = 0, message = "El número de envases no puede ser negativo")
        Integer envases,

        @Schema(description = "Nivel de llenado (si no se envía, se calcula automáticamente)", example = "NARANJA", nullable = true)
        NivelLlenado nivelLlenado
) {
}
