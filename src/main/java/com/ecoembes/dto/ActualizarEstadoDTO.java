package com.ecoembes.dto;

import com.ecoembes.dominio.NivelLlenado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Datos enviados por el sensor RecycloBin UltraEye para actualizar el contenedor")
public record ActualizarEstadoDTO(
        @Schema(description = "Cantidad estimada en toneladas", example = "0.08")
        @NotNull(message = "Las toneladas son obligatorias")
        @PositiveOrZero(message = "Las toneladas no pueden ser negativas")
        Double toneladas,

        @Schema(description = "Nivel de llenado (si no se envía, se calcula automáticamente)", example = "NARANJA", nullable = true)
        NivelLlenado nivelLlenado
) {
}
