package com.ecoembes.repositorio;

import com.ecoembes.dominio.PersonalEcoembes;

import java.util.List;
import java.util.Optional;

public interface RepositorioPersonal {
    Optional<PersonalEcoembes> buscarPorEmail(String email);
    void guardar(PersonalEcoembes personal);
    List<PersonalEcoembes> buscarTodos();
}
