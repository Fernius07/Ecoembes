# Proyecto Ecoembes - Prototipo 1

Repositorio correspondiente a la primera entrega de la asignatura **Diseño de Software** (Universidad de Deusto). Incluye el diseño del API REST, los diagramas UML de clases y secuencia, y la implementación en memoria del **Servidor Ecoembes**.

## 1. Requisitos

- **Java:** JDK 17 o superior (configurado con JDK 21 compilando hacia compatibilidad Java 17).
- **Gradle:** Incluido mediante Gradle Wrapper (`gradlew` / `gradlew.bat`).
- **Spring Boot:** 3.3.5 (`spring-boot-starter-web`, `spring-boot-starter-validation`).
- **Swagger / OpenAPI:** `springdoc-openapi-starter-webmvc-ui:2.6.0`.

## 2. Ejecución del proyecto

### Desde Eclipse IDE
1. Hacer clic derecho sobre el proyecto `ecoembes-prototipo1` -> **Refresh (F5)**.
2. Abrir `src/main/java/com/ecoembes/EcoembesApplication.java`.
3. Hacer clic derecho -> **Run As -> Java Application** (o *Spring Boot App*).
4. Para detener el servidor, pulsar el botón cuadrado rojo (**Terminate**) en la vista **Console**.

### Desde terminal
```bash
.\gradlew.bat build
.\gradlew.bat bootRun
```

## 3. Acceso a Swagger UI

Con el servidor arrancado en el puerto `8080`:
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

## 4. Usuarios y datos de prueba iniciales

### Personal de Ecoembes (`InicializadorDatos`)
| Nombre | Correo (`email`) | Contraseña (`contrasena`) |
|---|---|---|
| Ane García | `ane.garcia@ecoembes.com` | `ecoembes123` |
| Jon Etxebarria | `jon.etxebarria@ecoembes.com` | `ecoembes123` |

### Contenedores cargados en Bilbao
Se cargan inicialmente 9 contenedores (`CONT-001` a `CONT-009`) repartidos en los códigos postales `48001`, `48002`, `48003` y `48004`, con estados `VERDE`, `NARANJA` y `ROJO` y un histórico de 8 días por contenedor.

### Plantas de reciclaje simuladas
- **`PLASSB` (PlasSB Ltd.):** Capacidad habitual de `1,50 t` diarias durante los próximos 10 días (y `0,02 t` en el día `hoy + 1` para probar el caso de capacidad insuficiente).
- **`CONTSOCKET` (ContSocket Ltd.):** Capacidad habitual de `2,00 t` diarias (y `0,04 t` en el día actual para pruebas de límite de capacidad).

## 5. Organización de paquetes

```text
com.ecoembes
 ├── fachada       -> EcoembesController (Patrón Façade)
 ├── dto           -> Records DTO de entrada y salida
 ├── servicio      -> ServicioAutenticacion, ServicioContenedores, ServicioPlantas, ServicioAsignaciones (Patrón AppService)
 ├── dominio       -> Contenedor, RegistroEstadoContenedor, PlantaReciclaje, CapacidadDiariaPlanta, PersonalEcoembes, Asignacion, NivelLlenado
 ├── estado        -> EstadoServidor (Patrón StateManagement)
 ├── externo       -> PasarelaPlanta, PasarelaPlasSB, PasarelaContSocket, RegistroPasarelasPlanta
 ├── repositorio   -> Interfaces y repositorios en memoria
 ├── ensamblador   -> EnsambladorDTO
 ├── configuracion -> InicializadorDatos, ConfiguracionOpenApi
 └── excepcion     -> Excepciones y ManejadorGlobalExcepciones
```
