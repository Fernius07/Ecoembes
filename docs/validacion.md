# Validación del Prototipo 1 en Swagger UI

Resumen de las pruebas realizadas desde la interfaz de Swagger UI (`http://localhost:8080/swagger-ui/index.html`) para comprobar todas las operaciones del Servidor Ecoembes.

| # | Operación | Datos de entrada | Resultado esperado | Resultado obtenido | Captura |
|---|---|---|---|---|---|
| 01 | `POST /api/auth/login` | `email: ane.garcia@ecoembes.com`, `contrasena: ecoembes123` | `200 OK` con token de sesión | `200 OK` | `capturas/01_login_ok.png` |
| 02 | `POST /api/auth/login` | Contraseña incorrecta | `401 Unauthorized` | `401 Unauthorized` | `capturas/02_login_error_401.png` |
| 03 | `GET /api/contenedores` | `token` activo | `200 OK` con la lista de contenedores | `200 OK` (9 contenedores) | `capturas/03_get_containers_ok.png` |
| 04 | `GET /api/contenedores` (por zona) | `codigoPostal=48001`, `fecha=2026-11-11` | `200 OK` con estados de la zona `48001` | `200 OK` (3 contenedores) | `capturas/04_get_containers_zone_ok.png` |
| 05 | `POST /api/contenedores` | Alta de `CONT-010` en CP `48009` | `201 Created` | `201 Created` | `capturas/05_create_container_ok.png` |
| 06 | `POST /api/contenedores` | Intento de alta con ID repetido `CONT-001` | `400 Bad Request` | `400 Bad Request` | `capturas/06_create_container_duplicado_400.png` |
| 07 | `GET /api/contenedores/{id}/uso` | `id=CONT-001`, del `2026-11-04` al `2026-11-11` | `200 OK` con historial entre fechas | `200 OK` | `capturas/07_get_container_usage_ok.png` |
| 08 | `GET /api/contenedores/{id}/uso` | Fechas invertidas (`fechaInicio > fechaFin`) | `400 Bad Request` | `400 Bad Request` | `capturas/08_get_container_usage_fechas_invalidas_400.png` |
| 09 | `PUT /api/contenedores/{id}/estado` | `id=CONT-010`, `envases=1600` | `200 OK` con cálculo de nivel `NARANJA` | `200 OK` | `capturas/09_update_container_status_ok.png` |
| 10 | `GET /api/plantas` | `token` activo | `200 OK` con `PlasSB Ltd.` y `ContSocket Ltd.` | `200 OK` | `capturas/10_get_plants_ok.png` |
| 11 | `GET /api/plantas/{id}/capacidad` | `id=PLASSB`, `fecha=2026-11-11` | `200 OK` con toneladas disponibles | `200 OK` (`1.5 t`) | `capturas/11_get_plant_capacity_ok.png` |
| 12 | `POST /api/asignaciones` | `PLASSB`, `["CONT-001", "CONT-004"]`, `2026-11-11` | `201 Created` con datos de auditoría | `201 Created` (`ASIG-001`) | `capturas/12_create_assignment_ok.png` |
| 13 | `POST /api/asignaciones` | `PLASSB`, `["CONT-003"]`, `2026-11-12` (capacidad `0.02 t`) | `400 Bad Request` por falta de capacidad | `400 Bad Request` | `capturas/13_create_assignment_capacidad_excedida_400.png` |
| 14 | `POST /api/auth/logout` | `token` activo | `204 No Content` | `204 No Content` | `capturas/14_logout_ok.png` |
| 15 | `GET /api/plantas` | Llamada con el token tras cerrar sesión | `401 Unauthorized` | `401 Unauthorized` | `capturas/15_llamada_tras_logout_401.png` |
