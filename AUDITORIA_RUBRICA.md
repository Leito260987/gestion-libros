# 🧾 Auditoría contra la Rúbrica — Nivel Sobresaliente

Proyecto: **API Biblioteca Universitaria** · Build: `mvn clean verify` ✅ · Pruebas: **31/31 verdes** ✅ · Arranque real verificado (Tomcat + OpenAPI) ✅

Leyenda de nivel: 🟢 Sobresaliente · 🟡 Cumple · ⚪ Mejora futura

---

## 1. Persistencia

| Requisito | Implementación | Archivo(s) | Prueba | Nivel |
|---|---|---|---|---|
| JPA / Hibernate / Spring Data | Repositorios `JpaRepository` + entidades JPA | `repository/*`, `entity/*` | `ApiIntegrationTest` | 🟢 |
| Relaciones complejas | `@ManyToOne` LAZY (Prestamo→Usuario/Libro), `@ManyToMany` (Usuario↔Rol) con tabla `usuario_rol` | `entity/Prestamo.java`, `entity/Usuario.java` | flujo préstamo | 🟢 |
| Consultas personalizadas (JPQL) | `buscarCatalogo`, `findVencidos`, `contarPorEstado`, `librosMasPrestados`, `findDetalleById` | `repository/LibroRepository.java`, `PrestamoRepository.java` | integración + estadísticas | 🟢 |
| Consultas derivadas | `findByEmail`, `existsByIsbn`, `countByRoles_Nombre`, `existsByLibroIdAndEstado` | `repository/*` | varias | 🟢 |
| Paginación y ordenamiento | `Pageable` + `Page` + `PageResponse` + `@PageableDefault` | `LibroController`, `PrestamoController`, `dto/response/PageResponse.java` | `buscar` en integración | 🟢 |
| Filtros | Título, autor, categoría, disponibilidad (parámetros opcionales combinables) | `LibroRepository.buscarCatalogo` | `Listar/Buscar` (Postman) | 🟢 |
| Prevención de N+1 | `@EntityGraph(roles)` en login; `JOIN FETCH` de usuario+libro en préstamos | `UsuarioRepository`, `PrestamoRepository` | mapeo sin lazy exceptions | 🟢 |
| Índices | Índices en email, estado, isbn, título, autor, categoría, usuario_id, libro_id, estado, vencimiento | `entity/*` (`@Index`), `db/schema.sql` | — | 🟢 |
| Transacciones | `@Transactional` en todos los servicios; escritura vs `readOnly` | `service/impl/*` | — | 🟢 |
| Concurrencia / disponibilidad | Bloqueo pesimista `@Lock(PESSIMISTIC_WRITE)` + `@Version` en Libro | `LibroRepository.findByIdForUpdate`, `entity/Libro.java` | `flujoPrestamoDevolucion` | 🟢 |
| Manejo de excepciones de persistencia | `DataIntegrityViolationException` → 409 | `exception/GlobalExceptionHandler.java` | — | 🟢 |

## 2. Modelo de datos

| Requisito | Implementación | Archivo(s) | Nivel |
|---|---|---|---|
| Normalización (3FN) | Entidades separadas; N:M vía tabla intermedia | `entity/*`, `db/schema.sql` | 🟢 |
| Restricciones de integridad | `UNIQUE` (email, isbn), FKs, `CHECK` cantidades | `db/schema.sql`, `@UniqueConstraint` | 🟢 |
| DTOs | Requests y responses independientes de la entidad | `dto/request/*`, `dto/response/*` | 🟢 |
| Separación Entity/DTO | Mappers dedicados; entidades nunca expuestas | `mapper/*` | 🟢 |
| Lógica de negocio en Service | Controllers sin lógica; reglas en `service/impl` | `service/impl/*` | 🟢 |

## 3. Seguridad

| Requisito | Implementación | Archivo(s) | Prueba | Nivel |
|---|---|---|---|---|
| JWT | Emisión/validación HS con jjwt | `security/JwtService.java` | `JwtServiceTest` | 🟢 |
| Stateless | `SessionCreationPolicy.STATELESS`, sin sesiones | `security/SecurityConfig.java` | smoke | 🟢 |
| Refresh token | Secreto y expiración propios + claim `type` | `JwtService`, `AuthServiceImpl.refresh` | `Refresh` (Postman) | 🟢 |
| Filtro personalizado | `OncePerRequestFilter` que autentica por Bearer | `security/JwtAuthenticationFilter.java` | integración | 🟢 |
| Expiración / firma | Validadas en `parse*`; token manipulado rechazado | `JwtService` | `tokenManipuladoRechazado` | 🟢 |
| PasswordEncoder | BCrypt; contraseña solo como hash | `SecurityConfig`, `AuthServiceImpl` | — | 🟢 |
| AuthenticationManager | Login vía `authenticationManager.authenticate` | `AuthServiceImpl.login` | `login_ok` | 🟢 |
| Roles / autoridades | `ROLE_ADMIN` / `ROLE_USER` en authorities del token | `UserPrincipal`, `JwtAuthenticationFilter` | `admin_creaLibro` | 🟢 |
| 401 / 403 en JSON | EntryPoint (401) y AccessDeniedHandler (403) uniformes | `security/JwtAuthenticationEntryPoint.java`, `RestAccessDeniedHandler.java` | `sinToken_401`, `user_noCreaLibros` | 🟢 |

## 4. Control de acceso

| Requisito | Implementación | Archivo(s) | Prueba | Nivel |
|---|---|---|---|---|
| Protección por endpoint | `requestMatchers` + `hasRole` | `SecurityConfig` | `user_noCreaLibros` (403) | 🟢 |
| Protección por método | `@PreAuthorize("hasRole('ADMIN')")` (defensa en profundidad) | controllers | — | 🟢 |
| Protección por propietario | `verificarPropiedad` (USER no accede a préstamos ajenos) | `PrestamoServiceImpl` | `user_noVePrestamoAjeno` (403) | 🟢 |
| Lógica mantenible | `SecurityUtils` + `AuthenticatedUser` (id en principal) | `security/*` | — | 🟢 |

## 5. Manejo centralizado de errores

| Requisito | Implementación | Archivo(s) | Prueba | Nivel |
|---|---|---|---|---|
| `@RestControllerAdvice` | Traducción uniforme a `ApiError` | `GlobalExceptionHandler` | varias | 🟢 |
| Excepciones personalizadas | NotFound (404), Duplicate (409), BusinessRule (409), AccessDeniedBusiness (403) | `exception/*` | integración | 🟢 |
| Validación con detalles | `MethodArgumentNotValidException` → 400 + `details[]` | `GlobalExceptionHandler` | `registro_validacion` | 🟢 |
| Sin fugas de información | `include-stacktrace: never`; genérico 500 logueado | `application.yml`, handler | — | 🟢 |

## 6. Reglas de negocio

| Regla | Implementación | Prueba | Nivel |
|---|---|---|---|
| Prestar solo si disponible > 0 | `crear` valida `hayDisponibilidad` | `crear_sinDisponibilidad` | 🟢 |
| Préstamo: disponible − 1 | `crear` decrementa bajo lock | `crear_exitoso` | 🟢 |
| Devolución: disponible + 1 | `devolver` incrementa bajo lock | `devolver_exitoso` | 🟢 |
| No devolver dos veces | `devolver` valida estado DEVUELTO | `devolver_yaDevuelto` (409) | 🟢 |
| No prestar a usuario inactivo | `crear` valida `isActivo` | `crear_usuarioInactivo` | 🟢 |
| Identificar vencidos | `findVencidos` + marca `VENCIDO` | `/api/loans/vencidos` | 🟢 |
| Operaciones críticas transaccionales | `@Transactional` en crear/devolver | flujo integración | 🟢 |

## 7. Validaciones

| Requisito | Implementación | Nivel |
|---|---|---|
| Jakarta Validation | `@NotBlank`, `@Email`, `@Size`, `@Pattern`, `@Min/@Max`, `@Positive` | 🟢 |
| Validación personalizada | `@ValidIsbn` con checksum ISBN-10/13 | 🟢 |
| Contraseña robusta | `@Pattern` (mayúscula + minúscula + dígito, 8–72) | 🟢 |
| Cantidades no negativas | `@Min(0)` + regla de no bajar del prestado | 🟢 |

## 8. API REST y códigos HTTP

| Requisito | Implementación | Nivel |
|---|---|---|
| Verbos correctos | GET/POST/PUT/PATCH/DELETE | 🟢 |
| Códigos apropiados | 200/201/204/400/401/403/404/409/500 (no siempre 200) | 🟢 |
| Rutas coherentes | `/api/auth`, `/api/books`, `/api/users`, `/api/loans` | 🟢 |

## 9. Documentación y calidad

| Requisito | Implementación | Archivo(s) | Nivel |
|---|---|---|---|
| Swagger / OpenAPI | springdoc + esquema Bearer JWT + `@Operation` | `config/OpenApiConfig.java`, controllers | 🟢 |
| README profesional | 21 secciones, diagramas Mermaid (arquitectura + ER) | `README.md` | 🟢 |
| Postman | Colección + environment con tests 200/201/400/401/403/404/409 | `postman/*` | 🟢 |
| Pruebas reales | 31 pruebas (unit + integración + smoke) | `src/test/*` | 🟢 |
| Código limpio | Capas, responsabilidad única, sin lógica en controllers, sin entidades expuestas | todo el proyecto | 🟢 |
| Configuración segura | Variables de entorno, `.env.example`, `.gitignore` | raíz | 🟢 |

---

## Verificación ejecutada

```
mvn clean package        → BUILD SUCCESS, target/biblioteca.jar
mvn test                 → Tests run: 31, Failures: 0, Errors: 0
ApplicationSmokeTest     → Tomcat arranca en puerto real; /v3/api-docs OK; 401 sin token
```

## Mejoras futuras (⚪, no exigidas para sobresaliente)

- Persistir refresh tokens en BD para **rotación/revocación** explícita (hoy son JWT stateless).
- Rate limiting en `/api/auth/login` (protección contra fuerza bruta).
- Auditoría (`@CreatedDate`/`@LastModifiedDate`) con Spring Data Auditing.
- Migraciones versionadas con Flyway/Liquibase en lugar de `ddl-auto`.
- Job programado para marcar vencidos automáticamente (`@Scheduled`).

**Conclusión:** todos los criterios de nivel **Sobresaliente** de la rúbrica están implementados, evidenciados en código y respaldados por pruebas automatizadas. El proyecto compila, arranca y pasa el 100% de las pruebas.
