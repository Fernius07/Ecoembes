# Diseño del API REST - Servidor Ecoembes (Prototipo 1)

## 1. Patrones de diseño aplicados en la arquitectura

- **Patrón Fachada (`com.ecoembes.fachada.EcoembesController`)**: Punto único de entrada REST del servidor que expone todas las operaciones del sistema bajo el prefijo `/api` y delega la ejecución en los servicios de aplicación. Los nombres de los métodos de la fachada coinciden exactamente con los definidos en este documento y en los diagramas UML de clases y de secuencia.
- **Patrón Servicio de Aplicación (`com.ecoembes.servicio.*`)**: Clases de servicio donde reside la lógica de negocio (`ServicioAutenticacion`, `ServicioContenedores`, `ServicioPlantas` y `ServicioAsignaciones`).
- **Patrón Objeto de Transferencia de Datos (DTO) y Ensamblador (`com.ecoembes.dto.*` y `com.ecoembes.ensamblador.EnsambladorDTO`)**: Objetos de transferencia de datos para desacoplar las entidades del dominio de las peticiones y respuestas JSON del API REST.
- **Patrón Gestión de Estado (`com.ecoembes.estado.EstadoServidor`)**: Mantiene en memoria los tokens de sesión activos y los datos del personal autenticado asociado a cada sesión.
- **Patrón Pasarela (`com.ecoembes.externo.PasarelaPlanta`)**: Abstrae y desacopla la comunicación con las plantas de reciclaje externas (`PlasSB Ltd.` y `ContSocket Ltd.`).

---

## 2. Operaciones del API REST

| Operación | HTTP | Ruta | Parámetros | Retorno | Códigos HTTP |
|---|---|---|---|---|---|
| `login` | `POST` | `/api/auth/login` | `LoginDTO (email, contrasena)` | `TokenDTO` | `200, 401` |
| `logout` | `POST` | `/api/auth/logout` | `token` | Vacío | `204, 401` |
| `crearContenedor` | `POST` | `/api/contenedores` | `token, CrearContenedorDTO (id, direccion, codigoPostal, capacidadInicial)` | `ContenedorDTO` | `201, 400, 401` |
| `obtenerContenedores` | `GET` | `/api/contenedores` | `token` | `List<ContenedorDTO>` | `200, 401` |
| `obtenerContenedoresPorZona` | `GET` | `/api/contenedores` | `token, codigoPostal, fecha` | `List<EstadoContenedorDTO>` | `200, 400, 401` |
| `obtenerUsoContenedor` | `GET` | `/api/contenedores/{id}/uso` | `id, token, fechaInicio, fechaFin` | `List<EstadoContenedorDTO>` | `200, 400, 401, 404` |
| `actualizarEstadoContenedor` | `PUT` | `/api/contenedores/{id}/estado` | `id, token, ActualizarEstadoDTO (envases, nivelLlenado)` | `EstadoContenedorDTO` | `200, 400, 401, 404` |
| `obtenerPlantas` | `GET` | `/api/plantas` | `token` | `List<PlantaDTO>` | `200, 401` |
| `obtenerCapacidadPlanta` | `GET` | `/api/plantas/{id}/capacidad` | `id, token, fecha` | `CapacidadPlantaDTO` | `200, 400, 401, 404` |
| `crearAsignacion` | `POST` | `/api/asignaciones` | `token, CrearAsignacionDTO (idPlanta, idsContenedores, fecha)` | `AsignacionDTO` | `201, 400, 401, 404` |
