package com.ecoembes.servicio;

import com.ecoembes.dominio.PersonalEcoembes;
import com.ecoembes.dto.LoginDTO;
import com.ecoembes.dto.TokenDTO;
import com.ecoembes.estado.EstadoServidor;
import com.ecoembes.excepcion.ExcepcionNoAutorizado;
import com.ecoembes.repositorio.RepositorioPersonal;
import org.springframework.stereotype.Service;

@Service
public class ServicioAutenticacion {

    private final RepositorioPersonal repositorioPersonal;
    private final EstadoServidor estadoServidor;

    public ServicioAutenticacion(RepositorioPersonal repositorioPersonal, EstadoServidor estadoServidor) {
        this.repositorioPersonal = repositorioPersonal;
        this.estadoServidor = estadoServidor;
    }

    public TokenDTO login(LoginDTO peticion) {
        if (peticion == null || peticion.email() == null || peticion.contrasena() == null) {
            throw new ExcepcionNoAutorizado("Es necesario indicar el email y la contraseña");
        }
        PersonalEcoembes personal = repositorioPersonal.buscarPorEmail(peticion.email())
                .filter(p -> p.getContrasena().equals(peticion.contrasena()))
                .orElseThrow(() -> new ExcepcionNoAutorizado("Credenciales incorrectas"));

        String token = estadoServidor.crearSesion(personal);
        return new TokenDTO(token, personal.getEmail(), personal.getNombre());
    }

    public void logout(String token) {
        boolean eliminado = estadoServidor.cerrarSesion(token);
        if (!eliminado) {
            throw new ExcepcionNoAutorizado("El token no es válido o la sesión ya se ha cerrado");
        }
    }

    public PersonalEcoembes validarToken(String token) {
        return estadoServidor.obtenerPersonalPorToken(token)
                .orElseThrow(() -> new ExcepcionNoAutorizado("Token inválido o sesión caducada"));
    }
}
