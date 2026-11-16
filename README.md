# Proyecto Ecoembes — Servidor Ecoembes (Prototipo 1)

Sistema distribuido para la gestión inteligente del reciclaje de envases desarrollado en la asignatura **Diseño de Software** (Facultad de Ingeniería — Universidad de Deusto).

- **Equipo:** `DS-05`
- **Integrantes:** Ian Anaya Abascal, Xabier Diaz Garcia, Iker Diaz Lazaro, Iñigo Fernandez Garcia

---

## 1. Descripción del Proyecto y Alcance del Prototipo 1

El sistema **Ecoembes** da soporte a la gestión de contenedores de recogida selectiva equipados con sensores **RecycloBin UltraEye** (que transmiten diariamente las toneladas estimadas y el nivel de llenado) y a la asignación de dichos contenedores a plantas de reciclaje externas (**PlasSB Ltd.** y **ContSocket Ltd.**), las cuales publican su capacidad disponible en toneladas con 10 días de antelación.

En este **Prototipo 1** se implementa la primera versión funcional del **Servidor Ecoembes**:
- **Almacenamiento en memoria:** toda la información del dominio (personal, contenedores, históricos diarios, capacidades de plantas y asignaciones) se gestiona en memoria sin base de datos, precargando un conjunto de datos de prueba al iniciar la aplicación (`InicializadorDatos`).
- **Simulación desacoplada de servicios externos:** la comunicación con las plantas `PlasSB Ltd.` y `ContSocket Ltd.` se simula en la capa de pasarelas (`com.ecoembes.externo`) tras una interfaz común (`PasarelaPlanta`), permitiendo incorporar nuevas plantas de reciclaje sin modificar los servicios de negocio.
- **Documentación interactiva con Swagger / OpenAPI:** todos los puntos de acceso del API REST están documentados y pueden probarse directamente desde la interfaz web de **Swagger UI**.

---

## 2. Arquitectura y Patrones de Diseño Aplicados

El servidor sigue una arquitectura en capas organizada bajo el paquete base `com.ecoembes`:

```text
com.ecoembes
 ├── fachada       -> EcoembesController (Patrón Fachada)
 ├── servicio      -> ServicioAutenticacion, ServicioContenedores, ServicioPlantas, ServicioAsignaciones (Patrón Servicio de Aplicación)
 ├── dto           -> Clases de transferencia de datos de entrada y salida del API REST (Patrón DTO)
 ├── ensamblador   -> EnsambladorDTO (Patrón Ensamblador: conversión entre objetos de dominio y DTOs)
 ├── estado        -> EstadoServidor (Patrón Gestión de Estado: gestión de sesiones y tokens activos en memoria)
 ├── externo       -> PasarelaPlanta, PasarelaPlasSB, PasarelaContSocket, RegistroPasarelasPlanta (Patrón Pasarela)
 ├── dominio       -> Contenedor, RegistroEstadoContenedor, PlantaReciclaje, CapacidadDiariaPlanta, PersonalEcoembes, Asignacion, NivelLlenado
 ├── repositorio   -> Interfaces de repositorio e implementaciones en memoria (*Memoria)
 ├── configuracion -> InicializadorDatos y ConfiguracionOpenApi
 └── excepcion     -> Excepciones de negocio y ManejadorGlobalExcepciones (@RestControllerAdvice)
```

- **Patrón Fachada (`com.ecoembes.fachada.EcoembesController`):** único punto de entrada REST del servidor bajo el prefijo `/api` que centraliza las operaciones y delega en la capa de servicios. Los nombres de sus métodos coinciden exactamente con los del diseño del API y los diagramas UML de clases y de secuencia.
- **Patrón Servicio de Aplicación (`com.ecoembes.servicio.*`):** encapsula la lógica de negocio, validaciones, cálculo de niveles de llenado y vaciado de contenedores según la fecha de asignación (`ServicioAutenticacion`, `ServicioContenedores`, `ServicioPlantas` y `ServicioAsignaciones`).
- **Patrón Objeto de Transferencia de Datos y Ensamblador (`com.ecoembes.dto.*` y `com.ecoembes.ensamblador.EnsambladorDTO`):** desacopla las entidades del dominio de las peticiones y respuestas JSON expuestas por el API REST.
- **Patrón Gestión de Estado (`com.ecoembes.estado.EstadoServidor`):** mantiene en memoria un mapa concurrente (`ConcurrentHashMap<String, PersonalEcoembes>`) con los tokens activos (generados a partir de la marca de tiempo en milisegundos del inicio de sesión) y el empleado autenticado.
- **Patrón Pasarela (`com.ecoembes.externo.PasarelaPlanta` y `RegistroPasarelasPlanta`):** abstrae y desacopla la comunicación con las plantas de reciclaje externas (`PasarelaPlasSB` y `PasarelaContSocket`) para garantizar la extensibilidad del sistema.

---

## 3. Decisiones de Diseño y Reglas de Negocio

1. **Ubicación, código postal y filtrado unificado en `GET /api/contenedores`**:
   - Para dar soporte tanto al alta de contenedores con su dirección física como a la consulta del estado de los contenedores de una zona por código postal, la entidad `Contenedor` y el DTO `CrearContenedorDTO` incluyen ambos atributos: `direccion` y `codigoPostal`.
   - En el API REST, `GET /api/contenedores` permite listar todos los contenedores (cuando se invoca únicamente con `token` mediante `obtenerContenedores`) o filtrar por zona y fecha pasando los parámetros de consulta `codigoPostal` y `fecha` (mediante `obtenerContenedoresPorZona`).

2. **Cálculo del nivel de llenado (`NivelLlenado`) y fuente única de verdad en `Contenedor.historial`**:
   - El enumerado `NivelLlenado` define los rangos porcentuales (`porcentajeMinimo` y `porcentajeMaximo`) y el método estático `calcularSegunPorcentaje(double)` respecto a la `capacidadInicial` en toneladas del contenedor:
     - `VERDE`: `[0.0 %, 60.0 %)` (`0.0` a `60.0`) de ocupación.
     - `NARANJA`: `[60.0 %, 90.0 %]` (`60.0` a `90.0`) de ocupación.
     - `ROJO`: `(90.0 %, 100.0 %]` (`90.0` a `100.0`) de ocupación.
   - Cuando el sensor *RecycloBin UltraEye* actualiza el estado diario de un contenedor (`PUT /api/contenedores/{id}/estado`), si no se indica el nivel de llenado explícitamente, el servidor lo calcula invocando `NivelLlenado.calcularSegunPorcentaje`.
   - Cada actualización diaria mediante `PUT /api/contenedores/{id}/estado` guarda un registro fechado (`RegistroEstadoContenedor`) en `Contenedor.historial` para la fecha del día (`LocalDate.now()`) de forma idempotente. Para evitar redundancias de estado, `Contenedor` no almacena campos separados para la lectura actual: `getToneladasActuales()` y `getNivelLlenado()` se obtienen directamente desde `obtenerUltimoEstado()` sobre `historial`, que alimenta igualmente la consulta de uso entre fechas (`GET /api/contenedores/{id}/uso`).

3. **Unidad única en toneladas (`double`)**:
   - Todo el modelo de dominio, los DTOs y las pasarelas externas trabajan directamente en **toneladas (`double`)**: la `capacidadInicial` de `Contenedor`, las `toneladas` de `RegistroEstadoContenedor` y la actualización de estado en `ActualizarEstadoDTO`.
   - Al crear una asignación, `toneladasEstimadas` se calcula sumando directamente `Contenedor::getToneladasActuales`, habiéndose eliminado la conversión previa desde envases (`PESO_ENVASE_KG`) y el campo redundante `totalEnvases`.

4. **Reducción de capacidad de la planta y vaciado según la fecha de asignación (`POST /api/asignaciones`)**:
   - Cada planta de reciclaje (`PlasSB Ltd.` y `ContSocket Ltd.`) publica su capacidad diaria en toneladas para un horizonte de 10 días desde la fecha actual (`CapacidadDiariaPlanta`).
   - Al ejecutar `POST /api/asignaciones`, el servicio valida que ningún contenedor de la lista esté ya asignado en esa fecha y que `toneladasEstimadas <= toneladasDisponibles` en la planta para la fecha solicitada.
   - Al confirmarse la asignación y notificarse a la planta mediante su pasarela (`PasarelaPlanta.notificarAsignacion(fecha, totalContenedores, toneladasEstimadas)`), se descuenta automáticamente `toneladasEstimadas` de las `toneladasDisponibles` de esa planta para esa fecha (`PlantaReciclaje.reducirCapacidad`). Si una asignación posterior intenta superar la capacidad restante de ese día, el servidor devuelve el error `400 Petición inválida`.
   - Además, el efecto sobre los contenedores depende de la fecha de asignación: si la asignación se realiza para el día actual (`LocalDate.now()`), los contenedores asignados se vacían inmediatamente (`Contenedor.vaciar(hoy)`, quedando registrados con `0.0` toneladas y nivel `VERDE`); si la fecha es futura, los contenedores no se vacían hoy y conservan su carga actual.

5. **Auditoría de asignaciones**:
   - Cada `Asignacion` registra tanto la `PlantaReciclaje` asignada como el objeto `PersonalEcoembes` obtenido a partir del token en `EstadoServidor`, exponiendo `emailPersonal` en `AsignacionDTO` para mantener la trazabilidad de quién realizó la asignación.

6. **Comunicación desacoplada con plantas de reciclaje externas (`PasarelaPlanta`)**:
   - En este primer prototipo no existen aún los servidores externos independientes, por lo que `PasarelaPlasSB` y `PasarelaContSocket` simulan la consulta y descuento de capacidad y el registro de notificaciones.
   - Ambas clases implementan la interfaz común `PasarelaPlanta` (que centraliza la lógica común de búsqueda y descuento de capacidad mediante métodos `default` apoyados en `PlantaReciclaje.consultarCapacidadDisponible` y `PlantaReciclaje.reducirCapacidad`) y se registran dinámicamente en `RegistroPasarelasPlanta`. De esta manera, `ServicioPlantas` y `ServicioAsignaciones` no dependen de ninguna planta concreta.

7. **Gestión de sesiones en memoria (`EstadoServidor`) y coherencia de nombres**:
   - Al iniciar sesión (`POST /api/auth/login`), el servidor genera un token correspondiente a la marca de tiempo en milisegundos y lo asocia al objeto `PersonalEcoembes` dentro de `EstadoServidor`. Al cerrar sesión (`POST /api/auth/logout`), se elimina el token del mapa y cualquier petición posterior con ese token devuelve `401 No autorizado`.
   - Todos los paquetes, clases, atributos, rutas del API REST y los diagramas UML están íntegramente en español, con nombres idénticos en las operaciones de `EcoembesController`, en esta documentación y en `docs/diagramas/`.

---

## 4. Diseño Detallado del API REST (`EcoembesController`)

Una vez iniciado el servidor, la aplicación escucha en el puerto `8080`:

- **Interfaz Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Especificación OpenAPI (JSON):** http://localhost:8080/v3/api-docs

### Operaciones del API REST

| Operación | HTTP | Ruta | Parámetros | Retorno | Códigos HTTP |
|---|---|---|---|---|---|
| `login` | `POST` | `/api/auth/login` | `LoginDTO (email, contrasena)` | `TokenDTO` | `200, 401` |
| `logout` | `POST` | `/api/auth/logout` | `token` | Vacío | `204, 401` |
| `crearContenedor` | `POST` | `/api/contenedores` | `token, CrearContenedorDTO (id, direccion, codigoPostal, capacidadInicial)` | `ContenedorDTO` | `201, 400, 401` |
| `obtenerContenedores` | `GET` | `/api/contenedores` | `token` | `List<ContenedorDTO>` | `200, 401` |
| `obtenerContenedoresPorZona` | `GET` | `/api/contenedores` | `token, codigoPostal, fecha` | `List<EstadoContenedorDTO>` | `200, 400, 401` |
| `obtenerUsoContenedor` | `GET` | `/api/contenedores/{id}/uso` | `id, token, fechaInicio, fechaFin` | `List<EstadoContenedorDTO>` | `200, 400, 401, 404` |
| `actualizarEstadoContenedor` | `PUT` | `/api/contenedores/{id}/estado` | `id, token, ActualizarEstadoDTO (toneladas, nivelLlenado)` | `EstadoContenedorDTO` | `200, 400, 401, 404` |
| `obtenerPlantas` | `GET` | `/api/plantas` | `token` | `List<PlantaDTO>` | `200, 401` |
| `obtenerCapacidadPlanta` | `GET` | `/api/plantas/{id}/capacidad` | `id, token, fecha` | `CapacidadPlantaDTO` | `200, 400, 401, 404` |
| `crearAsignacion` | `POST` | `/api/asignaciones` | `token, CrearAsignacionDTO (idPlanta, idsContenedores, fecha)` | `AsignacionDTO` | `201, 400, 401, 404` |

> **Nota de autenticación:** Todas las operaciones salvo `POST /api/auth/login` requieren pasar un `token` activo como parámetro de consulta (`?token=...`).

---

## 5. Diagramas UML

### 5.1. Diagrama de Clases UML

![Diagrama de Clases del Servidor Ecoembes (Prototipo 1)](docs/diagramas/Diagrama%20de%20Clases%20Ecoembes.png)

### 5.2. Diagramas de Secuencia UML

#### 1. Inicio de sesión (`login`)
![Diagrama de Secuencia: login](docs/diagramas/secuencia_01_login.jpg)

#### 2. Cierre de sesión (`logout`)
![Diagrama de Secuencia: logout](docs/diagramas/secuencia_02_logout.jpg)

#### 3. Creación de contenedor (`crearContenedor`)
![Diagrama de Secuencia: crearContenedor](docs/diagramas/secuencia_03_crearContenedor.jpg)

#### 4. Listado de contenedores (`obtenerContenedores`)
![Diagrama de Secuencia: obtenerContenedores](docs/diagramas/secuencia_04_obtenerContenedores.jpg)

#### 5. Consulta de contenedores por zona y fecha (`obtenerContenedoresPorZona`)
![Diagrama de Secuencia: obtenerContenedoresPorZona](docs/diagramas/secuencia_05_obtenerContenedoresPorZona.jpg)

#### 6. Histórico de uso de un contenedor (`obtenerUsoContenedor`)
![Diagrama de Secuencia: obtenerUsoContenedor](docs/diagramas/secuencia_06_obtenerUsoContenedor.jpg)

#### 7. Actualización de estado del contenedor (`actualizarEstadoContenedor`)
![Diagrama de Secuencia: actualizarEstadoContenedor](docs/diagramas/secuencia_07_actualizarEstadoContenedor.jpg)

#### 8. Listado de plantas de reciclaje (`obtenerPlantas`)
![Diagrama de Secuencia: obtenerPlantas](docs/diagramas/secuencia_08_obtenerPlantas.jpg)

#### 9. Consulta de capacidad disponible de planta (`obtenerCapacidadPlanta`)
![Diagrama de Secuencia: obtenerCapacidadPlanta](docs/diagramas/secuencia_09_obtenerCapacidadPlanta.jpg)

#### 10. Asignación de contenedores a planta (`crearAsignacion`)
![Diagrama de Secuencia: crearAsignacion](docs/diagramas/secuencia_10_crearAsignacion.jpg)

---

## 6. Requisitos Previos, Instalación, Configuración y Ejecución

### Requisitos previos

- **Kit de Desarrollo de Java (JDK):** versión **17**, **21 LTS** o **25** (el proyecto está configurado en `build.gradle` con compatibilidad de código Java 17).
- **Gradle:** no requiere instalación manual; el repositorio incluye el envoltorio de Gradle (`gradlew` y `gradlew.bat`, versión 9.1.0).
- **Entorno de desarrollo (opcional):** Eclipse IDE (con el complemento de integración Gradle Buildship), IntelliJ IDEA o VS Code.

### Opción A: Descarga, conexión con GitHub y configuración en Eclipse IDE

#### Paso 1. Obtener el proyecto (desde GitHub o archivo ZIP)

- **Opción 1 — Conectar y clonar directamente desde GitHub en Eclipse:**
  1. En Eclipse, abre **File → Import... → Git → Projects from Git**.
  2. Selecciona **Clone URI** y pulsa **Next**.
  3. En **URI**, introduce:
     ```text
     https://github.com/Fernius07/Ecoembes.git
     ```
     *(Si vas a subir cambios como colaborador del repositorio, introduce tu usuario de GitHub y tu token de acceso personal en el apartado de autenticación).*
  4. Pulsa **Next**, asegúrate de que la rama **`main`** está marcada, elige la carpeta local de destino y finaliza el clonado.

- **Opción 2 — Clonar por consola o descomprimir el ZIP de entrega:**
  - Si prefieres usar Git por terminal:
    ```bash
    git clone https://github.com/Fernius07/Ecoembes.git
    ```
  - Si dispones del archivo `.zip` de la entrega, descomprímelo en cualquier carpeta local de tu equipo.

#### Paso 2. Importar y configurar como Proyecto Gradle en Eclipse

1. En Eclipse, ve a **File → Import... → Gradle → Existing Gradle Project** y pulsa **Next**.
2. En **Project root directory**, haz clic en **Browse...** y selecciona la carpeta raíz del proyecto (`Ecoembes`). Pulsa **Next**.
3. En **Import Options**, mantén las opciones por defecto (**Gradle wrapper**) y pulsa **Finish**. Eclipse descargará las dependencias de Spring Boot y sincronizará el proyecto usando el JDK configurado.
   - *(Nota importante sobre el JDK en Eclipse)*: Si en **Window → Preferences → Java → Installed JREs** tienes marcado por defecto el JRE interno de Eclipse (`jre (default)` de `.p2\pool\plugins`), marca en su lugar tu JDK instalado (`jdk-17`, `jdk-21` o `jdk-25`) y pulsa **Apply and Close**.
4. *(Opcional)* Si en cualquier momento deseas recargar dependencias, haz clic derecho sobre el proyecto y selecciona **Gradle → Refresh Gradle Project**.

#### Paso 3. Ejecutar el servidor con Spring Boot y los tests desde Eclipse

- **Arrancar el Servidor Ecoembes con Spring Boot:**
  - **Desde *Gradle Tasks*:** abre **Window → Show View → Other... → Gradle → Gradle Tasks**, despliega la carpeta del proyecto → **`application`** y haz doble clic en **`bootRun`**.
  - **Desde Spring Tools:** clic derecho sobre la carpeta raíz del proyecto y **Run As → Spring Boot App**.
  - En la pestaña **Console** aparecerá el arranque en el puerto `8080` y el mensaje `Datos iniciales del Servidor Ecoembes cargados en memoria.`
- **Ejecutar las pruebas unitarias:**
  - Desde **Gradle Tasks → verification → `test`** (doble clic), o clic derecho sobre el proyecto → **Run As → JUnit Test**.

### Opción B: Ejecución desde línea de comandos (Terminal)

1. Clonar el repositorio (o situarse en la carpeta del proyecto):
   ```bash
   git clone https://github.com/Fernius07/Ecoembes.git
   cd Ecoembes
   ```
2. Compilar el proyecto y ejecutar las pruebas unitarias (JUnit 5):
   ```bash
   # En Windows (PowerShell / CMD):
   .\gradlew.bat build

   # En Linux / macOS:
   ./gradlew build
   ```
3. Iniciar el Servidor Ecoembes:
   ```bash
   # En Windows:
   .\gradlew.bat bootRun

   # En Linux / macOS:
   ./gradlew bootRun
   ```

---

## 7. Datos de Prueba Precargados (`InicializadorDatos`)

Al arrancar el servidor se cargan automáticamente en memoria los siguientes datos para facilitar las pruebas en Swagger UI:

### 7.1. Credenciales del Personal de Ecoembes

| ID | Nombre | Correo (`email`) | Contraseña (`contrasena`) |
|---|---|---|---|
| `EMP-01` | **Ian Anaya Abascal** | `ian.anaya@ecoembes.com` | `ecoembes123` |
| `EMP-02` | **Xabier Diaz Garcia** | `xabier.diaz@ecoembes.com` | `ecoembes123` |
| `EMP-03` | **Iker Diaz Lazaro** | `iker.diaz@ecoembes.com` | `ecoembes123` |
| `EMP-04` | **Iñigo Fernandez Garcia** | `inigo.fernandez@ecoembes.com` | `ecoembes123` |

### 7.2. Contenedores en Bilbao (con histórico de los últimos 8 días)

| ID | Dirección | Código Postal | Capacidad Inicial (t) | Toneladas Actuales (t) | Nivel de Llenado |
|---|---|---|---|---|---|
| `CONT-001` | Plaza Circular 1, Abando | `48001` | `0.10` | `0.0325` | `VERDE` |
| `CONT-002` | Calle Buenos Aires 12, Abando | `48001` | `0.10` | `0.0725` | `NARANJA` |
| `CONT-003` | Alameda Mazarredo 24, Abando | `48001` | `0.10` | `0.0960` | `ROJO` |
| `CONT-004` | Calle Hurtado de Amézaga 18 | `48002` | `0.125` | `0.0400` | `VERDE` |
| `CONT-005` | Calle Autonomía 10, Indautxu | `48002` | `0.125` | `0.0925` | `NARANJA` |
| `CONT-006` | Calle San Francisco 32, Bilbao La Vieja | `48003` | `0.09` | `0.0260` | `VERDE` |
| `CONT-007` | Plaza Corazón de María 5 | `48003` | `0.09` | `0.0855` | `ROJO` |
| `CONT-008` | Calle Santutxu 40, Santutxu | `48004` | `0.11` | `0.0450` | `VERDE` |
| `CONT-009` | Avenida Zumalacárregui 65, Begoña | `48004` | `0.11` | `0.0825` | `NARANJA` |

### 7.3. Plantas de Reciclaje (horizonte de 10 días desde la fecha actual)

| ID Planta | Nombre | Ubicación | Capacidad Diaria Habitual | Escenario de Capacidad Reducida (para pruebas de error `400`) |
|---|---|---|---|---|
| `PLASSB` | **PlasSB Ltd.** | Polígono Industrial Asua-Erandio, Bizkaia | `1,50 t` | Día `hoy + 1`: `0,02 t` |
| `CONTSOCKET` | **ContSocket Ltd.** | Parque Tecnológico de Zamudio, Bizkaia | `2,00 t` | Día `hoy`: `0,04 t` |

---

## 8. Validación del Prototipo 1 en Swagger UI

Resumen de las pruebas realizadas desde la interfaz de Swagger UI (`http://localhost:8080/swagger-ui/index.html`) para comprobar todas las operaciones del Servidor Ecoembes:

| # | Operación | Datos de entrada | Resultado esperado | Resultado obtenido | Captura |
|---|---|---|---|---|---|
| 01 | `POST /api/auth/login` | `email: ian.anaya@ecoembes.com`, `contrasena: ecoembes123` | `200 OK` con token de sesión | `200 OK` | `docs/capturas/01_login_ok.png` |
| 02 | `POST /api/auth/login` | Contraseña incorrecta | `401 No autorizado` | `401 No autorizado` | `docs/capturas/02_login_error_401.png` |
| 03 | `GET /api/contenedores` | `token` activo (sin filtros opcionales) | `200 OK` con la lista de contenedores | `200 OK` (9 contenedores) | `docs/capturas/03_get_containers_ok.png` |
| 04 | `GET /api/contenedores` (por zona) | `codigoPostal=48001`, `fecha=2026-11-11` | `200 OK` con estados de la zona `48001` | `200 OK` (3 contenedores) | `docs/capturas/04_get_containers_zone_ok.png` |
| 05 | `POST /api/contenedores` | Alta de `CONT-010` en CP `48009` | `201 Creado` | `201 Creado` | `docs/capturas/05_create_container_ok.png` |
| 06 | `POST /api/contenedores` | Intento de alta con ID repetido `CONT-001` | `400 Petición inválida` | `400 Petición inválida` | `docs/capturas/06_create_container_duplicado_400.png` |
| 07 | `GET /api/contenedores/{id}/uso` | `id=CONT-001`, del `2026-11-04` al `2026-11-11` | `200 OK` con historial entre fechas | `200 OK` | `docs/capturas/07_get_container_usage_ok.png` |
| 08 | `GET /api/contenedores/{id}/uso` | Fechas invertidas (`fechaInicio > fechaFin`) | `400 Petición inválida` | `400 Petición inválida` | `docs/capturas/08_get_container_usage_fechas_invalidas_400.png` |
| 09 | `PUT /api/contenedores/{id}/estado` | `id=CONT-010`, `toneladas=0.08` | `200 OK` con cálculo de nivel `NARANJA` y registro en el histórico del día | `200 OK` | `docs/capturas/09_update_container_status_ok.png` |
| 10 | `GET /api/plantas` | `token` activo | `200 OK` con `PlasSB Ltd.` y `ContSocket Ltd.` | `200 OK` | `docs/capturas/10_get_plants_ok.png` |
| 11 | `GET /api/plantas/{id}/capacidad` | `id=PLASSB`, `fecha=2026-11-11` | `200 OK` con toneladas disponibles | `200 OK` (`1.5 t`) | `docs/capturas/11_get_plant_capacity_ok.png` |
| 12 | `POST /api/asignaciones` | `PLASSB`, `["CONT-001", "CONT-004"]`, `2026-11-11` | `201 Creado` con datos de auditoría, cálculo de toneladas y descuento de capacidad en la planta | `201 Creado` (`ASIG-001`) | `docs/capturas/12_create_assignment_ok.png` |
| 13 | `POST /api/asignaciones` | `PLASSB`, `["CONT-003"]`, `2026-11-12` (capacidad `0.02 t`) | `400 Petición inválida` por falta de capacidad disponible en la planta | `400 Petición inválida` | `docs/capturas/13_create_assignment_capacidad_excedida_400.png` |
| 14 | `POST /api/auth/logout` | `token` activo | `204 Sin contenido` | `204 Sin contenido` | `docs/capturas/14_logout_ok.png` |
| 15 | `GET /api/plantas` | Llamada con el token tras cerrar sesión | `401 No autorizado` | `401 No autorizado` | `docs/capturas/15_llamada_tras_logout_401.png` |

### Evidencias de Validación (Capturas Swagger UI)

#### Captura 1: Inicio de sesión correcto (`200 OK`)
![Captura 1: Inicio de sesión correcto (200 OK)](docs/capturas/01_login_ok.png)

#### Captura 2: Inicio de sesión con contraseña incorrecta (`401 No autorizado`)
![Captura 2: Inicio de sesión con contraseña incorrecta (401 No autorizado)](docs/capturas/02_login_error_401.png)

#### Captura 3: Consulta general de contenedores sin filtros (`200 OK`)
![Captura 3: Consulta general de contenedores sin filtros (200 OK)](docs/capturas/03_get_containers_ok.png)

#### Captura 4: Consulta de contenedores filtrando por zona y fecha (`200 OK`)
![Captura 4: Consulta de contenedores filtrando por zona y fecha (200 OK)](docs/capturas/04_get_containers_zone_ok.png)

#### Captura 5: Creación de un nuevo contenedor (`201 Creado`)
![Captura 5: Creación de un nuevo contenedor (201 Creado)](docs/capturas/05_create_container_ok.png)

#### Captura 6: Error al crear un contenedor con identificador duplicado (`400 Petición inválida`)
![Captura 6: Error al crear un contenedor con identificador duplicado (400 Petición inválida)](docs/capturas/06_create_container_duplicado_400.png)

#### Captura 7: Consulta del histórico de uso de un contenedor entre fechas (`200 OK`)
![Captura 7: Consulta del histórico de uso de un contenedor entre fechas (200 OK)](docs/capturas/07_get_container_usage_ok.png)

#### Captura 8: Consulta de uso con rango de fechas inválido (`400 Petición inválida`)
![Captura 8: Consulta de uso con rango de fechas inválido (400 Petición inválida)](docs/capturas/08_get_container_usage_fechas_invalidas_400.png)

#### Captura 9: Actualización diaria del sensor del contenedor con registro en el histórico (`200 OK`)
![Captura 9: Actualización diaria del sensor del contenedor con registro en el histórico (200 OK)](docs/capturas/09_update_container_status_ok.png)

#### Captura 10: Listado de plantas de reciclaje (`200 OK`)
![Captura 10: Listado de plantas de reciclaje (200 OK)](docs/capturas/10_get_plants_ok.png)

#### Captura 11: Consulta de capacidad disponible de una planta (`200 OK`)
![Captura 11: Consulta de capacidad disponible de una planta (200 OK)](docs/capturas/11_get_plant_capacity_ok.png)

#### Captura 12: Asignación válida de contenedores a una planta y descuento de capacidad (`201 Creado`)
![Captura 12: Asignación válida de contenedores a una planta y descuento de capacidad (201 Creado)](docs/capturas/12_create_assignment_ok.png)

#### Captura 13: Error por capacidad disponible insuficiente en la planta (`400 Petición inválida`)
![Captura 13: Error por capacidad disponible insuficiente en la planta (400 Petición inválida)](docs/capturas/13_create_assignment_capacidad_excedida_400.png)

#### Captura 14: Cierre de sesión (`204 Sin contenido`)
![Captura 14: Cierre de sesión (204 Sin contenido)](docs/capturas/14_logout_ok.png)

#### Captura 15: Petición rechazada tras cerrar sesión (`401 No autorizado`)
![Captura 15: Petición rechazada tras cerrar sesión (401 No autorizado)](docs/capturas/15_llamada_tras_logout_401.png)

---

## 9. Reflexión acerca del uso de IA Generativa

- **Asignatura:** Diseño de Software (Universidad de Deusto)
- **Equipo:** `DS-05`
- **Referencia:** https://www.petraai.org

### 9.1. Herramientas y tareas concretas

| Herramienta | Tarea concreta en la que se ha utilizado |
|---|---|
| ChatGPT | Apoyo en el diseño e implementación del API REST, generación de parte del código Java con Spring Boot y resolución de dudas relacionadas con la estructura de los controladores, servicios y DTO. También se ha utilizado para consultar la configuración de Swagger y documentar los endpoints. |
| Claude | Generación y revisión de fragmentos de código Java, especialmente relacionados con la lógica de los servicios y la organización de las clases. También se ha utilizado para contrastar las soluciones y respuestas obtenidas mediante ChatGPT, buscando posibles errores y alternativas de implementación. |
| ChatGPT y Claude | Comparación de las propuestas de ambas herramientas para comprobar si las soluciones planteadas eran coherentes con los requisitos del proyecto, los patrones de diseño utilizados y la arquitectura del Servidor Ecoembes. |

### 9.2. Aportaciones positivas

- **ChatGPT:** Nos ha permitido agilizar el desarrollo del proyecto mediante la generación de fragmentos de código y la resolución de dudas puntuales sobre Java y Spring Boot. También nos ha ayudado a comprender mejor determinados conceptos relacionados con la implementación de los controladores REST, los servicios y la documentación mediante Swagger.
- **Claude:** Nos ha aportado una segunda perspectiva sobre las soluciones propuestas, permitiéndonos contrastar el código generado por ChatGPT y valorar diferentes formas de implementar determinadas funcionalidades. Esto nos ha resultado especialmente útil para revisar la organización de las clases y la lógica de las operaciones del servidor.
- **Uso combinado de ambas herramientas:** Una de las principales ventajas ha sido poder comparar las respuestas de ChatGPT y Claude. De esta manera, no hemos dependido exclusivamente de una única herramienta, sino que hemos podido analizar distintas propuestas, detectar posibles inconsistencias y elegir aquellas soluciones que mejor se adaptaban a nuestro proyecto.

En general, ambas herramientas nos han permitido ahorrar tiempo en determinadas tareas y avanzar con mayor facilidad en aspectos técnicos que nos generaban dudas. No obstante, el desarrollo no se ha basado únicamente en la IA, ya que hemos tenido que analizar, adaptar y comprobar las propuestas para integrarlas correctamente en nuestra implementación.

### 9.3. Aspectos críticos

- **ChatGPT:** En algunas ocasiones, el código generado no se ajustaba completamente a la estructura de nuestro proyecto o a los requisitos específicos del enunciado. Esto nos ha obligado a revisar y modificar las propuestas, ya que incorporar directamente el código podía provocar incompatibilidades entre clases o problemas en la lógica de negocio.
- **Claude:** Aunque nos ha servido para contrastar información y código, sus respuestas tampoco han sido siempre completamente precisas. En ocasiones proponía soluciones diferentes a las que ya teníamos implementadas, por lo que era necesario analizar cuál encajaba mejor con nuestra arquitectura y comprobar que no introdujera errores.
- **Uso combinado de ambas herramientas:** Un aspecto que consideramos importante es que obtener una misma respuesta de dos herramientas diferentes no garantiza que sea correcta. Por ello, hemos tenido que mantener una actitud crítica y no aceptar automáticamente sus propuestas. También somos conscientes de que utilizar código generado por IA sin comprenderlo completamente podría perjudicar nuestro aprendizaje y dificultar la detección de errores posteriores.

Además, hemos comprobado que ninguna de las herramientas conoce por completo el contexto de nuestro proyecto, por lo que ha sido necesario proporcionarles información y revisar que sus respuestas fueran coherentes con los diagramas UML, los patrones de diseño y las funcionalidades solicitadas.

### 9.4. Motivaciones

La principal motivación para recurrir a ChatGPT y Claude ha sido facilitar determinadas tareas del desarrollo y disponer de herramientas adicionales para resolver dudas técnicas.

Al trabajar con Java, Spring Boot y Gradle, nos hemos encontrado con aspectos de implementación que requerían consultas y explicaciones adicionales. Por ello, hemos utilizado estas herramientas para obtener orientación, generar algunos fragmentos de código y comprender mejor cómo desarrollar determinadas funcionalidades.

Otra motivación importante ha sido contrastar la información entre ambas herramientas. Consideramos que consultar diferentes perspectivas nos permite tener una visión más amplia de los problemas y evitar aceptar una solución sin haber valorado otras alternativas.

También hemos recurrido a ellas para agilizar tareas que podían resultar repetitivas y dedicar más tiempo al análisis de los requisitos, al diseño de la arquitectura y a la comprobación del funcionamiento del servidor mediante Swagger.

En cualquier caso, nuestra intención no ha sido delegar todo el proyecto en la Inteligencia Artificial. Hemos participado activamente en el diseño, la toma de decisiones, la adaptación del código y la validación de las funcionalidades implementadas. Consideramos que la IA debe ser una herramienta de apoyo y no un sustituto de nuestro propio trabajo y aprendizaje.

---

## 10. Memoria de Entrega

- [`docs/Entrega_Prototipo1.docx`](docs/Entrega_Prototipo1.docx): Documento Word completo de entrega del Prototipo 1 (API REST, arquitectura, patrones, decisiones de diseño, diagrama de clases UML, validación en Swagger UI y reflexión sobre IA Generativa).
