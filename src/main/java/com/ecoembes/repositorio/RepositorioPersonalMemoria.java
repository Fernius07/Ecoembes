package com.ecoembes.repositorio;

import com.ecoembes.dominio.PersonalEcoembes;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RepositorioPersonalMemoria implements RepositorioPersonal {

    private final Map<String, PersonalEcoembes> personalPorEmail = new ConcurrentHashMap<>();

    @Override
    public Optional<PersonalEcoembes> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(personalPorEmail.get(email.trim().toLowerCase()));
    }

    @Override
    public void guardar(PersonalEcoembes personal) {
        personalPorEmail.put(personal.getEmail().trim().toLowerCase(), personal);
    }

    @Override
    public List<PersonalEcoembes> buscarTodos() {
        return new ArrayList<>(personalPorEmail.values());
    }
}
