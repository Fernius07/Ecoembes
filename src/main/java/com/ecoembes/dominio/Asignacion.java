package com.ecoembes.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Asignacion {

    private String id;
    private PlantaReciclaje planta;
    private PersonalEcoembes personal;
    private List<Contenedor> contenedores;
    private LocalDate fecha;
    private int totalEnvases;
    private double toneladasEstimadas;

    public Asignacion(String id, PlantaReciclaje planta, PersonalEcoembes personal,
                      List<Contenedor> contenedores, LocalDate fecha,
                      int totalEnvases, double toneladasEstimadas) {
        this.id = id;
        this.planta = planta;
        this.personal = personal;
        this.contenedores = new ArrayList<>(contenedores);
        this.fecha = fecha;
        this.totalEnvases = totalEnvases;
        this.toneladasEstimadas = toneladasEstimadas;
    }

    public String getId() {
        return id;
    }

    public PlantaReciclaje getPlanta() {
        return planta;
    }

    public PersonalEcoembes getPersonal() {
        return personal;
    }

    public List<Contenedor> getContenedores() {
        return contenedores;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getTotalEnvases() {
        return totalEnvases;
    }

    public double getToneladasEstimadas() {
        return toneladasEstimadas;
    }
}
