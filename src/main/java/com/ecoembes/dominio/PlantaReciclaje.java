package com.ecoembes.dominio;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PlantaReciclaje {

    private String id;
    private String nombre;
    private String ubicacion;
    private Map<LocalDate, CapacidadDiariaPlanta> capacidadesDiarias;

    public PlantaReciclaje(String id, String nombre, String ubicacion) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidadesDiarias = new ConcurrentHashMap<>();
    }

    public Optional<CapacidadDiariaPlanta> obtenerCapacidadEnFecha(LocalDate fecha) {
        return Optional.ofNullable(capacidadesDiarias.get(fecha));
    }

    public void establecerCapacidadEnFecha(LocalDate fecha, double toneladasMaximas, double toneladasDisponibles) {
        capacidadesDiarias.put(fecha, new CapacidadDiariaPlanta(fecha, toneladasMaximas, toneladasDisponibles));
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public Map<LocalDate, CapacidadDiariaPlanta> getCapacidadesDiarias() {
        return capacidadesDiarias;
    }
}
