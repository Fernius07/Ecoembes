package com.ecoembes.estado;

import com.ecoembes.dominio.PersonalEcoembes;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EstadoServidor {

    private final Map<String, PersonalEcoembes> sesionesActivas = new ConcurrentHashMap<>();

    public synchronized String crearSesion(PersonalEcoembes personal) {
        long marcaTiempo = System.currentTimeMillis();
        String token = String.valueOf(marcaTiempo);
        while (sesionesActivas.containsKey(token)) {
            marcaTiempo++;
            token = String.valueOf(marcaTiempo);
        }
        sesionesActivas.put(token, personal);
        return token;
    }

    public Optional<PersonalEcoembes> obtenerPersonalPorToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sesionesActivas.get(token.trim()));
    }

    public boolean cerrarSesion(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return sesionesActivas.remove(token.trim()) != null;
    }

    public boolean esTokenActivo(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return sesionesActivas.containsKey(token.trim());
    }

    public Map<String, PersonalEcoembes> getSesionesActivas() {
        return Collections.unmodifiableMap(sesionesActivas);
    }
}
