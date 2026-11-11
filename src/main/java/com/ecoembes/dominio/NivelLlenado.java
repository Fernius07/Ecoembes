package com.ecoembes.dominio;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Nivel de llenado del contenedor: VERDE, NARANJA o ROJO")
public enum NivelLlenado {
    VERDE,
    NARANJA,
    ROJO
}
