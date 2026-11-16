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
    private double capacidadInicial;
    private final List<RegistroEstadoContenedor> historial;

    public Contenedor(String id, String direccion, String codigoPostal, double capacidadInicial) {
        this(id, direccion, codigoPostal, capacidadInicial, 0.0, NivelLlenado.VERDE);
    }

    public Contenedor(String id, String direccion, String codigoPostal, double capacidadInicial,
                      double toneladasIniciales, NivelLlenado nivelInicial) {
        this.id = id;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.capacidadInicial = capacidadInicial;
        this.historial = new ArrayList<>();
        registrarEstado(LocalDate.now(), toneladasIniciales, nivelInicial);
    }

    public synchronized RegistroEstadoContenedor registrarEstado(LocalDate fecha, double toneladas, NivelLlenado nivel) {
        RegistroEstadoContenedor registro = new RegistroEstadoContenedor(fecha, toneladas, nivel);
        agregarRegistroHistorico(registro);
        return registro;
    }

    public synchronized void vaciar(LocalDate fecha) {
        registrarEstado(fecha, 0.0, NivelLlenado.VERDE);
    }

    public synchronized void agregarRegistroHistorico(RegistroEstadoContenedor registro) {
        historial.removeIf(r -> r.getFecha().equals(registro.getFecha()));
        historial.add(registro);
        historial.sort(Comparator.comparing(RegistroEstadoContenedor::getFecha));
    }

    public synchronized Optional<RegistroEstadoContenedor> obtenerUltimoEstado() {
        return historial.isEmpty()
                ? Optional.empty()
                : Optional.of(historial.get(historial.size() - 1));
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

    public double getCapacidadInicial() {
        return capacidadInicial;
    }

    public double getToneladasActuales() {
        return obtenerUltimoEstado().map(RegistroEstadoContenedor::getToneladas).orElse(0.0);
    }

    public NivelLlenado getNivelLlenado() {
        return obtenerUltimoEstado().map(RegistroEstadoContenedor::getNivelLlenado).orElse(NivelLlenado.VERDE);
    }

    public List<RegistroEstadoContenedor> getHistorial() {
        return historial;
    }
}
