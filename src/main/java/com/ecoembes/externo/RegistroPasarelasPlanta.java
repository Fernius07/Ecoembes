package com.ecoembes.externo;

import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RegistroPasarelasPlanta {

    private final Map<String, PasarelaPlanta> pasarelasPorIdPlanta = new ConcurrentHashMap<>();

    public RegistroPasarelasPlanta(List<PasarelaPlanta> pasarelas) {
        for (PasarelaPlanta pasarela : pasarelas) {
            pasarelasPorIdPlanta.put(pasarela.obtenerIdPlanta().toUpperCase(), pasarela);
        }
    }

    public PasarelaPlanta obtenerPasarela(String idPlanta) {
        if (idPlanta == null || idPlanta.isBlank()) {
            throw new ExcepcionNoEncontrado("El identificador de planta no puede estar vacío");
        }
        PasarelaPlanta pasarela = pasarelasPorIdPlanta.get(idPlanta.trim().toUpperCase());
        if (pasarela == null) {
            throw new ExcepcionNoEncontrado("No existe ninguna planta con identificador: " + idPlanta);
        }
        return pasarela;
    }
}
