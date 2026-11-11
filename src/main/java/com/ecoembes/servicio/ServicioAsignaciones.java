package com.ecoembes.servicio;

import com.ecoembes.dominio.Asignacion;
import com.ecoembes.dominio.Contenedor;
import com.ecoembes.dominio.PersonalEcoembes;
import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.dto.AsignacionDTO;
import com.ecoembes.dto.CrearAsignacionDTO;
import com.ecoembes.ensamblador.EnsambladorDTO;
import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import com.ecoembes.excepcion.ExcepcionPeticionInvalida;
import com.ecoembes.externo.PasarelaPlanta;
import com.ecoembes.externo.RegistroPasarelasPlanta;
import com.ecoembes.repositorio.RepositorioAsignaciones;
import com.ecoembes.repositorio.RepositorioContenedores;
import com.ecoembes.repositorio.RepositorioPlantas;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ServicioAsignaciones {

    public static final double PESO_ENVASE_KG = 0.05;
    public static final int DIAS_MAXIMOS_ANTELACION = 10;

    private final RepositorioContenedores repositorioContenedores;
    private final RepositorioPlantas repositorioPlantas;
    private final RepositorioAsignaciones repositorioAsignaciones;
    private final RegistroPasarelasPlanta registroPasarelas;
    private final EnsambladorDTO ensamblador;
    private final AtomicInteger secuencia = new AtomicInteger(1);

    public ServicioAsignaciones(RepositorioContenedores repositorioContenedores,
                                RepositorioPlantas repositorioPlantas,
                                RepositorioAsignaciones repositorioAsignaciones,
                                RegistroPasarelasPlanta registroPasarelas,
                                EnsambladorDTO ensamblador) {
        this.repositorioContenedores = repositorioContenedores;
        this.repositorioPlantas = repositorioPlantas;
        this.repositorioAsignaciones = repositorioAsignaciones;
        this.registroPasarelas = registroPasarelas;
        this.ensamblador = ensamblador;
    }

    public synchronized AsignacionDTO crearAsignacion(CrearAsignacionDTO peticion, PersonalEcoembes personal) {
        if (peticion == null || peticion.idPlanta() == null || peticion.idPlanta().isBlank()
                || peticion.idsContenedores() == null || peticion.idsContenedores().isEmpty()
                || peticion.fecha() == null) {
            throw new ExcepcionPeticionInvalida("Debe especificar la planta, los contenedores y la fecha");
        }

        PlantaReciclaje planta = repositorioPlantas.buscarPorId(peticion.idPlanta())
                .orElseThrow(() -> new ExcepcionNoEncontrado("No existe la planta con identificador: " + peticion.idPlanta()));

        PasarelaPlanta pasarela = registroPasarelas.obtenerPasarela(planta.getId());

        List<Contenedor> contenedoresSeleccionados = new ArrayList<>();
        Set<String> idsVistos = new HashSet<>();
        for (String idRecibido : peticion.idsContenedores()) {
            if (idRecibido == null || idRecibido.isBlank()) {
                throw new ExcepcionPeticionInvalida("La lista contiene un identificador de contenedor vacío");
            }
            String idLimpio = idRecibido.trim();
            Contenedor contenedor = repositorioContenedores.buscarPorId(idLimpio)
                    .orElseThrow(() -> new ExcepcionNoEncontrado("No existe el contenedor con identificador: " + idLimpio));
            if (!idsVistos.add(contenedor.getId())) {
                throw new ExcepcionPeticionInvalida("El contenedor " + contenedor.getId() + " está repetido en la solicitud");
            }
            contenedoresSeleccionados.add(contenedor);
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaMaxima = hoy.plusDays(DIAS_MAXIMOS_ANTELACION);
        if (peticion.fecha().isBefore(hoy) || peticion.fecha().isAfter(fechaMaxima)) {
            throw new ExcepcionPeticionInvalida("La fecha (" + peticion.fecha()
                    + ") debe estar dentro de los próximos " + DIAS_MAXIMOS_ANTELACION + " días");
        }

        for (Contenedor contenedor : contenedoresSeleccionados) {
            if (repositorioAsignaciones.estaContenedorAsignadoEnFecha(contenedor.getId(), peticion.fecha())) {
                throw new ExcepcionPeticionInvalida("El contenedor " + contenedor.getId()
                        + " ya está asignado para la fecha " + peticion.fecha());
            }
        }

        int totalEnvases = contenedoresSeleccionados.stream()
                .mapToInt(Contenedor::getEnvasesActuales)
                .sum();
        double toneladasEstimadas = calcularToneladas(totalEnvases);

        double toneladasDisponibles = pasarela.consultarCapacidad(peticion.fecha());
        if (toneladasEstimadas > toneladasDisponibles) {
            throw new ExcepcionPeticionInvalida(String.format(Locale.US,
                    "Capacidad disponible insuficiente en la planta %s para el día %s: se necesitan %.4f t (%d envases) y solo hay %.4f t disponibles",
                    planta.getNombre(), peticion.fecha(), toneladasEstimadas, totalEnvases, toneladasDisponibles));
        }

        pasarela.reducirCapacidad(peticion.fecha(), toneladasEstimadas);
        pasarela.notificarAsignacion(peticion.fecha(), contenedoresSeleccionados.size(), totalEnvases, toneladasEstimadas);

        String idAsignacion = String.format("ASIG-%03d", secuencia.getAndIncrement());
        Asignacion asignacion = new Asignacion(
                idAsignacion,
                planta,
                personal,
                contenedoresSeleccionados,
                peticion.fecha(),
                totalEnvases,
                toneladasEstimadas
        );
        repositorioAsignaciones.guardar(asignacion);
        return ensamblador.aAsignacionDTO(asignacion);
    }

    public double calcularToneladas(int totalEnvases) {
        double toneladas = (totalEnvases * PESO_ENVASE_KG) / 1000.0;
        return Math.round(toneladas * 100000.0) / 100000.0;
    }
}
