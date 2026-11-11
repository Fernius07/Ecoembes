package com.ecoembes.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con el token de sesión generado al iniciar sesión")
public record TokenDTO(
        @Schema(description = "Token de sesión (timestamp en milisegundos)", example = "1794393538316")
        String token,

        @Schema(description = "Correo electrónico del usuario autenticado", example = "ian.anaya@ecoembes.com")
        String email,

        @Schema(description = "Nombre del usuario autenticado", example = "Ian Anaya Abascal")
        String nombre
) {
}
