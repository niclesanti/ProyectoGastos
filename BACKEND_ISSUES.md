# Backend Issues - Análisis Completo

Fecha de análisis: 18 de julio de 2026

---

## DELIVERABLE 1: Plan de Mejoras (Refactorización y Optimización)

### 1. Arquitectura y Patrones de Diseño

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| A-1 | `buscarEspacioTrabajoPorId` duplicado en 5 service implementations | Alta | `TransaccionServiceImpl.java:449-455`, `CompraCreditoServiceImpl.java:852-858`, `CuentaBancariaServiceImpl.java:233-240`, `EspacioTrabajoServiceImpl.java:288-294`, `DashboardServiceImpl.java:141-147` |
| A-2 | Lógica de creación de `GastosIngresosMensuales` duplicada en 4 lugares | Alta | `TransaccionServiceImpl.java:387-398`, `CompraCreditoServiceImpl.java:738-749`, `CompraCreditoServiceImpl.java:791-802`, `DashboardServiceImpl.java:165-173` |
| A-3 | `buscarMotivoPorId` duplicado en 2 service implementations | Alta | `TransaccionServiceImpl.java:457-463`, `CompraCreditoServiceImpl.java:860-866` |
| A-4 | Actualizaciones manuales de `fechaModificacion` en múltiples lugares — usar `@OrderBy` o query sorting | Media | `TransaccionServiceImpl.java:101,121`, `CompraCreditoServiceImpl.java:126,138,143,618` |
| A-5 | Falta clase base `AuditableEntity` uniforme — algunos entities usan `@EntityListeners`, otros campos manuales | Media | `EspacioTrabajo.java`, `CuentaBancaria.java`, `Tarjeta.java` vs `Transaccion.java`, `CompraCredito.java` |
| A-6 | Lógica de negocio en entities — considerar mover a domain services | Baja | `EspacioTrabajo.java:52-66`, `CompraCredito.java:62-68` |

**Recomendaciones:**
- Extraer `buscarEspacioTrabajoPorId` a un componente compartido o `WorkspaceServiceHelper`
- Crear `GastosIngresosMensualesService` con patrón find-or-create centralizado
- Usar `@OrderBy` en relaciones `@ManyToMany` en lugar de mutar entidades para ordenamiento
- Crear `@MappedSuperclass AuditableEntity` con campos de auditoría uniformes
- Considerar extraer lógica de saldo de entities a services (DDD Lite)

---

### 2. Seguridad

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| S-1 | **JWT Secret con fallback hardcoded en producción** | **Crítica** | `application-prod.properties:43` |
| S-2 | `SecretKey` de JWT se recrea en cada request — cachear en `@PostConstruct` | Alta | `JwtTokenProvider.java:39,104` |
| S-3 | CORS configurado en dos lugares — eliminar `CorsConfig.java` | Alta | `SecurityConfig.java:55-66`, `CorsConfig.java:15-21` |
| S-4 | Actuator endpoints públicos sin security en management port en prod | Media | `SecurityConfig.java:97`, `application-dev.properties:80` |
| S-5 | JWT en URL query params para SSE — leak en logs y browser history | Media | `JwtAuthenticationFilter.java:101-104` |
| S-6 | `BCryptPasswordEncoder` bean definido pero sin uso | Baja | `SecurityConfig.java:49-52` |

**Recomendaciones:**
- Eliminar fallback de JWT secret en prod — la app debe fallar si no se provee
- Cachear `SecretKey` en `@PostConstruct` para no recrear por request
- Eliminar `CorsConfig.java` duplicado — Spring Security CORS es suficiente
- Agregar security al management port en producción
- Considerar tokens de corto vida o cookies para SSE en lugar de JWT en URL
- Eliminar bean `passwordEncoder()` si no se usa autenticación por password

---

### 3. Acceso a Datos y Persistencia

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| D-1 | **N+1 en query paginada** de compras crédito — falta `JOIN FETCH` | Alta | `CompraCreditoRepository.java:30-35` |
| D-2 | `CONCAT` en WHERE impide uso de índices en `GastosIngresosMensuales` | Alta | `GastosIngresosMensualesRepository.java:34-42` |
| D-3 | `ResumenScheduler` carga todas las tarjetas con `findAll()` | Media | `ResumenScheduler.java:73` |
| D-4 | Sin soft deletes en entidades financieras clave | Media | Todas las entidades principales |
| D-5 | Dashboard query podría usar window functions para optimizar porcentajes | Baja | `DashboardRepository.java:18-31` |

**Recomendaciones:**
- Agregar `JOIN FETCH` a la query paginada de `CompraCreditoRepository` o usar `@EntityGraph`
- Parsear year/month en la capa de aplicación y usar `OR` conditions en lugar de `CONCAT`
- Usar `findByDiaCierre` con join de workspace en `ResumenScheduler`
- Implementar `@SQLDelete` para soft deletes en `Transaccion`, `CompraCredito`
- Reemplazar subqueries de porcentaje con window functions `SUM(...) OVER()`

---

### 4. Diseño de API

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| P-1 | `POST /api/transaccion/buscar` y `POST /api/comprascredito/buscar` — buscar debería ser `GET` | Alta | `TransaccionController.java`, `ComprasCreditoController.java` |
| P-2 | `PUT` con boolean en path (`responder/{id}/{aceptada}`) — no RESTful | Alta | `EspacioTrabajoController.java` |
| P-3 | Endpoint de test `/api/notificaciones/test/enviar` siempre disponible | Alta | `NotificacionController.java:183-198` |
| P-4 | Sin API versioning (`/api/v1/...`) | Media | Todos los controllers |
| P-5 | `AuthController.getAuthStatus` genera JWT en cada status check | Media | `AuthController.java:45-49` |

**Recomendaciones:**
- Estandarizar: `POST` para create/search, `PUT` con body para updates
- Usar `POST` con request body en lugar de boolean en path para responder solicitudes
- Proteger endpoint de test con `@Profile("dev")` o eliminarlo
- Agregar prefijo `/api/v1/` para versionado del API
- Separar refresh token del status check de autenticación

---

### 5. Testing

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| T-1 | **0 tests de controllers** — agregar `@WebMvcTest` | Alta | `src/test/java/` |
| T-2 | **0 tests de integración** — agregar `@SpringBootTest` con Testcontainers | Alta | `src/test/java/` |
| T-3 | **0 tests de seguridad** — agregar tests de JWT y OAuth2 | Alta | `src/test/java/` |
| T-4 | Sin test fixtures/reutilizables — setUp() duplicado | Media | `TransaccionServiceTest.java:76-182` |
| T-5 | Sin tests para `NotificacionServiceImpl`, `SecurityServiceImpl`, schedulers | Media | `src/test/java/` |

**Recomendaciones:**
- Crear `@WebMvcTest` para todos los controllers
- Agregar tests de integración con Testcontainers para PostgreSQL
- Crear test fixtures compartidos (`TestFixtures`)
- Agregar tests de autorización con `spring-security-test`
- Cubrir servicios faltantes: Notificacion, Security, schedulers

---

### 6. Performance y Escalabilidad

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| F-1 | **Sin caché en ningún lugar** — dashboard stats se recalculan en cada request | Alta | `DashboardServiceImpl.java` |
| F-2 | SSE usa `ConcurrentHashMap` — no escala en cluster | Baja | `SseEmitterServiceImpl.java:38` |
| F-3 | Dashboard computation síncrona y potencialmente lenta | Baja | `DashboardServiceImpl.java` |

**Recomendaciones:**
- Agregar `@Cacheable` para dashboard stats y datos de referencia (motivos, contactos)
- Para escalabilidad futura, considerar Redis pub/sub para SSE
- Considerar `@Async` para computación de dashboard

---

### 7. Observabilidad

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| O-1 | Sin distributed tracing (Micrometer Tracing + OTel) | Media | `pom.xml` |
| O-2 | Sin `@Timed`/`@Observed` en métodos críticos de servicio | Media | Services |
| O-3 | Sin structured logging (JSON) para producción | Media | `logback-spring.xml` |
| O-4 | Logging con concatenación de strings en vez de parámetros SLF4J | Baja | `TransaccionServiceImpl.java:423` |

**Recomendaciones:**
- Agregar `micrometer-tracing-bridge-otel` para distributed tracing
- Agregar `@Timed`/`@Observed` en métodos como `registrarTransaccion`, `pagarResumenTarjeta`
- Agregar Logback JSON encoder para producción
- Usar logging parametrizado SLF4J consistentemente

---

### 8. Calidad de Código

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| C-1 | Métodos con mayúscula: `BuscarComprasCredito`, `FlujoMensual` | Media | `CompraCreditoService.java:20`, `DashboardServiceImpl.java:103,112,182,207` |
| C-2 | Timezone hardcoded en 8+ lugares | Media | Múltiples archivos |
| C-3 | `ProveedorAutenticacion` enum con valores sin uso | Baja | `ProveedorAutenticacion.java:5-7` |
| C-4 | `Descuento.porcentaje` es `String` — debería ser `BigDecimal` | Media | `Descuento.java:33` |
| C-5 | `pom.xml:15` — descripción desactualizada ("Producción Ganadera") | Baja | `pom.xml:15` |

**Recomendaciones:**
- Renombrar métodos a camelCase
- Extraer timezone a constante compartida
- Eliminar valores sin uso del enum
- Cambiar tipo de `porcentaje` a `BigDecimal`
- Actualizar descripción del pom.xml

---

### 9. Configuración

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| G-1 | `frontend.url` duplicado en dev properties | Media | `application-dev.properties:33,76` |
| G-2 | JWT expiration de 7 días — reducir a 1 hora + refresh token | Alta | `application-dev.properties:39`, `application-prod.properties:44` |
| G-3 | `spring.flyway.enabled` no explícito en dev | Baja | `application-dev.properties` |

**Recomendaciones:**
- Eliminar `frontend.url` duplicado
- Reducir JWT expiration y agregar mecanismo de refresh token
- Agregar `spring.flyway.enabled=true` explícito en dev

---

### 10. Build y Deployment

| # | Hallazgo | Prioridad | Archivos Afectados |
|---|----------|-----------|-------------------|
| B-1 | Sin `.dockerignore` — build copia archivos innecesarios | Alta | `backend/` |
| B-2 | `dockerfile-maven-plugin` (Spotify) redundante | Media | `pom.xml:186-195` |
| B-3 | `COPY *.jar` podría fallar con múltiples JARs | Media | `Dockerfile:36` |
| B-4 | Sin `HEALTHCHECK` en Dockerfile | Media | `Dockerfile:40` |
| B-5 | `forward-headers-strategy=FRAMEWORK` innecesario en dev | Baja | `application-dev.properties:73` |

**Recomendaciones:**
- Crear `.dockerignore` con entries para `target/`, `.git/`, `*.md`, `logs/`
- Eliminar `dockerfile-maven-plugin` redundante
- Usar nombre específico de JAR en COPY
- Agregar `HEALTHCHECK` instruction al Dockerfile
- Eliminar `forward-headers-strategy` del profile dev

---

## DELIVERABLE 2: Gap Analysis para SaaS Profesional

### Top 10 Features Faltantes (Priorizados)

| # | Feature | Por qué es necesario para SaaS | Complejidad |
|---|---------|--------------------------------|-------------|
| 1 | **Sistema de Suscripciones y Billing** (Stripe/MercadoPago) | Modelo de monetización core. Planes Free/Pro/Enterprise | Alta |
| 2 | **Rate Limiting** | Seguridad base de API. Prevenir abuso y DDoS | Media |
| 3 | **Notificaciones por Email** | Engagement. Recordatorios de pago, invitaciones, alertas de seguridad | Alta |
| 4 | **Audit Trail** (quién cambió qué, cuándo) | Cumplimiento legal (SOX, GDPR). Trazabilidad | Alta |
| 5 | **Role-Based Access Control** (RBAC) | Permisos granulares: ADMIN, MEMBER, VIEWER por workspace | Media |
| 6 | **Importación de Datos** (CSV/OFX/QIF) | Migración de usuarios desde otras herramientas | Media |
| 7 | **Generación de Reportes PDF** | Valor profesional. Reportes mensuales/anuales financieros | Media |
| 8 | **Ambiente Staging** | Seguridad en deployments. Testing antes de producción | Media |
| 9 | **GDPR Export/Delete de Datos** | Obligación legal. Derecho a portabilidad y olvido | Media |
| 10 | **API Versioning** (`/api/v1/`) | Evolución compatible del API sin romper clientes | Media |

### Features SaaS Adicionales por Categoría

#### Multi-tenancy
| Feature | Complejidad |
|---------|-------------|
| Row-Level Security (RLS) en PostgreSQL | Alta |
| Límites de recursos por workspace | Media |

#### User Management
| Feature | Complejidad |
|---------|-------------|
| User profile management (editar nombre/foto) | Baja |
| Email/password registration (alternativa a OAuth) | Media |
| Session management (ver/revocar sesiones) | Media |

#### Notifications
| Feature | Complejidad |
|---------|-------------|
| Push notifications (web/mobile) | Alta |
| Preferencias de notificación por usuario | Media |

#### Reporting
| Feature | Complejidad |
|---------|-------------|
| CSV/Excel export | Baja |
| Presupuestos vs real | Media |
| Forecasting basado en histórico | Alta |

#### Integrations
| Feature | Complejidad |
|---------|-------------|
| Webhook system | Media |
| OCR de recibos (receipt scanning) | Alta |
| Open Banking (auto-import de bancos) | Alta |
| Integración con contabilidad (QuickBooks/Xero) | Alta |

#### DevOps
| Feature | Complejidad |
|---------|-------------|
| Blue-green deployments | Alta |
| Infrastructure as Code (Terraform/Pulumi) | Alta |
| Secrets management (Vault/AWS SM) | Alta |

#### Monitoring
| Feature | Complejidad |
|---------|-------------|
| APM (New Relic/Datadog/Grafana Tempo) | Media |
| Error tracking (Sentry/Rollbar) | Media |
| Alerting rules (PagerDuty/OpsGenie) | Media |

#### Data Management
| Feature | Complejidad |
|---------|-------------|
| Soft deletes | Media |
| Data archival | Media |
| Undo/redo de acciones | Alta |

#### Internationalization (i18n)
| Feature | Complejidad |
|---------|-------------|
| Multi-language support | Alta |
| Multi-currency support | Alta |
| Timezone-aware handling | Media |

#### Developer Experience
| Feature | Complejidad |
|---------|-------------|
| Public API documentation portal | Media |
| SDK/client libraries | Alta |
| Postman collection | Baja |
