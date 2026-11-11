package com.ecoembes.configuracion;

import com.ecoembes.dominio.Contenedor;
import com.ecoembes.dominio.NivelLlenado;
import com.ecoembes.dominio.PersonalEcoembes;
import com.ecoembes.dominio.PlantaReciclaje;
import com.ecoembes.dominio.RegistroEstadoContenedor;
import com.ecoembes.externo.PasarelaContSocket;
import com.ecoembes.externo.PasarelaPlasSB;
import com.ecoembes.repositorio.RepositorioContenedores;
import com.ecoembes.repositorio.RepositorioPersonal;
import com.ecoembes.repositorio.RepositorioPlantas;
import com.ecoembes.servicio.ServicioContenedores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class InicializadorDatos implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorDatos.class);

    private final RepositorioPersonal repositorioPersonal;
    private final RepositorioContenedores repositorioContenedores;
    private final RepositorioPlantas repositorioPlantas;
    private final ServicioContenedores servicioContenedores;

    public InicializadorDatos(RepositorioPersonal repositorioPersonal,
                              RepositorioContenedores repositorioContenedores,
                              RepositorioPlantas repositorioPlantas,
                              ServicioContenedores servicioContenedores) {
        this.repositorioPersonal = repositorioPersonal;
        this.repositorioContenedores = repositorioContenedores;
        this.repositorioPlantas = repositorioPlantas;
        this.servicioContenedores = servicioContenedores;
    }

    @Override
    public void run(String... args) {
        cargarPersonal();
        cargarContenedores();
        cargarPlantas();
        log.info("Datos iniciales del Servidor Ecoembes cargados en memoria.");
    }

    private void cargarPersonal() {
        repositorioPersonal.guardar(new PersonalEcoembes(
                "EMP-01", "Ian Anaya Abascal", "ian.anaya@ecoembes.com", "ecoembes123"));
        repositorioPersonal.guardar(new PersonalEcoembes(
                "EMP-02", "Xabier Diaz Garcia", "xabier.diaz@ecoembes.com", "ecoembes123"));
        repositorioPersonal.guardar(new PersonalEcoembes(
                "EMP-03", "Iker Diaz Lazaro", "iker.diaz@ecoembes.com", "ecoembes123"));
        repositorioPersonal.guardar(new PersonalEcoembes(
                "EMP-04", "Iñigo Fernandez Garcia", "inigo.fernandez@ecoembes.com", "ecoembes123"));
    }

    private void cargarContenedores() {
        LocalDate hoy = LocalDate.now();

        crearContenedorSemilla("CONT-001", "Plaza Circular 1, Abando", "48001", 2000, 650, hoy);
        crearContenedorSemilla("CONT-002", "Calle Buenos Aires 12, Abando", "48001", 2000, 1450, hoy);
        crearContenedorSemilla("CONT-003", "Alameda Mazarredo 24, Abando", "48001", 2000, 1920, hoy);
        crearContenedorSemilla("CONT-004", "Calle Hurtado de Amézaga 18", "48002", 2500, 800, hoy);
        crearContenedorSemilla("CONT-005", "Calle Autonomía 10, Indautxu", "48002", 2500, 1850, hoy);
        crearContenedorSemilla("CONT-006", "Calle San Francisco 32, Bilbao La Vieja", "48003", 1800, 520, hoy);
        crearContenedorSemilla("CONT-007", "Plaza Corazón de María 5", "48003", 1800, 1710, hoy);
        crearContenedorSemilla("CONT-008", "Calle Santutxu 40, Santutxu", "48004", 2200, 900, hoy);
        crearContenedorSemilla("CONT-009", "Avenida Zumalacárregui 65, Begoña", "48004", 2200, 1650, hoy);
    }

    private void crearContenedorSemilla(String id, String direccion, String codigoPostal,
                                        int capacidad, int envasesHoy, LocalDate hoy) {
        NivelLlenado nivelActual = servicioContenedores.calcularNivelLlenado(envasesHoy, capacidad);
        Contenedor contenedor = new Contenedor(id, direccion, codigoPostal, capacidad, envasesHoy, nivelActual);

        for (int diasAtras = 7; diasAtras >= 1; diasAtras--) {
            LocalDate fechaRegistro = hoy.minusDays(diasAtras);
            double factor = 0.25 + ((7 - diasAtras) * 0.10);
            int envasesHistoricos = Math.min(capacidad, (int) Math.round(envasesHoy * factor));
            NivelLlenado nivelHistorico = servicioContenedores.calcularNivelLlenado(envasesHistoricos, capacidad);
            contenedor.agregarRegistroHistorico(new RegistroEstadoContenedor(fechaRegistro, envasesHistoricos, nivelHistorico));
        }
        contenedor.registrarEstado(hoy, envasesHoy, nivelActual);
        repositorioContenedores.guardar(contenedor);
    }

    private void cargarPlantas() {
        LocalDate hoy = LocalDate.now();

        PlantaReciclaje plasSb = new PlantaReciclaje(
                PasarelaPlasSB.ID_PLANTA,
                PasarelaPlasSB.NOMBRE_PLANTA,
                "Polígono Industrial Asua-Erandio, Bizkaia"
        );

        PlantaReciclaje contSocket = new PlantaReciclaje(
                PasarelaContSocket.ID_PLANTA,
                PasarelaContSocket.NOMBRE_PLANTA,
                "Parque Tecnológico de Zamudio, Bizkaia"
        );

        for (int i = 0; i <= 10; i++) {
            LocalDate fecha = hoy.plusDays(i);
            if (i == 1) {
                plasSb.establecerCapacidadEnFecha(fecha, 0.02, 0.02);
            } else {
                plasSb.establecerCapacidadEnFecha(fecha, 1.50, 1.50);
            }

            if (i == 0) {
                contSocket.establecerCapacidadEnFecha(fecha, 0.04, 0.04);
            } else {
                contSocket.establecerCapacidadEnFecha(fecha, 2.00, 2.00);
            }
        }

        repositorioPlantas.guardar(plasSb);
        repositorioPlantas.guardar(contSocket);
    }
}
