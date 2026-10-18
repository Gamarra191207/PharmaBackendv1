# Matriz de Pruebas de Seguridad — Sesión 10

**Proyecto:** PharmaSoft Backend (`PharmaBackend`)  
**Módulo:** Seguridad, Autenticación (JWT), Autorización (RBAC) y Gestión de Usuarios  
**Branch de trabajo:** `feature/seguridad-pruebas-gamarra`  
**Fecha de ejecución:** 18 de octubre de 2026  
**Ambiente:** Oracle Database 23ai Free (Docker), Spring Boot 3.4.x, Java 21 LTS  

---

## 1. Resumen Ejecutivo de Ejecución

| Métrica | Valor |
| :--- | :--- |
| **Total de Casos Ejecutados** | **28** |
| **Casos Base de Seguridad (Parte A)** | 16 (`A-01` a `A-04`, `T-01` a `T-05`, `P-01` a `P-04`, `U-01` a `U-02`, `C-01`) |
| **Casos Propios Adicionales (Parte A)** | 4 (`A-05`, `T-06`, `P-05`, `U-03`) |
| **Casos de Endpoints de Gestión (Parte B: B1, B2, B3)** | 8 (`B-01` a `B-08`) |
| **Casos Exitosos (Pasa)** | **28 (100%)** |
| **Casos Fallidos (Falla)** | **0 (0%)** |
| **Tasa de Cobertura de Seguridad** | **100%** |

---

## 2. Matriz Consolidada de Pruebas de Seguridad y Gestión

| ID | Grupo | Usuario / token | Petición | Resultado esperado (código y mensaje) | Resultado obtenido | Estado (Pasa / Falla) | Evidencia |
| :--- | :--- | :--- | :--- | :--- | :--- | :---: | :--- |
| **A-01** | Autenticación | `admin`, `vendedor`, `almacen`, `supervisor` | `POST /api/v1/auth/login` | 200 OK; cada respuesta trae sus roles y sus permisos; la supervisora, la unión de VENDEDOR y ALMACENERO. | 200 OK. Admin: roles=[ROLE_ADMIN] (8 perm); Vendedor: roles=[ROLE_VENDEDOR] (4 perm); Almacen: roles=[ROLE_ALMACENERO] (3 perm); Supervisor: roles=[ROLE_SUPERVISOR] (7 perm, unión vendedor y almacenero). | **Pasa** | `evidencias/A-01_login_roles.png` |
| **A-02** | Autenticación | Usuario inexistente / Clave incorrecta | `POST /api/v1/auth/login` | 401 Unauthorized con el mismo mensaje en ambos casos («Credenciales inválidas»). | 401 Unauthorized; `{"message": "Credenciales inválidas"}` en ambos casos, sin revelar si el usuario existe. | **Pasa** | `evidencias/A-02_login_invalido.png` |
| **A-03** | Autenticación | `inactivo` (cuenta con `activo=false`) | `POST /api/v1/auth/login` | 401 Unauthorized; el mensaje no revela que la cuenta existe ni su estado. | 401 Unauthorized; `{"message": "Credenciales inválidas"}`. La respuesta es indistinguible de una clave incorrecta. | **Pasa** | `evidencias/A-03_login_inactivo.png` |
| **A-04** | Autenticación | `VENDEDOR` (mayúsculas) | `POST /api/v1/auth/login` | 200 OK; `findByUsernameIgnoreCase` permite login sin distinguir mayúsculas/minúsculas. | 200 OK. Inicio de sesión exitoso, token JWT generado correctamente y authorities cargadas. | **Pasa** | `evidencias/A-04_login_case_insensitive.png` |
| **T-01** | Token | `vendedor` | `GET /api/v1/productos` (sin prefijo Bearer) | 401 Unauthorized «Debe iniciar sesión para acceder a este recurso». | 401 Unauthorized; `{"message": "Debe iniciar sesión para acceder a este recurso"}`. Filtro JWT exige esquema Bearer. | **Pasa** | `evidencias/T-01_sin_prefijo_bearer.png` |
| **T-02** | Token | `vendedor` | `GET /api/v1/productos` (carácter alterado en payload) | 401 Unauthorized «El token no es válido». | 401 Unauthorized; `{"message": "El token no es válido"}`. Detección de firma criptográfica HMAC manipulada. | **Pasa** | `evidencias/T-02_token_alterado.png` |
| **T-03** | Token | Token de otro secreto | `GET /api/v1/productos` (firmado con clave externa) | 401 Unauthorized «El token no es válido». | 401 Unauthorized; `{"message": "El token no es válido"}`. Validación de SignatureException por JJWT. | **Pasa** | `evidencias/T-03_token_otro_secreto.png` |
| **T-04** | Token | Token vencido | `GET /api/v1/productos` (exp en el pasado) | 401 Unauthorized «El token ha expirado. Inicie sesión nuevamente». | 401 Unauthorized; `{"message": "El token ha expirado. Inicie sesión nuevamente"}`. Atrapado por ExpiredJwtException. | **Pasa** | `evidencias/T-04_token_expirado.png` |
| **T-05** | Token | `vendedor` | Decodificación payload Base64 (`atob`) | Datos correctos; ningún dato sensible en el payload. | 200 OK. Claims válidos: sub, authorities, iat, exp. Sin contraseñas, hashes, ni datos sensibles en texto claro. | **Pasa** | `evidencias/T-05_jwt_payload_inspeccion.png` |
| **P-01** | Permisos | `vendedor` | `POST`, `PUT` y `DELETE` de `/api/v1/categorias` | 403 Forbidden en los tres endpoints. | 403 Forbidden en POST, PUT y DELETE. `{"message": "No tiene permisos para realizar esta operación"}`. | **Pasa** | `evidencias/P-01_vendedor_modificar_categoria.png` |
| **P-02** | Permisos | `almacen` | `GET /api/v1/clientes` y `POST /api/v1/ventas` | 403 Forbidden en ambos endpoints. | 403 Forbidden en GET /clientes y POST /ventas. Restricción por authorities CLIENTES_GESTIONAR y VENTAS_REGISTRAR. | **Pasa** | `evidencias/P-02_almacen_clientes_ventas.png` |
| **P-03** | Permisos | `vendedor` y `almacen` | `GET /api/v1/reportes/productos-mas-vendidos` | 403 Forbidden (rechazo por `@PreAuthorize`). | 403 Forbidden en ambos usuarios. Acceso bloqueado a nivel método de controlador por falta de `REPORTES_VER`. | **Pasa** | `evidencias/P-03_preauthorize_reportes.png` |
| **P-04** | Permisos | `admin` | Petición a cada módulo: productos, clientes, ventas, reportes, usuarios | 2xx en todas las peticiones (200 OK). | 200 OK en las 5 peticiones (`/productos`: 200, `/clientes`: 200, `/ventas`: 200, `/reportes/*`: 200, `/usuarios`: 200). | **Pasa** | `evidencias/P-04_admin_acceso_total.png` |
| **U-01** | Usuarios | `admin` | `POST /api/v1/usuarios` (clave sin número y roles vacío) | 400 Bad Request en ambos (número obligatorio en la contraseña; al menos un rol). | 400 Bad Request en ambos intentos. Validación Bean Validation `@Pattern` y `@NotEmpty` activada. | **Pasa** | `evidencias/U-01_validacion_password_roles.png` |
| **U-02** | Usuarios | `admin` | `POST /api/v1/usuarios` (empleado repetido y alta de María Huamán) | 409 Conflict (relación 1 a 1) y luego 201 Created; la respuesta no trae contraseña, password guarda `$2a$10$...` y hay fila nueva en `usuario_roles`. | 409 Conflict («El empleado ya tiene un usuario asignado»); Alta: 201 Created sin contraseña en DTO y persistencia segura con BCrypt. | **Pasa** | `evidencias/U-02_usuario_unicidad_alta.png` |
| **C-01** | CORS | SPA (`Origin: http://localhost:4200`) | `OPTIONS /api/v1/productos` | La verificación responde 200 y la petición real 200 o 401, nunca un error de CORS. | 200 OK. Header `Access-Control-Allow-Origin: http://localhost:4200` y métodos permitidos (GET, POST, PUT, DELETE, OPTIONS). | **Pasa** | `evidencias/C-01_cors_preflight.png` |
| **A-05** | Autenticación (Propio) | `  vendedor  ` (con espacios) | `POST /api/v1/auth/login` | 200 OK; normalización con `.trim()` permite autenticación válida sin rechazar por espacios accidentales. | 200 OK. Token generado exitosamente tras sanear el username en el request. | **Pasa** | `evidencias/A-05_login_trim_espacios.png` |
| **T-06** | Token (Propio) | Sin encabezado `Authorization` | `GET /api/v1/auth/me` | 401 Unauthorized «Debe iniciar sesión para acceder a este recurso». | 401 Unauthorized; filtro de seguridad intercepta la petición anónima al endpoint protegido del perfil. | **Pasa** | `evidencias/T-06_auth_me_sin_token.png` |
| **P-05** | Permisos (Propio) | `almacen` | `DELETE /api/v1/clientes/1` | 403 Forbidden «No tiene permisos para realizar esta operación». | 403 Forbidden; el rol ALMACENERO carece de autorización para eliminar registros de clientes. | **Pasa** | `evidencias/P-05_almacen_delete_cliente.png` |
| **U-03** | Usuarios (Propio) | `admin` | `POST /api/v1/usuarios` (con `empleadoId: 999` inexistente) | 404 Not Found «Empleado no encontrado con id: 999». | 404 Not Found; `{"message": "Empleado no encontrado con id: 999"}`. Verificación de existencia previa a la creación. | **Pasa** | `evidencias/U-03_usuario_empleado_inexistente.png` |
| **B-01** | Contraseña (B1) | `vendedor` | `PUT /api/v1/auth/password` (clave actual válida y nueva correcta) | 204 No Content; actualización segura con BCrypt y log INFO sin contraseña. | 204 No Content. Contraseña persistida en hash BCrypt; log registra auditoría sin exponer claves. | **Pasa** | `evidencias/B-01_cambio_password_exito.png` |
| **B-02** | Contraseña (B1) | `vendedor` | `PUT /api/v1/auth/password` (clave actual errónea) | 409 Conflict «La contraseña actual no es correcta». | 409 Conflict; `{"message": "La contraseña actual no es correcta"}`. | **Pasa** | `evidencias/B-02_password_actual_incorrecta.png` |
| **B-03** | Contraseña (B1) | `vendedor` | `PUT /api/v1/auth/password` (clave nueva igual a la actual) | 409 Conflict «La nueva contraseña debe ser distinta de la actual». | 409 Conflict; `{"message": "La nueva contraseña debe ser distinta de la actual"}`. | **Pasa** | `evidencias/B-03_password_nueva_igual_actual.png` |
| **B-04** | Contraseña (B1) | `vendedor` | `PUT /api/v1/auth/password` (clave nueva corta o sin números) | 400 Bad Request por validación `@Size` y `@Pattern`. | 400 Bad Request; `{"validationErrors": {"passwordNueva": "La contraseña debe tener entre 8 y 72 caracteres"}}`. | **Pasa** | `evidencias/B-04_password_nueva_invalida.png` |
| **B-05** | Roles (B2) | `admin` | `PUT /api/v1/usuarios/2/roles` (`rolIds: []` vacío) | 400 Bad Request «Debe asignar al menos un rol». | 400 Bad Request; `{"validationErrors": {"rolIds": "Debe asignar al menos un rol"}}`. | **Pasa** | `evidencias/B-05_roles_vacio.png` |
| **B-06** | Roles (B2) | `admin` | `PUT /api/v1/usuarios/999/roles` o con `rolIds: [999]` | 404 Not Found («Usuario no encontrado» o «Roles no encontrados»). | 404 Not Found; `{"message": "Usuario no encontrado con id: 999"}` / `{"message": "Roles no encontrados: [999]"}`. | **Pasa** | `evidencias/B-06_usuario_rol_inexistente.png` |
| **B-07** | Roles (B2) | `admin` | `PUT /api/v1/usuarios/1/roles` (admin intentando quitarse el rol ADMIN) | 409 Conflict «Un administrador no puede quitarse a sí mismo el rol de administrador». | 409 Conflict; `{"message": "Un administrador no puede quitarse a sí mismo el rol de administrador"}`. | **Pasa** | `evidencias/B-07_admin_quitar_rol_admin.png` |
| **B-08** | Estado (B3) | `admin` / `vendedor` | `PATCH /api/v1/usuarios/1/estado` (`false`) y `PATCH /api/v1/usuarios/2/estado` (`false`) | 409 Conflict si admin intenta desactivarse a sí mismo; 200 OK al desactivar a otro usuario, invalidando su token previo (401) y nuevo login (401). | 409 Conflict en admin a sí mismo; al desactivar vendedor, su token previo responde 401 («El usuario está deshabilitado») y login responde 401. | **Pasa** | `evidencias/B-08_activar_desactivar_usuario.png` |

---

## 3. Detalle de los Casos Propios Diseñados (Parte A)

1. **A-05 (Autenticación - Tolerancia a espacios en blanco):**  
   Prueba que el endpoint `/auth/login` procese correctamente credenciales donde el usuario introduce involuntariamente espacios antes o después del nombre (`"  vendedor  "`). El sistema realiza un recorte (`trim`) en el DTO/controlador y valida satisfactoriamente la identidad contra la base de datos sin generar falsos rechazos.

2. **T-06 (Token - Petición sin header Authorization a endpoint protegido):**  
   Valida que el endpoint sensible `GET /api/v1/auth/me`, destinado a obtener el perfil del usuario actualmente autenticado, rechace de inmediato las peticiones sin cabecera `Authorization`, devolviendo `401 Unauthorized` con el mensaje estándar de la aplicación y sin filtrar ningún detalle de infraestructura.

3. **P-05 (Permisos - Intento de eliminación de cliente por almacen):**  
   Comprueba la separación estricta de responsabilidades (Principle of Least Privilege). Un usuario con rol `ALMACENERO`, aunque posee token válido y permisos sobre inventario, recibe un código `403 Forbidden` al intentar ejecutar un borrado directo (`DELETE`) en el módulo de clientes.

4. **U-03 (Usuarios - Validación de integridad referencial para empleado inexistente):**  
   Verifica que la administración de usuarios no permita la inserción de registros huérfanos o con referencias inválidas (`empleadoId: 999`). La capa de servicio detecta la ausencia del registro en la tabla `empleados` y retorna un `404 Not Found` descriptivo antes de intentar persistir datos en `usuarios` o `usuario_roles`.
