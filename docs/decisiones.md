# Decisiones de Diseño - Prototipo 1 (Ecoembes)

En este documento se detallan las decisiones de diseño y las reglas de negocio adoptadas por el equipo durante la construcción del Prototipo 1 del Servidor Ecoembes.

## 1. Modelo de datos, reglas de negocio y decisiones del API REST

1. **Ubicación, código postal y filtrado en `GET /api/contenedores`**:
   - Para dar soporte tanto al alta de contenedores con su dirección física como a la consulta del estado de los contenedores de una zona por código postal, la entidad `Contenedor` y el DTO `CrearContenedorDTO` incluyen ambos atributos: `direccion` y `codigoPostal`.
   - En el API REST, el recurso `GET /api/contenedores` permite tanto listar todos los contenedores (cuando se invoca únicamente con `token` mediante `obtenerContenedores`) como filtrar por zona y fecha pasando los parámetros de consulta `codigoPostal` y `fecha` (mediante `obtenerContenedoresPorZona`).

2. **Cálculo del nivel de llenado (`NivelLlenado`) y registro histórico diario (`PUT /api/contenedores/{id}/estado`)**:
   - Se han definido tres niveles de llenado en función del porcentaje de ocupación respecto a la capacidad inicial del contenedor:
     - `VERDE`: menos del 60 % de ocupación.
     - `NARANJA`: entre el 60 % y el 90 % de ocupación.
     - `ROJO`: más del 90 % de ocupación.
   - Cuando el sensor *RecycloBin UltraEye* actualiza el estado diario de un contenedor (`PUT /api/contenedores/{id}/estado`), si no se indica el nivel de llenado explícitamente, el servidor lo calcula aplicando estos umbrales.
   - Asimismo, cada llamada a `PUT /api/contenedores/{id}/estado` no solo actualiza el estado actual del contenedor, sino que **guarda un registro fechado (`RegistroEstadoContenedor`) en el historial del contenedor para la fecha del día (`LocalDate.now()`)** (actualizándolo de forma idempotente si ya existía una lectura en ese mismo día). Esto garantiza que la consulta de uso entre fechas (`GET /api/contenedores/{id}/uso`) disponga siempre de la serie histórica diaria completa.

3. **Conversión de envases a toneladas (`toneladasEstimadas`)**:
   - Para estimar el peso de los envases al realizar una asignación a una planta de reciclaje, se establece un factor fijo de **`0,05 kg` (50 gramos) por envase** (`PESO_ENVASE_KG = 0.05` en `ServicioAsignaciones`).
   - Por tanto, la fórmula aplicada es: `toneladasEstimadas = totalEnvases * 0,05 / 1000`.

4. **Reducción de la capacidad disponible de la planta tras cada asignación**:
   - Cada planta de reciclaje (`PlasSB Ltd.` y `ContSocket Ltd.`) publica su capacidad diaria en toneladas para un horizonte de 10 días desde la fecha actual (`CapacidadDiariaPlanta`).
   - Al ejecutar `POST /api/asignaciones`, el servicio valida primero que ningún contenedor de la lista haya sido asignado ya en esa misma fecha y que `toneladasEstimadas <= toneladasDisponibles` en la planta para la fecha solicitada.
   - Cuando la asignación es válida y se notifica a la planta a través de su pasarela (`PasarelaPlanta.notificarAsignacion`), **se descuenta automáticamente el valor de `toneladasEstimadas` de las `toneladasDisponibles` de esa planta para esa fecha** (`PlantaReciclaje.reducirCapacidad`). Si una asignación posterior intenta superar la capacidad restante de ese día, el servidor devuelve el error `400 Petición inválida`.

5. **Gestión de sesiones en memoria (Patrón Gestión de Estado en `EstadoServidor`)**:
   - Al iniciar sesión (`POST /api/auth/login`), el servidor genera un token correspondiente a la marca de tiempo en milisegundos del momento del inicio de sesión y lo asocia al objeto `PersonalEcoembes` dentro de un mapa concurrente en `EstadoServidor`.
   - Este mapa permite tanto validar el token en todas las operaciones protegidas como recuperar el empleado autenticado que realiza una asignación para registrarlo en auditoría (`emailPersonal`).
   - Al cerrar sesión (`POST /api/auth/logout`), se elimina el token del mapa, quedando inválido inmediatamente para cualquier petición posterior (`401 No autorizado`).

6. **Comunicación desacoplada con plantas de reciclaje externas (Patrón Pasarela en `PasarelaPlanta`)**:
   - En este primer prototipo no existen aún los servidores externos independientes, por lo que `PasarelaPlasSB` y `PasarelaContSocket` simulan la consulta de capacidad y la recepción de notificaciones de asignación.
   - Ambas clases implementan la interfaz común `PasarelaPlanta` y se registran dinámicamente en `RegistroPasarelasPlanta`. De esta manera, `ServicioPlantas` y `ServicioAsignaciones` no dependen de ninguna planta concreta y es posible añadir nuevas plantas en el futuro sin modificar la lógica de negocio.

7. **Coherencia de nombres e idioma en todo el proyecto**:
   - Todos los paquetes, clases, atributos, rutas del API REST y diagramas están íntegramente en español.
   - Los nombres de las operaciones de la fachada (`EcoembesController`) son idénticos en el código fuente, en la especificación del API REST (`docs/diseno_api.md`) y en los diagramas UML de clases y de secuencia (`login`, `logout`, `crearContenedor`, `obtenerContenedores`, `obtenerContenedoresPorZona`, `obtenerUsoContenedor`, `actualizarEstadoContenedor`, `obtenerPlantas`, `obtenerCapacidadPlanta`, `crearAsignacion`).
