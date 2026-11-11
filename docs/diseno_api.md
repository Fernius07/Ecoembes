# Diseño del API REST - Servidor Ecoembes (Prototipo 1)

## 1. Patrones aplicados en la arquitectura

- **Façade (`com.ecoembes.fachada.EcoembesController`)**: Punto único de entrada REST del servidor que expone las operaciones del sistema y delega en los servicios de aplicación.
- **AppService (`com.ecoembes.servicio.*`)**: Servicios donde reside la lógica de negocio (`ServicioAutenticacion`, `ServicioContenedores`, `ServicioPlantas` y `ServicioAsignaciones`).
- **DTO y Assembler (`com.ecoembes.dto.*` y `com.ecoembes.ensamblador.EnsambladorDTO`)**: Objetos de transferencia de datos para desacoplar las entidades del dominio de las respuestas JSON del API.
- **StateManagement (`com.ecoembes.estado.EstadoServidor`)**: Mantiene en memoria los tokens activos y los datos del personal autenticado.
- **Gateway (`com.ecoembes.externo.PasarelaPlanta`)**: Abstrae la comunicación con las plantas de reciclaje externas (`PlasSB Ltd.` y `ContSocket Ltd.`).

---

## 2. Operaciones del API REST

### 2.1. Iniciar sesión (`login`)
- **Método HTTP y ruta:** `POST /api/auth/login`
- **Parámetros:** Body `LoginDTO { email, contrasena }`
- **Retorno:** `TokenDTO { token, email, nombre }` (`200 OK`) o error de autenticación (`401 Unauthorized`).

### 2.2. Cerrar sesión (`logout`)
- **Método HTTP y ruta:** `POST /api/auth/logout?token={token}`
- **Parámetros:** Query `token` (`String`)
- **Retorno:** Sin contenido (`204 No Content`) o token inválido (`401 Unauthorized`).

### 2.3. Crear un nuevo contenedor (`crearContenedor`)
- **Método HTTP y ruta:** `POST /api/contenedores?token={token}`
- **Parámetros:** Query `token`; Body `CrearContenedorDTO { id, direccion, codigoPostal, capacidadInicial }`
- **Retorno:** `ContenedorDTO` (`201 Created`), error de validación o ID duplicado (`400 Bad Request`), o `401 Unauthorized`.

### 2.4. Listar contenedores (`obtenerContenedores`)
- **Método HTTP y ruta:** `GET /api/contenedores?token={token}`
- **Parámetros:** Query `token`
- **Retorno:** `List<ContenedorDTO>` (`200 OK`) o `401 Unauthorized`.

### 2.5. Consultar contenedores de una zona en una fecha (`obtenerContenedoresPorZona`)
- **Método HTTP y ruta:** `GET /api/contenedores?token={token}&codigoPostal={cp}&fecha={yyyy-MM-dd}`
- **Parámetros:** Query `token`, `codigoPostal`, `fecha`
- **Retorno:** `List<EstadoContenedorDTO>` (`200 OK`), `400 Bad Request` o `401 Unauthorized`.

### 2.6. Consultar uso/estado de un contenedor entre fechas (`obtenerUsoContenedor`)
- **Método HTTP y ruta:** `GET /api/contenedores/{id}/uso?token={token}&fechaInicio={yyyy-MM-dd}&fechaFin={yyyy-MM-dd}`
- **Parámetros:** Path `id`; Query `token`, `fechaInicio`, `fechaFin`
- **Retorno:** `List<EstadoContenedorDTO>` (`200 OK`), fechas inválidas (`400 Bad Request`), contenedor no encontrado (`404 Not Found`) o `401 Unauthorized`.

### 2.7. Actualizar información diaria del contenedor (`actualizarEstadoContenedor`)
- **Método HTTP y ruta:** `PUT /api/contenedores/{id}/estado?token={token}`
- **Parámetros:** Path `id`; Query `token`; Body `ActualizarEstadoDTO { envases, nivelLlenado (opcional) }`
- **Retorno:** `EstadoContenedorDTO` (`200 OK`), `400 Bad Request`, `404 Not Found` o `401 Unauthorized`.

### 2.8. Listar plantas de reciclaje (`obtenerPlantas`)
- **Método HTTP y ruta:** `GET /api/plantas?token={token}`
- **Parámetros:** Query `token`
- **Retorno:** `List<PlantaDTO>` (`200 OK`) o `401 Unauthorized`.

### 2.9. Consultar capacidad disponible de una planta (`obtenerCapacidadPlanta`)
- **Método HTTP y ruta:** `GET /api/plantas/{id}/capacidad?token={token}&fecha={yyyy-MM-dd}`
- **Parámetros:** Path `id`; Query `token`, `fecha`
- **Retorno:** `CapacidadPlantaDTO { idPlanta, nombre, fecha, toneladasDisponibles }` (`200 OK`), `400 Bad Request`, `404 Not Found` o `401 Unauthorized`.

### 2.10. Asignar contenedores a una planta (`crearAsignacion`)
- **Método HTTP y ruta:** `POST /api/asignaciones?token={token}`
- **Parámetros:** Query `token`; Body `CrearAsignacionDTO { idPlanta, idsContenedores, fecha }`
- **Retorno:** `AsignacionDTO { id, idPlanta, nombrePlanta, emailPersonal, fecha, idsContenedores, totalContenedores, totalEnvases, toneladasEstimadas }` (`201 Created`), capacidad excedida o contenedor ya asignado (`400 Bad Request`), `404 Not Found` o `401 Unauthorized`.
