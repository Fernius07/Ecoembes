package com.ecoembes.repositorio;

import com.ecoembes.dominio.PlantaReciclaje;

import java.util.List;
import java.util.Optional;

public interface RepositorioPlantas {
    Optional<PlantaReciclaje> buscarPorId(String id);
    PlantaReciclaje guardar(PlantaReciclaje planta);
    List<PlantaReciclaje> buscarTodas();
}
