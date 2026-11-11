package com.ecoembes.repositorio;

import com.ecoembes.dominio.Contenedor;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class RepositorioContenedoresMemoria implements RepositorioContenedores {

    private final Map<String, Contenedor> contenedoresPorId = new ConcurrentHashMap<>();

    @Override
    public Optional<Contenedor> buscarPorId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(contenedoresPorId.get(id.trim()));
    }

    @Override
    public boolean existePorId(String id) {
        if (id == null) {
            return false;
        }
        return contenedoresPorId.containsKey(id.trim());
    }

    @Override
    public Contenedor guardar(Contenedor contenedor) {
        contenedoresPorId.put(contenedor.getId(), contenedor);
        return contenedor;
    }

    @Override
    public List<Contenedor> buscarTodos() {
        return contenedoresPorId.values().stream()
                .sorted(Comparator.comparing(Contenedor::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Contenedor> buscarPorCodigoPostal(String codigoPostal) {
        if (codigoPostal == null) {
            return List.of();
        }
        String cp = codigoPostal.trim();
        return contenedoresPorId.values().stream()
                .filter(c -> c.getCodigoPostal().equalsIgnoreCase(cp))
                .sorted(Comparator.comparing(Contenedor::getId))
                .collect(Collectors.toList());
    }
}
