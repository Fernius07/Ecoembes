# Decisiones de Diseño - Prototipo 1 (Ecoembes)

En este documento resumimos las decisiones tomadas por el equipo durante el diseño e implementación del Prototipo 1 del Servidor Ecoembes.

## 1. Modelo de datos y reglas de negocio

1. **Ubicación y código postal de los contenedores**:
   - Para poder realizar tanto el alta de contenedores con su dirección física como la consulta del estado de los contenedores de una zona por código postal, la clase `Contenedor` y el DTO `CrearContenedorDTO` incluyen ambos campos: `direccion` y `codigoPostal`.

2. **Cálculo del nivel de llenado (`NivelLlenado`)**:
   - Se han definido tres estados en función del porcentaje de ocupación respecto a la capacidad inicial del contenedor:
     - `VERDE`: menos del 60 % de ocupación.
     - `NARANJA`: entre el 60 % y el 90 % de ocupación.
     - `ROJO`: más del 90 % de ocupación.
   - Cuando se actualiza el estado de un contenedor (`PUT /api/contenedores/{id}/estado`), si no se indica el nivel de llenado explícitamente, el servidor lo calcula aplicando estos umbrales.

3. **Conversión de envases a toneladas**:
   - Se toma un peso medio de `0,05 kg` (50 gramos) por envase (`PESO_ENVASE_KG = 0.05` en `ServicioAsignaciones`), por lo que `toneladas = envases * 0,05 / 1000`.

4. **Gestión de sesiones en memoria (`EstadoServidor`)**:
   - Al iniciar sesión (`POST /api/auth/login`), el servidor genera un token correspondiente al timestamp en milisegundos del momento del login y lo asocia al objeto `PersonalEcoembes` dentro de un mapa concurrente en `EstadoServidor` (patrón *StateManagement*).
   - Al cerrar sesión (`POST /api/auth/logout`), se elimina el token del mapa, quedando inválido para siguientes peticiones.

5. **Comunicación con plantas de reciclaje externas (`PasarelaPlanta`)**:
   - En este primer prototipo no existen aún los servidores externos independientes, por lo que `PasarelaPlasSB` y `PasarelaContSocket` simulan la consulta de capacidad y la recepción de notificaciones.
   - Ambas clases implementan la interfaz común `PasarelaPlanta` y se registran en `RegistroPasarelasPlanta`. De esta manera, `ServicioPlantas` y `ServicioAsignaciones` no dependen de ninguna planta concreta y es posible añadir nuevas plantas en el futuro sin modificar la lógica de negocio.
