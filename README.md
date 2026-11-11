# Proyecto Ecoembes — Servidor Ecoembes (Prototipo 1)

Sistema distribuido para la gestión inteligente del reciclaje de envases desarrollado en la asignatura **Diseño de Software** (Facultad de Ingeniería — Universidad de Deusto).

---

## 1. Descripción del Proyecto y Alcance del Prototipo 1

El sistema **Ecoembes** da soporte a la gestión de contenedores de recogida selectiva equipados con sensores **RecycloBin UltraEye** (que transmiten diariamente el número estimado de envases y el nivel de llenado) y a la asignación de dichos contenedores a plantas de reciclaje externas (**PlasSB Ltd.** y **ContSocket Ltd.**), las cuales publican su capacidad disponible en toneladas con 10 días de antelación.

En este **Prototipo 1** se implementa la primera versión funcional del **Servidor Ecoembes**:
- **Almacenamiento en memoria:** toda la información del dominio (personal, contenedores, históricos diarios, capacidades de plantas y asignaciones) se gestiona en memoria sin base de datos, precargando un conjunto de datos de prueba al iniciar la aplicación (`InicializadorDatos`).
- **Simulación desacoplada de servicios externos:** la comunicación con las plantas `PlasSB Ltd.` y `ContSocket Ltd.` se simula en la capa de pasarelas (`com.ecoembes.externo`) tras una interfaz común (`PasarelaPlanta`), permitiendo incorporar nuevas plantas de reciclaje sin modificar los servicios de negocio.
- **Documentación interactiva con Swagger / OpenAPI:** todos los endpoints del API REST están documentados y pueden probarse directamente desde **Swagger UI**.

---

## 2. Arquitectura y Patrones de Diseño Aplicados

El servidor sigue una arquitectura en capas organizada bajo el paquete base `com.ecoembes`:

```text
com.ecoembes
 ├── fachada       -> EcoembesController (Patrón Façade)
 ├── servicio      -> ServicioAutenticacion, ServicioContenedores, ServicioPlantas, ServicioAsignaciones (Patrón AppService)
 ├── dto           -> Records de entrada y salida del API REST (Patrón DTO)
 ├── ensamblador   -> EnsambladorDTO (Conversión entre objetos de dominio y DTOs)
 ├── estado        -> EstadoServidor (Patrón StateManagement: gestión de sesiones y tokens activos en memoria)
 ├── externo       -> PasarelaPlanta, PasarelaPlasSB, PasarelaContSocket, RegistroPasarelasPlanta (Patrón Gateway)
 ├── dominio       -> Contenedor, RegistroEstadoContenedor, PlantaReciclaje, CapacidadDiariaPlanta, PersonalEcoembes, Asignacion, NivelLlenado
 ├── repositorio   -> Interfaces de repositorio e implementaciones en memoria (*Memoria)
 ├── configuracion -> InicializadorDatos y ConfiguracionOpenApi
 └── excepcion     -> Excepciones de negocio y ManejadorGlobalExcepciones (@RestControllerAdvice)
```

- **Patrón *Façade* (`EcoembesController`):** único punto de entrada REST del servidor que centraliza las operaciones del API y delega en la capa de servicios.
- **Patrón *Application Service* (`com.ecoembes.servicio`):** encapsula toda la lógica de negocio, validaciones, cálculo de niveles de llenado y conversión de envases a toneladas.
- **Patrón *DTO* y *Assembler* (`com.ecoembes.dto`, `EnsambladorDTO`):** desacopla el modelo de dominio interno de los contratos JSON expuestos a los clientes.
- **Patrón *State Management* (`EstadoServidor`):** mantiene en memoria un mapa concurrente (`ConcurrentHashMap<String, PersonalEcoembes>`) con los tokens activos (generados a partir del timestamp en milisegundos del inicio de sesión) y el empleado asociado.
- **Patrón *Gateway* (`PasarelaPlanta`, `RegistroPasarelasPlanta`):** aísla el acceso a las plantas de reciclaje externas para garantizar la extensibilidad del sistema.

---

## 3. Requisitos Previos

- **Java Development Kit (JDK):** versión **17** o **21 LTS** (el proyecto está configurado en `build.gradle` con compatibilidad Java 17 mediante `options.release = 17`).
- **Gradle:** no requiere instalación manual; el repositorio incluye **Gradle Wrapper** (`gradlew` y `gradlew.bat`, versión 8.10.2).
- **IDE (opcional):** Eclipse IDE (con plugin *Buildship Gradle Integration*), IntelliJ IDEA o VS Code.

---

## 4. Instalación, Configuración y Ejecución

A continuación se detallan las dos formas de obtener, configurar y ejecutar el proyecto, tanto desde **Eclipse IDE** (recomendado para compañeros del equipo y evaluación del profesor) como desde la **terminal**.

### Opción A: Descarga, conexión con GitHub y configuración en Eclipse IDE

#### Paso 1. Obtener el proyecto (desde GitHub o archivo ZIP)

Puedes obtener el código fuente de cualquiera de estas dos maneras:

- **Opción 1 — Conectar y clonar directamente desde GitHub en Eclipse (recomendado para compañeros):**
  1. En Eclipse, abre la perspectiva de Git desde **Window → Perspective → Open Perspective → Other... → Git** (o ve directamente a **File → Import... → Git → Projects from Git (with smart import)**).
  2. Selecciona **Clone URI** y pulsa **Next**.
  3. En el campo **URI**, introduce la dirección del repositorio:
     ```text
     https://github.com/Fernius07/Ecoembes.git
     ```
     *(Los campos Host y Repository path se rellenarán automáticamente; si vas a hacer push como colaborador, introduce tu usuario de GitHub y tu Personal Access Token en Authentication).*
  4. Pulsa **Next**, asegúrate de que la rama **`main`** está marcada, elige la carpeta local de destino donde se guardará el repositorio y finaliza el clonado (**Cancel** si te pide importar como proyecto general, ya que en el Paso 2 lo importaremos como proyecto Gradle para que configure todas las dependencias).

- **Opción 2 — Clonar por consola o descomprimir el ZIP de entrega:**
  - Si prefieres usar Git por terminal:
    ```bash
    git clone https://github.com/Fernius07/Ecoembes.git
    ```
  - Si dispones del archivo `.zip` de la entrega, descomprímelo en cualquier carpeta local de tu equipo.

#### Paso 2. Importar y configurar como Proyecto Gradle en Eclipse

1. En Eclipse, ve a **File → Import... → Gradle → Existing Gradle Project** y pulsa **Next** (si aparece la pantalla de bienvenida de Buildship, pulsa **Next** de nuevo).
2. En **Project root directory**, haz clic en **Browse...** y selecciona la carpeta raíz del proyecto que acabas de clonar o descomprimir (`Ecoembes` o `ecoembes-prototipo1`). Pulsa **Next**.
3. En la ventana **Import Options**:
   - Marca la casilla **Override workspace settings**.
   - Asegúrate de que está seleccionado **Gradle wrapper** (para usar la versión `8.10.2` incluida en el proyecto).
   - En **Java home** (dentro de *Advanced Options*), selecciona la ruta de un **JDK 17 o JDK 21 LTS** instalado en tu equipo (por ejemplo, `C:\Program Files\Eclipse Adoptium\jdk-21...`). *Nota: Esto evita problemas si tu instalación de Eclipse trae por defecto un JRE interno experimental superior (como Java 27) aún no soportado por Gradle 8.*
4. Pulsa **Finish** y espera unos segundos a que Eclipse descargue las dependencias de Spring Boot y sincronice el proyecto.
5. *(Opcional)* Si tras importar deseas forzar la actualización de dependencias en cualquier momento, haz clic derecho sobre el proyecto en el *Package Explorer* y selecciona **Gradle → Refresh Gradle Project**.

#### Paso 3. Ejecutar el servidor y los tests desde Eclipse

- **Arrancar el Servidor Ecoembes:**
  1. En el *Package Explorer*, despliega `src/main/java` → paquete `com.ecoembes`.
  2. Haz clic derecho sobre [`EcoembesApplication.java`](src/main/java/com/ecoembes/EcoembesApplication.java) y selecciona **Run As → Java Application** (o **Spring Boot App**).
  3. En la pestaña **Console** de Eclipse verás el arranque de Spring Boot en el puerto `8080` y el mensaje `Datos iniciales del Servidor Ecoembes cargados en memoria.`
- **Ejecutar las pruebas unitarias (JUnit 5):**
  1. Haz clic derecho sobre la carpeta `src/test/java` (o sobre el proyecto).
  2. Selecciona **Run As → JUnit Test** para verificar que todos los casos de prueba pasan en verde.

---

### Opción B: Ejecución desde línea de comandos (Terminal)

1. Clonar el repositorio (o situarse en la carpeta descomprimida del proyecto):
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

## 5. Uso y Validación del API REST en Swagger UI

Una vez iniciado el servidor, la aplicación escucha en el puerto `8080`:

- **Interfaz Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Especificación OpenAPI (JSON):** http://localhost:8080/v3/api-docs

### Resumen de Endpoints del API REST

| Método HTTP | Ruta | Operación en Fachada | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | `login` | Autentica a un empleado con email y contraseña y devuelve un `token` (timestamp en ms). |
| `POST` | `/api/auth/logout?token=` | `logout` | Cierra la sesión activa eliminando el `token` del estado del servidor. |
| `POST` | `/api/contenedores?token=` | `crearContenedor` | Registra un nuevo contenedor con nivel inicial `VERDE` y `0` envases. |
| `GET` | `/api/contenedores?token=` | `obtenerContenedores` | Devuelve el listado completo de contenedores registrados y su estado actual. |
| `GET` | `/api/contenedores?token=&codigoPostal=&fecha=` | `obtenerContenedoresPorZona` | Consulta el estado de los contenedores de un código postal en una fecha concreta. |
| `GET` | `/api/contenedores/{id}/uso?token=&fechaInicio=&fechaFin=` | `obtenerUsoContenedor` | Obtiene el histórico de estados de un contenedor entre dos fechas. |
| `PUT` | `/api/contenedores/{id}/estado?token=` | `actualizarEstadoContenedor` | Simula la lectura diaria del sensor *RecycloBin UltraEye* (calcula el nivel si se omite). |
| `GET` | `/api/plantas?token=` | `obtenerPlantas` | Lista las plantas de reciclaje disponibles en el sistema. |
| `GET` | `/api/plantas/{id}/capacidad?token=&fecha=` | `obtenerCapacidadPlanta` | Consulta la capacidad disponible (en toneladas) de una planta para una fecha dada. |
| `POST` | `/api/asignaciones?token=` | `crearAsignacion` | Asigna contenedores a una planta validando capacidad disponible, notifica a la planta y registra auditoría del empleado. |

> **Nota de autenticación:** Todos los endpoints salvo `POST /api/auth/login` requieren pasar un `token` activo como parámetro de consulta (`?token=...`).

---

## 6. Datos de Prueba Precargados (`InicializadorDatos`)

Al arrancar el servidor se cargan automáticamente en memoria los siguientes datos para facilitar las pruebas en Swagger UI:

### 6.1. Credenciales del Personal de Ecoembes

| ID | Nombre | Email (`email`) | Contraseña (`contrasena`) |
|---|---|---|---|
| `EMP-01` | **Ian Anaya Abascal** | `ian.anaya@ecoembes.com` | `ecoembes123` |
| `EMP-02` | **Xabier Diaz Garcia** | `xabier.diaz@ecoembes.com` | `ecoembes123` |
| `EMP-03` | **Iker Diaz Lazaro** | `iker.diaz@ecoembes.com` | `ecoembes123` |
| `EMP-04` | **Iñigo Fernandez Garcia** | `inigo.fernandez@ecoembes.com` | `ecoembes123` |

### 6.2. Contenedores en Bilbao (con histórico de los últimos 8 días)

| ID | Dirección | Código Postal | Capacidad Inicial | Envases Actuales | Nivel de Llenado |
|---|---|---|---|---|---|
| `CONT-001` | Plaza Circular 1, Abando | `48001` | `2000` | `650` | `VERDE` |
| `CONT-002` | Calle Buenos Aires 12, Abando | `48001` | `2000` | `1450` | `NARANJA` |
| `CONT-003` | Alameda Mazarredo 24, Abando | `48001` | `2000` | `1920` | `ROJO` |
| `CONT-004` | Calle Hurtado de Amézaga 18 | `48002` | `2500` | `800` | `VERDE` |
| `CONT-005` | Calle Autonomía 10, Indautxu | `48002` | `2500` | `1850` | `NARANJA` |
| `CONT-006` | Calle San Francisco 32, Bilbao La Vieja | `48003` | `1800` | `520` | `VERDE` |
| `CONT-007` | Plaza Corazón de María 5 | `48003` | `1800` | `1710` | `ROJO` |
| `CONT-008` | Calle Santutxu 40, Santutxu | `48004` | `2200` | `900` | `VERDE` |
| `CONT-009` | Avenida Zumalacárregui 65, Begoña | `48004` | `2200` | `1650` | `NARANJA` |

- **Umbrales de llenado:** `VERDE` (`< 60 %`), `NARANJA` (`60 % – 90 %`), `ROJO` (`> 90 %`).
- **Conversión de peso:** `0,05 kg` por envase (`toneladas = envases * 0,05 / 1000`).

### 6.3. Plantas de Reciclaje (horizonte de 10 días desde la fecha actual)

| ID Planta | Nombre | Ubicación | Capacidad Diaria Habitual | Escenario de Capacidad Reducida (para pruebas de error `400`) |
|---|---|---|---|---|
| `PLASSB` | **PlasSB Ltd.** | Polígono Industrial Asua-Erandio, Bizkaia | `1,50 t` | Día `hoy + 1`: `0,02 t` (máx. 400 envases) |
| `CONTSOCKET` | **ContSocket Ltd.** | Parque Tecnológico de Zamudio, Bizkaia | `2,00 t` | Día `hoy`: `0,04 t` (máx. 800 envases) |

---

## 7. Documentación Adicional en `/docs`

- [`docs/Entrega_Prototipo1.docx`](docs/Entrega_Prototipo1.docx): Memoria completa de entrega del Prototipo 1.
- [`docs/diseno_api.md`](docs/diseno_api.md): Especificación detallada del API REST y patrones de diseño.
- [`docs/decisiones.md`](docs/decisiones.md): Decisiones de diseño y reglas de negocio adoptadas.
- [`docs/validacion.md`](docs/validacion.md): Tabla de pruebas de validación en Swagger UI y capturas en `docs/capturas/`.
- `docs/diagramas/`: Diagramas UML de clases y de secuencia en formato PNG, PlantUML (`.puml`) y XMI para Visual Paradigm (`.xmi`).
- [`docs/Reflexion_IA.md`](docs/Reflexion_IA.md): Documento de reflexión sobre el uso de herramientas de IA Generativa.
