package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos de acceso para iniciar sesión")
public record LoginDTO(
        @Schema(description = "Correo electrónico del empleado", example = "ane.garcia@ecoembes.com")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        String email,

        @Schema(description = "Contraseña del empleado", example = "ecoembes123")
        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena
) {
}
