package com.ecoembes.repositorio;

import com.ecoembes.dominio.Asignacion;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioAsignaciones {
    Asignacion guardar(Asignacion asignacion);
    List<Asignacion> buscarTodas();
    boolean estaContenedorAsignadoEnFecha(String idContenedor, LocalDate fecha);
}
