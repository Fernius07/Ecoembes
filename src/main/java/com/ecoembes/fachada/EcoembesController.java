package com.ecoembes.fachada;

import com.ecoembes.dominio.PersonalEcoembes;
import com.ecoembes.dto.ActualizarEstadoDTO;
import com.ecoembes.dto.AsignacionDTO;
import com.ecoembes.dto.CapacidadPlantaDTO;
import com.ecoembes.dto.ContenedorDTO;
import com.ecoembes.dto.CrearAsignacionDTO;
import com.ecoembes.dto.CrearContenedorDTO;
import com.ecoembes.dto.EstadoContenedorDTO;
import com.ecoembes.dto.LoginDTO;
import com.ecoembes.dto.PlantaDTO;
import com.ecoembes.dto.TokenDTO;
import com.ecoembes.servicio.ServicioAsignaciones;
import com.ecoembes.servicio.ServicioAutenticacion;
import com.ecoembes.servicio.ServicioContenedores;
import com.ecoembes.servicio.ServicioPlantas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Ecoembes Controller", description = "Fachada REST del Servidor Ecoembes")
public class EcoembesController {

    private final ServicioAutenticacion servicioAutenticacion;
    private final ServicioContenedores servicioContenedores;
    private final ServicioPlantas servicioPlantas;
    private final ServicioAsignaciones servicioAsignaciones;

    public EcoembesController(ServicioAutenticacion servicioAutenticacion,
                              ServicioContenedores servicioContenedores,
                              ServicioPlantas servicioPlantas,
                              ServicioAsignaciones servicioAsignaciones) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.servicioContenedores = servicioContenedores;
        this.servicioPlantas = servicioPlantas;
        this.servicioAsignaciones = servicioAsignaciones;
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Iniciar sesión de personal de Ecoembes")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada correctamente")
    @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO peticion) {
        return ResponseEntity.ok(servicioAutenticacion.login(peticion));
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "Cerrar sesión activa")
    @ApiResponse(responseCode = "204", description = "Sesión cerrada")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token) {
        servicioAutenticacion.logout(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/contenedores")
    @Operation(summary = "Registrar un nuevo contenedor")
    @ApiResponse(responseCode = "201", description = "Contenedor creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o contenedor ya existente")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    public ResponseEntity<ContenedorDTO> crearContenedor(
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token,
            @Valid @RequestBody CrearContenedorDTO peticion) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioContenedores.crearContenedor(peticion));
    }

    @GetMapping(value = "/contenedores", params = {"!codigoPostal", "!fecha"})
    @Operation(summary = "Listar contenedores o consultar estado por zona y fecha")
    @ApiResponse(responseCode = "200", description = "Listado de contenedores")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    public ResponseEntity<List<ContenedorDTO>> obtenerContenedores(
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.ok(servicioContenedores.obtenerContenedores());
    }

    @GetMapping(value = "/contenedores", params = {"codigoPostal", "fecha"})
    @Operation(summary = "Consultar contenedores de una zona en una fecha")
    @ApiResponse(responseCode = "200", description = "Estados de la zona obtenidos")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    public ResponseEntity<List<EstadoContenedorDTO>> obtenerContenedoresPorZona(
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token,
            @Parameter(description = "Código postal de la zona", example = "48001", required = false)
            @RequestParam(value = "codigoPostal", required = false) String codigoPostal,
            @Parameter(description = "Fecha de consulta (yyyy-MM-dd)", example = "2026-11-11", required = false)
            @RequestParam(value = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.ok(servicioContenedores.obtenerContenedoresPorZona(codigoPostal, fecha));
    }

    @GetMapping("/contenedores/{id}/uso")
    @Operation(summary = "Consultar histórico de uso de un contenedor entre fechas")
    @ApiResponse(responseCode = "200", description = "Histórico obtenido")
    @ApiResponse(responseCode = "400", description = "Rango de fechas inválido")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    public ResponseEntity<List<EstadoContenedorDTO>> obtenerUsoContenedor(
            @Parameter(description = "Identificador del contenedor", example = "CONT-001")
            @PathVariable("id") String id,
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token,
            @Parameter(description = "Fecha de inicio (yyyy-MM-dd)", example = "2026-11-04")
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha de fin (yyyy-MM-dd)", example = "2026-11-11")
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.ok(servicioContenedores.obtenerUsoContenedor(id, fechaInicio, fechaFin));
    }

    @PutMapping("/contenedores/{id}/estado")
    @Operation(summary = "Actualizar estado diario del contenedor (sensor RecycloBin)")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    @ApiResponse(responseCode = "404", description = "Contenedor no encontrado")
    public ResponseEntity<EstadoContenedorDTO> actualizarEstadoContenedor(
            @Parameter(description = "Identificador del contenedor", example = "CONT-001")
            @PathVariable("id") String id,
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token,
            @Valid @RequestBody ActualizarEstadoDTO peticion) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.ok(servicioContenedores.actualizarEstadoContenedor(id, peticion));
    }

    @GetMapping("/plantas")
    @Operation(summary = "Listar plantas de reciclaje")
    @ApiResponse(responseCode = "200", description = "Lista de plantas")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    public ResponseEntity<List<PlantaDTO>> obtenerPlantas(
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.ok(servicioPlantas.obtenerPlantas());
    }

    @GetMapping("/plantas/{id}/capacidad")
    @Operation(summary = "Consultar capacidad disponible de una planta en una fecha")
    @ApiResponse(responseCode = "200", description = "Capacidad disponible obtenida")
    @ApiResponse(responseCode = "400", description = "Fecha fuera del rango disponible")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    @ApiResponse(responseCode = "404", description = "Planta no encontrada")
    public ResponseEntity<CapacidadPlantaDTO> obtenerCapacidadPlanta(
            @Parameter(description = "Identificador de la planta", example = "PLASSB")
            @PathVariable("id") String id,
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token,
            @Parameter(description = "Fecha de consulta (yyyy-MM-dd)", example = "2026-11-11")
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        servicioAutenticacion.validarToken(token);
        return ResponseEntity.ok(servicioPlantas.obtenerCapacidadPlanta(id, fecha));
    }

    @PostMapping("/asignaciones")
    @Operation(summary = "Asignar contenedores a una planta de reciclaje")
    @ApiResponse(responseCode = "201", description = "Asignación creada")
    @ApiResponse(responseCode = "400", description = "Capacidad insuficiente o datos inválidos")
    @ApiResponse(responseCode = "401", description = "Token no válido")
    @ApiResponse(responseCode = "404", description = "Planta o contenedor no encontrado")
    public ResponseEntity<AsignacionDTO> crearAsignacion(
            @Parameter(description = "Token de sesión activo", required = true)
            @RequestParam("token") String token,
            @Valid @RequestBody CrearAsignacionDTO peticion) {
        PersonalEcoembes personal = servicioAutenticacion.validarToken(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioAsignaciones.crearAsignacion(peticion, personal));
    }
}
