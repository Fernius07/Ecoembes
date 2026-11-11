package com.ecoembes.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Contenedor {

    private String id;
    private String direccion;
    private String codigoPostal;
    private int capacidadInicial;
    private int envasesActuales;
    private NivelLlenado nivelLlenado;
    private List<RegistroEstadoContenedor> historial;

    public Contenedor(String id, String direccion, String codigoPostal, int capacidadInicial,
                      int envasesActuales, NivelLlenado nivelLlenado) {
        this.id = id;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.capacidadInicial = capacidadInicial;
        this.envasesActuales = envasesActuales;
        this.nivelLlenado = nivelLlenado;
        this.historial = new ArrayList<>();
    }

    public synchronized RegistroEstadoContenedor registrarEstado(LocalDate fecha, int envases, NivelLlenado nivel) {
        this.envasesActuales = envases;
        this.nivelLlenado = nivel;
        RegistroEstadoContenedor registro = new RegistroEstadoContenedor(fecha, envases, nivel);
        historial.removeIf(r -> r.getFecha().equals(fecha));
        historial.add(registro);
        historial.sort(Comparator.comparing(RegistroEstadoContenedor::getFecha));
        return registro;
    }

    public synchronized void agregarRegistroHistorico(RegistroEstadoContenedor registro) {
        historial.removeIf(r -> r.getFecha().equals(registro.getFecha()));
        historial.add(registro);
        historial.sort(Comparator.comparing(RegistroEstadoContenedor::getFecha));
    }

    public synchronized Optional<RegistroEstadoContenedor> obtenerEstadoEnFecha(LocalDate fecha) {
        return historial.stream()
                .filter(r -> r.getFecha().equals(fecha))
                .findFirst();
    }

    public synchronized List<RegistroEstadoContenedor> obtenerHistorialEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return historial.stream()
                .filter(r -> !r.getFecha().isBefore(fechaInicio) && !r.getFecha().isAfter(fechaFin))
                .sorted(Comparator.comparing(RegistroEstadoContenedor::getFecha))
                .collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public int getCapacidadInicial() {
        return capacidadInicial;
    }

    public int getEnvasesActuales() {
        return envasesActuales;
    }

    public NivelLlenado getNivelLlenado() {
        return nivelLlenado;
    }

    public List<RegistroEstadoContenedor> getHistorial() {
        return historial;
    }
}
