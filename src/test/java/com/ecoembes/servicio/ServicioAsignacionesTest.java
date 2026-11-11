package com.ecoembes.servicio;

import com.ecoembes.dominio.Contenedor;
import com.ecoembes.dominio.NivelLlenado;
import com.ecoembes.dominio.PersonalEcoembes;
import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.dto.AsignacionDTO;
import com.ecoembes.dto.CrearAsignacionDTO;
import com.ecoembes.ensamblador.EnsambladorDTO;
import com.ecoembes.excepcion.ExcepcionNoEncontrado;
import com.ecoembes.excepcion.ExcepcionPeticionInvalida;
import com.ecoembes.externo.PasarelaContSocket;
import com.ecoembes.externo.PasarelaPlasSB;
import com.ecoembes.externo.RegistroPasarelasPlanta;
import com.ecoembes.repositorio.RepositorioAsignaciones;
import com.ecoembes.repositorio.RepositorioAsignacionesMemoria;
import com.ecoembes.repositorio.RepositorioContenedores;
import com.ecoembes.repositorio.RepositorioContenedoresMemoria;
import com.ecoembes.repositorio.RepositorioPlantas;
import com.ecoembes.repositorio.RepositorioPlantasMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioAsignacionesTest {

    private RepositorioContenedores repositorioContenedores;
    private RepositorioPlantas repositorioPlantas;
    private RepositorioAsignaciones repositorioAsignaciones;
    private RegistroPasarelasPlanta registroPasarelas;
    private ServicioAsignaciones servicioAsignaciones;
    private PersonalEcoembes personal;
    private LocalDate hoy;

    @BeforeEach
    void inicializar() {
        repositorioContenedores = new RepositorioContenedoresMemoria();
        repositorioPlantas = new RepositorioPlantasMemoria();
        repositorioAsignaciones = new RepositorioAsignacionesMemoria();

        PasarelaPlasSB pasarelaPlasSB = new PasarelaPlasSB(repositorioPlantas);
        PasarelaContSocket pasarelaContSocket = new PasarelaContSocket(repositorioPlantas);
        registroPasarelas = new RegistroPasarelasPlanta(List.of(pasarelaPlasSB, pasarelaContSocket));

        EnsambladorDTO ensamblador = new EnsambladorDTO();
        servicioAsignaciones = new ServicioAsignaciones(
                repositorioContenedores, repositorioPlantas, repositorioAsignaciones, registroPasarelas, ensamblador);

        personal = new PersonalEcoembes("EMP-01", "Ian Anaya Abascal", "ian.anaya@ecoembes.com", "ecoembes123");
        hoy = LocalDate.now();

        repositorioContenedores.guardar(new Contenedor("CONT-001", "Plaza Circular 1", "48001", 2000, 1000, NivelLlenado.VERDE));
        repositorioContenedores.guardar(new Contenedor("CONT-002", "Calle Buenos Aires 12", "48001", 2000, 1500, NivelLlenado.NARANJA));

        PlantaReciclaje plasSb = new PlantaReciclaje("PLASSB", "PlasSB Ltd.", "Erandio");
        plasSb.establecerCapacidadEnFecha(hoy, 1.00, 1.00);
        plasSb.establecerCapacidadEnFecha(hoy.plusDays(1), 0.05, 0.05);
        repositorioPlantas.guardar(plasSb);
    }

    @Test
    void testCrearAsignacionValida() {
        CrearAsignacionDTO peticion = new CrearAsignacionDTO(
                "PLASSB", List.of("CONT-001", "CONT-002"), hoy);

        AsignacionDTO resultado = servicioAsignaciones.crearAsignacion(peticion, personal);

        assertNotNull(resultado.id());
        assertEquals("PLASSB", resultado.idPlanta());
        assertEquals("ian.anaya@ecoembes.com", resultado.emailPersonal());
        assertEquals(2, resultado.totalContenedores());
        assertEquals(2500, resultado.totalEnvases());
        assertEquals(0.125, resultado.toneladasEstimadas(), 0.0001);

        double capacidadRestante = registroPasarelas.obtenerPasarela("PLASSB").consultarCapacidad(hoy);
        assertEquals(0.875, capacidadRestante, 0.0001);
    }

    @Test
    void testCrearAsignacionCapacidadExcedida() {
        CrearAsignacionDTO peticion = new CrearAsignacionDTO(
                "PLASSB", List.of("CONT-002"), hoy.plusDays(1));

        assertThrows(ExcepcionPeticionInvalida.class, () ->
                servicioAsignaciones.crearAsignacion(peticion, personal));
    }

    @Test
    void testCrearAsignacionContenedorInexistente() {
        CrearAsignacionDTO peticion = new CrearAsignacionDTO(
                "PLASSB", List.of("CONT-999"), hoy);

        assertThrows(ExcepcionNoEncontrado.class, () ->
                servicioAsignaciones.crearAsignacion(peticion, personal));
    }

    @Test
    void testCrearAsignacionContenedorDuplicadoEnMismaFecha() {
        CrearAsignacionDTO primera = new CrearAsignacionDTO("PLASSB", List.of("CONT-001"), hoy);
        servicioAsignaciones.crearAsignacion(primera, personal);

        CrearAsignacionDTO segunda = new CrearAsignacionDTO("PLASSB", List.of("CONT-001"), hoy);
        assertThrows(ExcepcionPeticionInvalida.class, () ->
                servicioAsignaciones.crearAsignacion(segunda, personal));
    }
}
