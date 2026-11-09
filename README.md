# PharmaSoft · Backend

API Spring Boot para catálogo, clientes, usuarios, ventas y reportes. Usa Oracle como base de datos y Redis para la caché de categorías y productos. El informe y las evidencias de la actividad autónoma están en [docs/informe-s13.pdf](docs/informe-s13.pdf).

## Desarrollo

Se requiere Java 21, Maven (o el wrapper del repositorio), un esquema Oracle de pruebas y Redis. Configura el perfil `dev` para el entorno local y prepara el esquema y los usuarios de ensayo antes de iniciar la API. El frontend de desarrollo usa el puerto 4200. Los errores manejados incluyen `requestId`, que coincide con `X-Request-Id` y con el identificador en los logs.

## Despliegue en producción

Los tres locales acceden al mismo servicio central. Oracle conserva ventas, existencias y catálogos. Redis guarda copias temporales del catálogo.

```text
Navegadores de los locales 1, 2 y 3
    | HTTPS :443 (HTTP :80 redirige)
    v
Nginx · SPA Angular + proxy /api/
    | HTTP privado :8080
    v
PharmaBackend · Spring Boot, JWT y Actuator
    |-- TCP :1521 --> Oracle / PDB (datos persistentes)
    '-- TCP :6379 --> Redis (caché de categorías y productos)

Ensayo local: Nginx :8081 -> backend :8080; Oracle se publica en :1522.
```

| Variable | Propósito |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Selecciona el perfil de producción. |
| `SERVER_PORT` | Puerto privado del backend, coordinado con el proxy. |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | JDBC de la PDB y credenciales de la cuenta con privilegios mínimos. |
| `JWT_SECRET` | Clave aleatoria Base64 de al menos 32 bytes decodificados, inyectada por el gestor de secretos. |
| `JWT_EXPIRACION_MINUTOS` | Vigencia de la sesión JWT. |
| `REDIS_HOST`, `REDIS_PORT` | Dirección y puerto privados de Redis. |
| `SPRING_DATA_REDIS_USERNAME`, `SPRING_DATA_REDIS_PASSWORD` | Cuenta y secreto de la ACL si Redis usa autenticación. |
| `FRONTEND_ORIGINS` | Orígenes HTTPS permitidos si hay clientes en otro origen. La SPA de producción usa `/api/v1` relativo. |

### Construcción, publicación y reversión

1. Selecciona una revisión aprobada de cada repositorio. En el backend, ejecuta `./mvnw clean verify` con Java 21 y un Oracle de pruebas preparado. En el frontend, usa la versión de Node compatible del README y ejecuta `npm ci` y `npm run build -- --configuration production --stats-json`. Conserva el JAR, el directorio `dist/pharma-frontend/browser` y el ID de cada revisión.
2. Respalda Oracle y aplica las migraciones revisadas en orden antes de arrancar. El backend de producción valida el esquema con `ddl-auto=validate`. Prepara Redis en una red privada. Inyecta las variables desde el servicio de ejecución, activa el perfil `prod` y ejecuta `java -jar <artefacto>.jar`.
3. Adapta `deploy/nginx.conf` del frontend al nombre privado del backend, monta el certificado TLS y redirige HTTP a HTTPS. Valida con `nginx -t`. Publica los archivos con hash primero y sustituye `index.html` al final de forma atómica. Conserva temporalmente los recursos anteriores para sesiones abiertas y para reversión.
4. Comprueba en un terminal piloto el login, F5 en Productos, una venta de ensayo, el stock y `/actuator/health` desde la red de monitoreo. Confirma que la cuenta operativa recibe 403 en métricas y que el administrador ve los detalles. Despliega la misma versión validada para los tres locales.
5. Para revertir, restaura el JAR y el `index.html` de la última pareja de versiones compatible. Mantén sus archivos con hash. Comprueba salud y venta de ensayo. Si hubo cambios de esquema, sigue la reversión de Oracle planificada para esa migración. No vuelvas a arrancar la versión anterior contra un esquema incompatible.

### Buenas prácticas de la sesión

| Práctica | Estado | Acción pendiente |
| --- | --- | --- |
| Línea base y comparación con el mismo procedimiento | Cumple | Repetir en una terminal piloto con la red de cada local. |
| Rutas diferidas y reporte de Inicio con `@defer` y permiso | Cumple | Revisar el build cuando se agreguen pantallas. |
| Entornos separados y presupuestos del build | Cumple | Automatizar el control en la integración continua. |
| Entrada HTML revalidada, JS/CSS con hash inmutable, proxy y fallback | Cumple | Validar configuración y certificado en el dominio final. |
| API autenticada con `no-store` | Cumple | Conservar la cabecera a través del proxy. |
| TTL de 10 minutos e invalidación de escrituras de catálogo y ventas | Cumple parcialmente | La matriz secuencial pasa. Validar concurrencia y vaciar el espacio de caché tras una recuperación de Redis que pueda conservar claves antiguas. |
| Degradación a Oracle y salud DOWN si falla Redis | Cumple | Alertar por Redis y vigilar carga sobre Oracle. |
| Correlación de errores, ventas y logs sin datos sensibles en la prueba | Cumple | Mantener acceso y retención restringidos y repetir la auditoría al cambiar logs. |
| Salud pública resumida y métricas/detalles protegidos | Cumple | Consultar Actuator solo desde la red de monitoreo. |
| Perfil de producción y secretos externos | Cumple | Aprovisionar y rotar secretos en el entorno final. |
| HTTPS, alertas y restauración de Oracle operativas en los tres locales | No cumple | Implementarlas y probarlas antes de abrir el acceso a los locales. |

### Prioridades para los tres locales

1. **HTTPS y red privada.** Publicar un solo nombre HTTPS para los tres locales. Restringir los puertos de Oracle, Redis y del backend a la red interna.
2. **Respaldo de Oracle y restauración probada.** Automatizar copias cifradas fuera del host y ensayar la restauración con ventas y existencias de prueba. Acordar el tiempo y la pérdida de datos tolerables.
3. **Redis central y alertas.** Compartir un Redis privado para invalidar el mismo catálogo desde todos los locales. Alertar sobre salud, latencia de ventas y pérdida de aciertos. Si Redis se recupera con datos persistidos, purgar las claves del catálogo antes de reanudar su uso.
