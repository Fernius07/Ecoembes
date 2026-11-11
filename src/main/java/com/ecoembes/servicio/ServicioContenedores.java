package com.ecoembes.servicio;

import com.ecoembes.dominio.Contenedor;
import com.ecoembes.dominio.NivelLlenado;
import com.ecoembes.dominio.RegistroEstadoContenedor;
import com.ecoembes.dto.ActualizarEstadoDTO;
import com.ecoembes.dto.ContenedorDTO;
import com.ecoembes.dto.CrearContenedorDTO;
import com.ecoembes.dto.EstadoContenedorDTO;
import com.ecoembes.ensamblador.EnsambladorDTO;
import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import com.ecoembes.excepcion.ExcepcionPeticionInvalida;
import com.ecoembes.repositorio.RepositorioContenedores;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ServicioContenedores {

    private final RepositorioContenedores repositorioContenedores;
    private final EnsambladorDTO ensamblador;

    public ServicioContenedores(RepositorioContenedores repositorioContenedores, EnsambladorDTO ensamblador) {
        this.repositorioContenedores = repositorioContenedores;
        this.ensamblador = ensamblador;
    }

    public ContenedorDTO crearContenedor(CrearContenedorDTO peticion) {
        if (peticion == null || peticion.id() == null || peticion.id().isBlank()
                || peticion.direccion() == null || peticion.direccion().isBlank()
                || peticion.codigoPostal() == null || peticion.codigoPostal().isBlank()
                || peticion.capacidadInicial() == null || peticion.capacidadInicial() <= 0) {
            throw new ExcepcionPeticionInvalida("Todos los datos del contenedor son obligatorios y la capacidad debe ser positiva");
        }

        String idNormalizado = peticion.id().trim();
        if (repositorioContenedores.existePorId(idNormalizado)) {
            throw new ExcepcionPeticionInvalida("Ya existe un contenedor con el identificador: " + idNormalizado);
        }

        Contenedor contenedor = new Contenedor(
                idNormalizado,
                peticion.direccion().trim(),
                peticion.codigoPostal().trim(),
                peticion.capacidadInicial(),
                0,
                NivelLlenado.VERDE
        );
        contenedor.registrarEstado(LocalDate.now(), 0, NivelLlenado.VERDE);
        repositorioContenedores.guardar(contenedor);
        return ensamblador.aContenedorDTO(contenedor);
    }

    public List<ContenedorDTO> obtenerContenedores() {
        return repositorioContenedores.buscarTodos().stream()
                .map(ensamblador::aContenedorDTO)
                .collect(Collectors.toList());
    }

    public List<EstadoContenedorDTO> obtenerContenedoresPorZona(String codigoPostal, LocalDate fecha) {
        if (codigoPostal == null || codigoPostal.isBlank() || fecha == null) {
            throw new ExcepcionPeticionInvalida("Debe indicar el código postal y la fecha");
        }
        List<Contenedor> contenedores = repositorioContenedores.buscarPorCodigoPostal(codigoPostal);
        List<EstadoContenedorDTO> resultado = new ArrayList<>();
        for (Contenedor contenedor : contenedores) {
            Optional<RegistroEstadoContenedor> registroOpt = contenedor.obtenerEstadoEnFecha(fecha);
            if (registroOpt.isPresent()) {
                resultado.add(ensamblador.aEstadoContenedorDTO(contenedor, registroOpt.get()));
            } else if (fecha.equals(LocalDate.now())) {
                RegistroEstadoContenedor actual = new RegistroEstadoContenedor(
                        fecha, contenedor.getEnvasesActuales(), contenedor.getNivelLlenado());
                resultado.add(ensamblador.aEstadoContenedorDTO(contenedor, actual));
            }
        }
        return resultado;
    }

    public List<EstadoContenedorDTO> obtenerUsoContenedor(String id, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ExcepcionPeticionInvalida("Debe indicar la fecha de inicio y la fecha de fin");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new ExcepcionPeticionInvalida("La fecha de inicio (" + fechaInicio
                    + ") no puede ser posterior a la fecha de fin (" + fechaFin + ")");
        }
        Contenedor contenedor = repositorioContenedores.buscarPorId(id)
                .orElseThrow(() -> new ExcepcionNoEncontrado("No existe el contenedor con identificador: " + id));

        return contenedor.obtenerHistorialEntreFechas(fechaInicio, fechaFin).stream()
                .map(registro -> ensamblador.aEstadoContenedorDTO(contenedor, registro))
                .collect(Collectors.toList());
    }

    public EstadoContenedorDTO actualizarEstadoContenedor(String id, ActualizarEstadoDTO peticion) {
        if (peticion == null || peticion.envases() == null || peticion.envases() < 0) {
            throw new ExcepcionPeticionInvalida("El número de envases debe ser mayor o igual a cero");
        }
        Contenedor contenedor = repositorioContenedores.buscarPorId(id)
                .orElseThrow(() -> new ExcepcionNoEncontrado("No existe el contenedor con identificador: " + id));

        NivelLlenado nivel = peticion.nivelLlenado() != null
                ? peticion.nivelLlenado()
                : calcularNivelLlenado(peticion.envases(), contenedor.getCapacidadInicial());

        RegistroEstadoContenedor registro = contenedor.registrarEstado(LocalDate.now(), peticion.envases(), nivel);
        repositorioContenedores.guardar(contenedor);
        return ensamblador.aEstadoContenedorDTO(contenedor, registro);
    }

    public NivelLlenado calcularNivelLlenado(int envases, int capacidad) {
        if (capacidad <= 0) {
            return NivelLlenado.ROJO;
        }
        double porcentaje = (envases * 100.0) / capacidad;
        if (porcentaje < 60.0) {
            return NivelLlenado.VERDE;
        } else if (porcentaje <= 90.0) {
            return NivelLlenado.NARANJA;
        } else {
            return NivelLlenado.ROJO;
        }
    }
}
