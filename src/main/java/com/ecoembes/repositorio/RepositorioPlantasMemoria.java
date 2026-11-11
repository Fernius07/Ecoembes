package com.ecoembes.repositorio;

import com.ecoembes.dominio.PlantaReciclaje;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class RepositorioPlantasMemoria implements RepositorioPlantas {

    private final Map<String, PlantaReciclaje> plantasPorId = new ConcurrentHashMap<>();

    @Override
    public Optional<PlantaReciclaje> buscarPorId(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(plantasPorId.get(id.trim().toUpperCase()));
    }

    @Override
    public PlantaReciclaje guardar(PlantaReciclaje planta) {
        plantasPorId.put(planta.getId().toUpperCase(), planta);
        return planta;
    }

    @Override
    public List<PlantaReciclaje> buscarTodas() {
        return plantasPorId.values().stream()
                .sorted(Comparator.comparing(PlantaReciclaje::getId))
                .collect(Collectors.toList());
    }
}
