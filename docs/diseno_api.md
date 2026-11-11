# Diseño del API REST - Servidor Ecoembes (Prototipo 1)

## 1. Patrones de diseño aplicados en la arquitectura

- **Patrón Fachada (`com.ecoembes.fachada.EcoembesController`)**: Punto único de entrada REST del servidor que expone todas las operaciones del sistema bajo el prefijo `/api` y delega la ejecución en los servicios de aplicación. Los nombres de los métodos de la fachada coinciden exactamente con los definidos en este documento y en los diagramas UML de clases y de secuencia.
- **Patrón Servicio de Aplicación (`com.ecoembes.servicio.*`)**: Clases de servicio donde reside la lógica de negocio (`ServicioAutenticacion`, `ServicioContenedores`, `ServicioPlantas` y `ServicioAsignaciones`).
- **Patrón Objeto de Transferencia de Datos (DTO) y Ensamblador (`com.ecoembes.dto.*` y `com.ecoembes.ensamblador.EnsambladorDTO`)**: Objetos de transferencia de datos para desacoplar las entidades del dominio de las peticiones y respuestas JSON del API REST.
- **Patrón Gestión de Estado (`com.ecoembes.estado.EstadoServidor`)**: Mantiene en memoria los tokens de sesión activos y los datos del personal autenticado asociado a cada sesión.
- **Patrón Pasarela (`com.ecoembes.externo.PasarelaPlanta`)**: Abstrae y desacopla la comunicación con las plantas de reciclaje externas (`PlasSB Ltd.` y `ContSocket Ltd.`).

---

## 2. Operaciones del API REST

### 2.1. Iniciar sesión (`login`)
- **Método HTTP y ruta:** `POST /api/auth/login`
- **Parámetros de entrada:** Cuerpo JSON `LoginDTO { email, contrasena }`
- **Valor de retorno:** `TokenDTO { token, email, nombre }` (`200 OK`) o error de autenticación (`401 No autorizado`).

### 2.2. Cerrar sesión (`logout`)
- **Método HTTP y ruta:** `POST /api/auth/logout?token={token}`
- **Parámetros de entrada:** Parámetro de consulta `token` (`String`)
- **Valor de retorno:** Sin contenido (`204 Sin contenido`) o token inválido (`401 No autorizado`).

### 2.3. Crear un nuevo contenedor (`crearContenedor`)
- **Método HTTP y ruta:** `POST /api/contenedores?token={token}`
- **Parámetros de entrada:** Parámetro de consulta `token`; Cuerpo JSON `CrearContenedorDTO { id, direccion, codigoPostal, capacidadInicial }`
- **Valor de retorno:** `ContenedorDTO` (`201 Creado`), error de validación o identificador duplicado (`400 Petición inválida`), o `401 No autorizado`.

### 2.4. Consultar contenedores general o filtrando por zona y fecha (`obtenerContenedores` / `obtenerContenedoresPorZona`)
- **Método HTTP y ruta:** `GET /api/contenedores?token={token}&codigoPostal={cp}&fecha={yyyy-MM-dd}`
- **Parámetros de entrada:**
  - `token` (obligatorio): Token de sesión activo.
  - `codigoPostal` y `fecha` (filtros opcionales): Código postal de la zona y fecha de consulta en formato `yyyy-MM-dd`.
- **Comportamiento unificado del recurso:**
  - **Sin filtros opcionales (`GET /api/contenedores?token={token}`):** invoca `obtenerContenedores` y devuelve `List<ContenedorDTO>` (`200 OK`) con todos los contenedores registrados y su estado actual.
  - **Con filtros de zona y fecha (`GET /api/contenedores?token={token}&codigoPostal={cp}&fecha={yyyy-MM-dd}`):** invoca `obtenerContenedoresPorZona` y devuelve `List<EstadoContenedorDTO>` (`200 OK`) con la ubicación, envases y nivel de llenado que tenían los contenedores de ese código postal en la fecha indicada. En Swagger UI ambos casos se presentan unificados bajo `GET /api/contenedores`.
- **Valor de retorno:** `200 OK`, parámetros inválidos (`400 Petición inválida`) o `401 No autorizado`.

### 2.5. Consultar histórico de uso de un contenedor entre fechas (`obtenerUsoContenedor`)
- **Método HTTP y ruta:** `GET /api/contenedores/{id}/uso?token={token}&fechaInicio={yyyy-MM-dd}&fechaFin={yyyy-MM-dd}`
- **Parámetros de entrada:** Parámetro de ruta `id`; Parámetros de consulta `token`, `fechaInicio`, `fechaFin`
- **Valor de retorno:** `List<EstadoContenedorDTO>` con los registros históricos del contenedor dentro del rango `[fechaInicio, fechaFin]` (`200 OK`), fechas invertidas o inválidas (`400 Petición inválida`), contenedor no encontrado (`404 No encontrado`) o `401 No autorizado`.

### 2.6. Actualizar información diaria del contenedor (`actualizarEstadoContenedor`)
- **Método HTTP y ruta:** `PUT /api/contenedores/{id}/estado?token={token}`
- **Parámetros de entrada:** Parámetro de ruta `id`; Parámetro de consulta `token`; Cuerpo JSON `ActualizarEstadoDTO { envases, nivelLlenado (opcional) }`
- **Persistencia en el histórico diario:** Esta operación simula la lectura diaria enviada por el sensor *RecycloBin UltraEye*. Además de actualizar el número de envases y el nivel de llenado actuales del contenedor (calculando el nivel según los umbrales `< 60 %` `VERDE`, `60 %–90 %` `NARANJA`, `> 90 %` `ROJO` si no se especifica), **guarda un registro fechado (`RegistroEstadoContenedor`) asociado a la fecha del día (`LocalDate.now()`) en el historial del contenedor**. Si el sensor envía más de una lectura en la misma fecha, actualiza de forma idempotente el registro de ese día, manteniendo así el histórico diario necesario para la consulta de uso entre fechas (`GET /api/contenedores/{id}/uso`).
- **Valor de retorno:** `EstadoContenedorDTO` (`200 OK`), `400 Petición inválida`, `404 No encontrado` o `401 No autorizado`.

### 2.7. Listar plantas de reciclaje (`obtenerPlantas`)
- **Método HTTP y ruta:** `GET /api/plantas?token={token}`
- **Parámetros de entrada:** Parámetro de consulta `token`
- **Valor de retorno:** `List<PlantaDTO>` (`200 OK`) o `401 No autorizado`.

### 2.8. Consultar capacidad disponible de una planta (`obtenerCapacidadPlanta`)
- **Método HTTP y ruta:** `GET /api/plantas/{id}/capacidad?token={token}&fecha={yyyy-MM-dd}`
- **Parámetros de entrada:** Parámetro de ruta `id`; Parámetros de consulta `token`, `fecha`
- **Valor de retorno:** `CapacidadPlantaDTO { idPlanta, nombre, fecha, toneladasDisponibles }` (`200 OK`), fecha fuera del horizonte de 10 días (`400 Petición inválida`), planta no encontrada (`404 No encontrado`) o `401 No autorizado`.

### 2.9. Asignar contenedores a una planta (`crearAsignacion`)
- **Método HTTP y ruta:** `POST /api/asignaciones?token={token}`
- **Parámetros de entrada:** Parámetro de consulta `token`; Cuerpo JSON `CrearAsignacionDTO { idPlanta, idsContenedores, fecha }`
- **Cálculo de `toneladasEstimadas` y actualización de la capacidad de la planta:**
  1. Se suman los envases actuales de todos los contenedores seleccionados (`totalEnvases`) y se convierten a toneladas aplicando un factor fijo de **`0,05 kg` (50 gramos) por envase**:
     $$\text{toneladasEstimadas} = \frac{\text{totalEnvases} \times 0,05}{1000}$$
  2. Se consulta a través de la pasarela de la planta (`PasarelaPlanta.consultarCapacidad`) la capacidad disponible para esa `fecha` y se verifica que `toneladasEstimadas <= toneladasDisponibles`. Si las toneladas requeridas superan la capacidad disponible (o si algún contenedor ya fue asignado en esa misma fecha), se rechaza la operación con `400 Petición inválida`.
  3. Al confirmarse la asignación y notificar a la planta (`PasarelaPlanta.notificarAsignacion`), **la capacidad disponible de dicha planta para esa fecha se reduce automáticamente restando las `toneladasEstimadas` asignadas** (`PlantaReciclaje.reducirCapacidad(fecha, toneladasEstimadas)`).
  4. Finalmente, se almacena la asignación incluyendo para fines de auditoría el identificador y correo del empleado autenticado (`emailPersonal`) obtenido a partir del `token` en `EstadoServidor`.
- **Valor de retorno:** `AsignacionDTO { id, idPlanta, nombrePlanta, emailPersonal, fecha, idsContenedores, totalContenedores, totalEnvases, toneladasEstimadas }` (`201 Creado`), capacidad insuficiente o contenedor ya asignado (`400 Petición inválida`), `404 No encontrado` o `401 No autorizado`.
