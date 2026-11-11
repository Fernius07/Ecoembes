package com.ecoembes.repositorio;

import com.ecoembes.dominio.Contenedor;

import java.util.List;
import java.util.Optional;

public interface RepositorioContenedores {
    Optional<Contenedor> buscarPorId(String id);
    boolean existePorId(String id);
    Contenedor guardar(Contenedor contenedor);
    List<Contenedor> buscarTodos();
    List<Contenedor> buscarPorCodigoPostal(String codigoPostal);
}
