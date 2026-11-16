# Decisiones de Diseño - Prototipo 1 (Ecoembes)

En este documento se detallan las decisiones de diseño y las reglas de negocio adoptadas por el equipo durante la construcción del Prototipo 1 del Servidor Ecoembes.

## 1. Modelo de datos, reglas de negocio y decisiones del API REST

1. **Ubicación, código postal y filtrado en `GET /api/contenedores`**:
   - Para dar soporte tanto al alta de contenedores con su dirección física como a la consulta del estado de los contenedores de una zona por código postal, la entidad `Contenedor` y el DTO `CrearContenedorDTO` incluyen ambos atributos: `direccion` y `codigoPostal`.
   - En el API REST, el recurso `GET /api/contenedores` permite tanto listar todos los contenedores (cuando se invoca únicamente con `token` mediante `obtenerContenedores`) como filtrar por zona y fecha pasando los parámetros de consulta `codigoPostal` y `fecha` (mediante `obtenerContenedoresPorZona`).

2. **Cálculo del nivel de llenado (`NivelLlenado`) y registro histórico diario (`PUT /api/contenedores/{id}/estado`)**:
   - El enumerado `NivelLlenado` define los rangos porcentuales (`porcentajeMinimo` y `porcentajeMaximo`) y el método `calcularSegunPorcentaje(double)` respecto a la `capacidadInicial` en toneladas del contenedor:
     - `VERDE`: `[0.0 %, 60.0 %)` (`0.0` a `60.0`) de ocupación.
     - `NARANJA`: `[60.0 %, 90.0 %]` (`60.0` a `90.0`) de ocupación.
     - `ROJO`: `(90.0 %, 100.0 %]` (`90.0` a `100.0`) de ocupación.
   - Cuando el sensor *RecycloBin UltraEye* actualiza el estado diario de un contenedor (`PUT /api/contenedores/{id}/estado`), si no se indica el nivel de llenado explícitamente, el servidor lo calcula invocando `NivelLlenado.calcularSegunPorcentaje` a partir de las toneladas actuales frente a la capacidad inicial.
   - Asimismo, cada llamada a `PUT /api/contenedores/{id}/estado` no solo actualiza el estado actual del contenedor, sino que **guarda un registro fechado (`RegistroEstadoContenedor`) en el historial del contenedor para la fecha del día (`LocalDate.now()`)** (actualizándolo de forma idempotente si ya existía una lectura en ese mismo día). Esto garantiza que la consulta de uso entre fechas (`GET /api/contenedores/{id}/uso`) disponga siempre de la serie histórica diaria completa.

3. **Unidad única en toneladas (`double`)**:
   - Todo el modelo de dominio, los DTOs y las pasarelas externas trabajan directamente en **toneladas (`double`)**: la `capacidadInicial` y `toneladasActuales` de `Contenedor`, las `toneladas` de `RegistroEstadoContenedor` y la actualización de estado en `ActualizarEstadoDTO`.
   - Al crear una asignación, `toneladasEstimadas` se calcula sumando directamente `toneladasActuales` de los contenedores asignados, eliminando la conversión previa desde envases (`PESO_ENVASE_KG`) y el campo redundante `totalEnvases`.

4. **Reducción de la capacidad de la planta y vaciado según la fecha de asignación**:
   - Cada planta de reciclaje (`PlasSB Ltd.` y `ContSocket Ltd.`) publica su capacidad diaria en toneladas para un horizonte de 10 días desde la fecha actual (`CapacidadDiariaPlanta`).
   - Al ejecutar `POST /api/asignaciones`, el servicio valida primero que ningún contenedor de la lista haya sido asignado ya en esa misma fecha y que `toneladasEstimadas <= toneladasDisponibles` en la planta para la fecha solicitada.
   - Cuando la asignación es válida y se notifica a la planta a través de su pasarela (`PasarelaPlanta.notificarAsignacion(fecha, totalContenedores, toneladasEstimadas)`), **se descuenta automáticamente `toneladasEstimadas` de las `toneladasDisponibles` de esa planta para esa fecha** (`PlantaReciclaje.reducirCapacidad`). Si una asignación posterior intenta superar la capacidad restante de ese día, el servidor devuelve el error `400 Petición inválida`.
   - Además, el comportamiento sobre los contenedores depende de la fecha de asignación: si la asignación se realiza para el día actual (`LocalDate.now()`), los contenedores asignados se vacían inmediatamente (`Contenedor.vaciar(hoy)`, quedando en `0.0` toneladas y nivel `VERDE`); si la fecha es futura, los contenedores no se vacían hoy y conservan su carga actual.

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
