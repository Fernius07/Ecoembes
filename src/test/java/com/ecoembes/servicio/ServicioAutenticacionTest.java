package com.ecoembes.servicio;

import com.ecoembes.dominio.PersonalEcoembes;
import com.ecoembes.dto.LoginDTO;
import com.ecoembes.dto.TokenDTO;
import com.ecoembes.estado.EstadoServidor;
import com.ecoembes.excepcion.ExcepcionNoAutorizado;
import com.ecoembes.repositorio.RepositorioPersonal;
import com.ecoembes.repositorio.RepositorioPersonalMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioAutenticacionTest {

    private RepositorioPersonal repositorioPersonal;
    private EstadoServidor estadoServidor;
    private ServicioAutenticacion servicioAutenticacion;

    @BeforeEach
    void inicializar() {
        repositorioPersonal = new RepositorioPersonalMemoria();
        estadoServidor = new EstadoServidor();
        servicioAutenticacion = new ServicioAutenticacion(repositorioPersonal, estadoServidor);

        repositorioPersonal.guardar(new PersonalEcoembes(
                "EMP-01", "Ian Anaya Abascal", "ian.anaya@ecoembes.com", "ecoembes123"));
    }

    @Test
    void testLoginCorrecto() {
        TokenDTO respuesta = servicioAutenticacion.login(
                new LoginDTO("ian.anaya@ecoembes.com", "ecoembes123"));

        assertNotNull(respuesta.token());
        assertEquals("ian.anaya@ecoembes.com", respuesta.email());
        assertEquals("Ian Anaya Abascal", respuesta.nombre());
        assertTrue(estadoServidor.esTokenActivo(respuesta.token()));

        PersonalEcoembes validado = servicioAutenticacion.validarToken(respuesta.token());
        assertEquals("EMP-01", validado.getId());
    }

    @Test
    void testLoginCredencialesIncorrectas() {
        assertThrows(ExcepcionNoAutorizado.class, () ->
                servicioAutenticacion.login(new LoginDTO("ian.anaya@ecoembes.com", "incorrecta")));

        assertThrows(ExcepcionNoAutorizado.class, () ->
                servicioAutenticacion.login(new LoginDTO("otro@ecoembes.com", "ecoembes123")));
    }

    @Test
    void testLogoutEliminaToken() {
        TokenDTO respuesta = servicioAutenticacion.login(
                new LoginDTO("ian.anaya@ecoembes.com", "ecoembes123"));
        String token = respuesta.token();

        servicioAutenticacion.logout(token);
        assertFalse(estadoServidor.esTokenActivo(token));

        assertThrows(ExcepcionNoAutorizado.class, () -> servicioAutenticacion.validarToken(token));
    }
}
