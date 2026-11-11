package com.ecoembes.repositorio;

import com.ecoembes.dominio.Asignacion;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class RepositorioAsignacionesMemoria implements RepositorioAsignaciones {

    private final List<Asignacion> asignaciones = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Asignacion guardar(Asignacion asignacion) {
        asignaciones.add(asignacion);
        return asignacion;
    }

    @Override
    public List<Asignacion> buscarTodas() {
        synchronized (asignaciones) {
            return new ArrayList<>(asignaciones);
        }
    }

    @Override
    public boolean estaContenedorAsignadoEnFecha(String idContenedor, LocalDate fecha) {
        synchronized (asignaciones) {
            return asignaciones.stream()
                    .filter(a -> a.getFecha().equals(fecha))
                    .flatMap(a -> a.getContenedores().stream())
                    .anyMatch(c -> c.getId().equalsIgnoreCase(idContenedor));
        }
    }
}
