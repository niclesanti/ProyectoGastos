# Backend - Sistema de Gestión de Gastos Personales

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Problema que Resuelve](#-problema-que-resuelve)
- [Funcionalidades Principales](#-funcionalidades-principales)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Modelo de Datos](#-modelo-de-datos)
- [Sistema de Notificaciones en Tiempo Real](#-sistema-de-notificaciones-en-tiempo-real)
- [Observabilidad y Métricas](#-observabilidad-y-métricas)
- [Configuración y Requisitos](#%EF%B8%8F-configuración-y-requisitos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [API Endpoints](#-api-endpoints)
- [Seguridad y Autenticación](#-seguridad-y-autenticación)
- [Migraciones de Base de Datos](#-migraciones-de-base-de-datos)
- [Testing](#-testing)
- [CI/CD - Integración y Despliegue Continuo](#-cicd---integración-y-despliegue-continuo)
- [Despliegue con Docker](#-despliegue-con-docker)
- [Mejores Prácticas Implementadas](#-mejores-prácticas-implementadas)

---

## 🎯 Descripción General

Sistema backend RESTful desarrollado con Spring Boot que proporciona una solución completa para la gestión de finanzas personales y familiares. El sistema permite el registro y control de transacciones, cuentas bancarias, tarjetas de crédito, compras en cuotas y análisis financiero mediante dashboards interactivos.

### Características Destacadas

- ✅ **Arquitectura de Capas**: Implementación del patrón MVC con separación clara de responsabilidades
- ✅ **Autenticación OAuth2**: Integración con proveedores externos (por lo pronto solo de Google)
- ✅ **Gestión Multi-Tenant**: Espacios de trabajo compartidos para gestión familiar o grupal
- ✅ **Procesamiento Automático**: Cierre automático de resúmenes de tarjetas mediante schedulers
- ✅ **Notificaciones en Tiempo Real**: SSE (Server-Sent Events) y arquitectura dirigida por eventos
- ✅ **Observabilidad y Métricas**: Instrumentación completa con Micrometer y Prometheus para monitoreo en producción
- ✅ **CI/CD Automatizado**: Pipeline completo de integración y despliegue continuo con GitHub Actions
- **Agente IA Financiero**: Asistente conversacional con LLM (Llama 3.3 70B via Groq), function calling, selección dinámica de tools y retry automático ante rate limits
- ✅ **Validaciones Robustas**: Bean Validation con validadores personalizados
- ✅ **Documentación Automática**: API documentada con Swagger/OpenAPI
- ✅ **Manejo de Errores**: Sistema centralizado de gestión de excepciones

---

## 💡 Problema que Resuelve

### Contexto

La gestión de finanzas personales y familiares es un desafío constante. Las personas necesitan:
- Controlar múltiples cuentas bancarias y medios de pago
- Hacer seguimiento de gastos e ingresos categorizados
- Gestionar compras en cuotas y resúmenes de tarjetas de crédito
- Compartir información financiera con miembros de la familia
- Visualizar el estado financiero de forma clara y centralizada

### Solución

Este backend proporciona una API REST completa que permite:

1. **Gestión Centralizada**: Unifica todas las transacciones financieras en un solo lugar
2. **Colaboración Familiar**: Espacios de trabajo compartidos para gestión conjunta
3. **Automatización**: Cierre automático de períodos y cálculo de estadísticas
4. **Trazabilidad**: Auditoría completa de todas las operaciones financieras
5. **Flexibilidad**: Categorización personalizada y múltiples tipos de transacciones
6. **Análisis**: Dashboard con indicadores clave y gráficos de tendencias

---

## 🚀 Funcionalidades Principales

### 1. Gestión de Usuarios y Autenticación
- Autenticación mediante OAuth2 (Google)
- Control de sesiones y tokens

### 2. Espacios de Trabajo Colaborativos
- Creación y administración de espacios de trabajo
- Sistema de invitaciones con solicitudes pendientes
- Aprobación o rechazo de invitaciones por el usuario invitado
- Sistema de permisos (administrador/participante)
- Compartir espacios entre múltiples usuarios
- Gestión de miembros del espacio
- Saldo consolidado por espacio

### 3. Gestión de Transacciones
- Registro de ingresos y gastos
- Categorización mediante motivos personalizados
- Asociación con cuentas bancarias
- Contactos para transferencias
- Filtros avanzados de búsqueda
- Auditoría completa (usuario, fecha, hora)

### 4. Cuentas Bancarias
- Gestión de múltiples cuentas
- Actualización automática de saldos
- Transferencias entre cuentas
- Histórico de movimientos

### 5. Tarjetas de Crédito y Compras en Cuotas
- Registro de tarjetas con configuración de cierre y vencimiento
- Compras en cuotas con seguimiento individual
- Generación automática de cuotas
- Cierre automático de resúmenes mensuales
- Pago de resúmenes con actualización de cuotas
- Estados de resúmenes (abierto, cerrado, pagado, pagado parcial)

### 6. Dashboard y Estadísticas
- Balance total del espacio de trabajo
- Gastos mensuales consolidados
- Resumen mensual de tarjetas
- Deuda total pendiente
- Flujo mensual (ingresos vs gastos)
- Distribución de gastos por categoría
- **Flujo mensual de tarjeta de crédito** (compras con crédito vs pagos de resúmenes — últimos 3/6/12 meses)
- **Distribución de compras con crédito por categoría** (participación porcentual por motivo)
- Optimización mediante tabla agregada para evitar recálculos
- Los registros mensuales se actualizan usando la **fecha real de la operación**, garantizando que transacciones backdated afecten el período correcto

### 7. Notificaciones en Tiempo Real
- **SSE (Server-Sent Events)**: Conexión persistente para notificaciones instantáneas
- **Arquitectura de Eventos**: Publicación/suscripción con `ApplicationEventPublisher`
- **Tipos de Notificaciones**:
  - `CIERRE_TARJETA`: Cierre automático de resúmenes
  - `VENCIMIENTO_RESUMEN`: Recordatorio de vencimiento
  - `INVITACION_ESPACIO`: Invitación a workspace
  - `MIEMBRO_AGREGADO`: Nuevo miembro en espacio
  - `SISTEMA`: Mensajes del sistema
- **Limpieza Automática**: Schedulers para eliminar notificaciones antiguas
- **Autenticación SSE**: Query parameter con token JWT (compatible con EventSource nativo)

### 8. Automatización
- Cierre automático diario de resúmenes de tarjetas (scheduler)
- Actualización automática de saldos
- Cálculo incremental de estadísticas
- Limpieza automática de notificaciones

### 9. Agente IA Financiero
- **Asistente Conversacional**: Consultas en lenguaje natural sobre finanzas personales
- **LLM**: Llama 3.3 70B Versatile vía API de Groq (compatible con OpenAI)
- **Function Calling con selección dinámica**: El agente elige automáticamente el subconjunto de tools relevantes según el mensaje (2-6 tools en lugar de las 11 siempre), reduciendo el consumo de tokens por request
  - `obtenerDashboardFinanciero`: Balance total, gastos mensuales, deuda pendiente
  - `buscarTransacciones`: Búsqueda filtrada por mes, año, motivo/categoría y contacto
  - `listarTarjetasCredito`: Consulta de tarjetas registradas
  - `listarResumenesTarjetas`: Estado de resúmenes mensuales por espacio de trabajo
  - `listarResumenesPorTarjeta`: Historial de resúmenes de una tarjeta específica
  - `listarCuotasPorTarjeta`: Cuotas del período actual de una tarjeta específica
  - `buscarTodasComprasCredito`: Historial completo de compras en cuotas
  - `listarComprasCreditoPendientes`: Compras con cuotas aún pendientes de pago
  - `listarCuentasBancarias`: Saldos de cuentas
  - `listarMotivosTransacciones`: Categorías disponibles
  - `listarContactosTransaccion`: Contactos de transferencia registrados
- **Rate Limiting**: 60 mensajes/minuto por usuario con burst capacity de 10
- **Auditoría Completa**: Registro de todas las interacciones en `agente_audit_log`
- **Streaming SSE**: Respuestas en tiempo real token por token
- **Multi-tenant**: Respeta permisos de espacios de trabajo
- **Gratuito**: API de Groq sin necesidad de facturación

### 10. Observabilidad y Métricas
- **Instrumentación de Negocio**: Métricas sobre transacciones, compras a crédito, resúmenes y notificaciones
- **Micrometer + Prometheus**: Formato estándar de métricas exportables
- **Spring Boot Actuator**: Endpoints de salud y métricas (/actuator/health, /actuator/prometheus)
- **Métricas Implementadas**:
  - Contadores: Transacciones creadas/eliminadas, compras a crédito, cuotas pagadas, resúmenes generados
  - Timers: Tiempo de ejecución del scheduler de resúmenes
  - Gauges: Conexiones SSE activas, cuotas pendientes de pago
- **Tags Inteligentes**: Filtrado por tipo de transacción, espacio de trabajo y tarjeta
- **Integración Grafana**: Dashboards profesionales prediseñados con 11 paneles de métricas

---

## 🛠 Stack Tecnológico

### Core Framework
- **Spring Boot 3.5.3**: Framework principal con Spring 6
- **Java 21**: Aprovechamiento de características modernas del lenguaje
- **Maven**: Gestión de dependencias y construcción

### Persistencia
- **Spring Data JPA**: Abstracción de acceso a datos
- **Hibernate**: ORM para mapeo objeto-relacional
- **PostgreSQL**: Base de datos relacional en producción
- **H2**: Base de datos en memoria para testing
- **Flyway**: Gestión de migraciones y versionado de esquema

### Seguridad
- **Spring Security**: Framework de seguridad
- **OAuth2 Client**: Autenticación con proveedores externos
- **BCrypt**: Encriptación de contraseñas

### Mapeo y Transformación
- **MapStruct 1.5.5**: Mapeo automático entre entidades y DTOs
- **Lombok**: Reducción de código boilerplate

### Validación
- **Bean Validation**: Validación declarativa de datos
- **Hibernate Validator**: Implementación de JSR-380
- **Validadores Personalizados**: Lógica de validación específica del dominio

### Inteligencia Artificial
- **Spring AI 1.0.0-M5**: Framework para integración de LLMs
- **Groq API**: Proveedor de inferencia ultra-rápida (compatible con OpenAI)
- **Llama 3.3 70B Versatile**: Modelo de lenguaje de Meta optimizado
- **Function Calling**: Ejecución de herramientas desde el LLM
- **Bucket4j**: Rate limiting con token bucket algorithm

### Documentación
- **SpringDoc OpenAPI 2.8.8**: Generación automática de documentación API
- **Swagger UI**: Interfaz interactiva para testing de endpoints

### Observabilidad
- **Spring Boot Actuator**: Endpoints de salud y métricas de aplicación
- **Micrometer**: Facade de métricas con soporte para múltiples sistemas de monitoreo
- **Prometheus Format**: Exportación de métricas en formato Prometheus
- **Métricas Custom**: Instrumentación de lógica de negocio específica

### Utilidades
- **Spring Boot DevTools**: Herramientas de desarrollo (hot reload)
- **Logback**: Framework de logging con configuración personalizada
- **HikariCP**: Pool de conexiones de alto rendimiento

### Testing
- **JUnit 5**: Framework de testing
- **Spring Boot Test**: Herramientas de testing integradas
- **Spring Security Test**: Testing de seguridad

### Despliegue
- **Docker**: Contenerización de la aplicación
- **Alpine Linux**: Imagen base ligera y segura (eclipse-temurin:21-jre-alpine)
- **Multi-stage Build**: Optimización de imágenes Docker

### CI/CD
- **GitHub Actions**: Automatización de workflows
- **Continuous Integration**: Tests automáticos en cada push
- **Continuous Deployment**: Despliegue automático a producción
- **Docker Hub**: Registro de imágenes Docker

---

## 🏗 Arquitectura del Sistema

### Patrón de Arquitectura: Arquitectura en Capas

```
┌─────────────────────────────────────────────┐
│          CAPA DE PRESENTACIÓN               │
│        (Controllers - REST API)             │
│  - Manejo de peticiones HTTP                │
│  - Validación de entrada                    │
│  - Serialización JSON                       │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│          CAPA DE SERVICIO                   │
│        (Services - Lógica de Negocio)       │
│  - Reglas de negocio                        │
│  - Orquestación de operaciones              │
│  - Transacciones                            │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│          CAPA DE PERSISTENCIA               │
│        (Repositories - Acceso a Datos)      │
│  - Consultas a BD                           │
│  - Queries personalizadas                   │
│  - Gestión de entidades                     │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│          CAPA DE DATOS                      │
│        (Base de Datos PostgreSQL)           │
│  - Almacenamiento persistente               │
│  - Integridad referencial                   │
│  - Índices optimizados                      │
└─────────────────────────────────────────────┘

        COMPONENTES TRANSVERSALES
┌─────────────────────────────────────────────┐
│  - Seguridad (OAuth2 + Spring Security)    │
│  - Manejo de Excepciones (ControllerAdvisor)│
│  - Observabilidad (Actuator + Micrometer)   │
│  - Mappers (MapStruct)                      │
│  - Validadores (Bean Validation)            │
│  - DTOs (Data Transfer Objects)             │
│  - Schedulers (Tareas Programadas)          │
│  - Eventos (ApplicationEventPublisher)      │
│  - SSE (Server-Sent Events)                 │
│  - Configuración (application.properties)   │
└─────────────────────────────────────────────┘
```

### Principios Aplicados

1. **Separación de Responsabilidades (SoC)**: Cada capa tiene una responsabilidad específica
2. **Inyección de Dependencias**: Uso de constructor injection con Lombok `@RequiredArgsConstructor`
3. **Programación Orientada a Interfaces**: Servicios definidos mediante interfaces
4. **DTOs**: Separación entre modelo de dominio y modelo de transferencia
5. **Repository Pattern**: Abstracción del acceso a datos
6. **Service Layer**: Lógica de negocio centralizada y reutilizable

---

## 📁 Estructura del Proyecto

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/campito/backend/
│   │   │   ├── config/                    # Configuraciones de Spring
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/                # Controladores REST
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ComprasCreditoController.java
│   │   │   │   ├── CuentaBancariaController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── EspacioTrabajoController.java
│   │   │   │   ├── NotificacionController.java    # Sistema de notificaciones
│   │   │   │   ├── TransaccionController.java
│   │   │   │   └── UsuarioController.java
│   │   │   ├── dao/                       # Repositorios JPA
│   │   │   │   ├── CompraCreditoRepository.java
│   │   │   │   ├── ContactoTransferenciaRepository.java
│   │   │   │   ├── CuentaBancariaRepository.java
│   │   │   │   ├── CuotaCreditoRepository.java
│   │   │   │   ├── DashboardRepository.java
│   │   │   │   ├── EspacioTrabajoRepository.java
│   │   │   │   ├── GastosIngresosMensualesRepository.java
│   │   │   │   ├── MotivoTransaccionRepository.java
│   │   │   │   ├── NotificacionRepository.java    # Repositorio de notificaciones
│   │   │   │   ├── ResumenRepository.java
│   │   │   │   ├── SolicitudPendienteEspacioTrabajoRepository.java # Solicitudes
│   │   │   │   ├── TarjetaRepository.java
│   │   │   │   ├── TransaccionRepository.java
│   │   │   │   └── UsuarioRepository.java
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── *DTORequest.java       # DTOs para peticiones
│   │   │   │   ├── *DTOResponse.java      # DTOs para respuestas
│   │   │   │   ├── NotificacionDTOResponse.java  # DTO de notificaciones
│   │   │   │   └── SolicitudPendienteEspacioTrabajoDTOResponse.java # DTO solicitud
│   │   │   │   └── NotificacionDTOResponse.java  # DTO de notificaciones
│   │   │   ├── exception/                 # Manejo de excepciones
│   │   │   │   ├── ControllerAdvisor.java
│   │   │   │   └── ExceptionInfo.java
│   │   │   ├── event/                     # Eventos del sistema
│   │   │   │   ├── NotificacionEvent.java
│   │   │   │   └── NotificacionEventListener.java # Listener asíncrono
│   │   │   ├── mapper/                    # MapStruct Mappers
│   │   │   │   ├── config/
│   │   │   │   │   └── MapstructConfig.java
│   │   │   │   ├── SolicitudPendienteEspacioTrabajoMapper.java
│   │   │   │   ├── NotificacionMapper.java
│   │   │   │   └── *Mapper.java
│   │   │   │   ├── model/                     # Entidades JPA
│   │   │   │   ├── CompraCredito.java
│   │   │   │   ├── ContactoTransferencia.java
│   │   │   │   ├── CuentaBancaria.java
│   │   │   │   ├── CuotaCredito.java
│   │   │   │   ├── CustomOAuth2User.java
│   │   │   │   ├── EspacioTrabajo.java
│   │   │   │   ├── EstadoResumen.java     # Enum
│   │   │   │   ├── GastosIngresosMensuales.java
│   │   │   │   ├── MotivoTransaccion.java
│   │   │   │   ├── Notificacion.java      # Entidad de notificaciones
│   │   │   │   ├── ProveedorAutenticacion.java # Enum
│   │   │   │   ├── Resumen.java
│   │   │   │   ├── SolicitudPendienteEspacioTrabajo.java # Solicitudes de colaboración
│   │   │   │   ├── Tarjeta.java
│   │   │   │   ├── TipoNotificacion.java  # Enum de tipos de notificación
│   │   │   │   ├── TipoTransaccion.java   # Enum
│   │   │   │   ├── Transaccion.java
│   │   │   │   └── Usuario.java
│   │   │   ├── scheduler/                 # Tareas programadas
│   │   │   │   ├── NotificacionScheduler.java # Limpieza de notificaciones
│   │   │   │   └── ResumenScheduler.java
│   │   │   ├── security/                  # Componentes de seguridad JWT
│   │   │   │   ├── JwtAuthenticationFilter.java  # Soporta query param para SSE
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── OAuth2AuthenticationSuccessHandler.java
│   │   │   ├── service/                   # Capa de servicios
│   │   │   │   ├── *Service.java          # Interfaces
│   │   │   │   ├── *ServiceImpl.java      # Implementaciones
│   │   │   │   ├── CustomOidcUserService.java # Servicio OAuth2
│   │   │   │   ├── NotificacionService.java   # Servicio de notificaciones
│   │   │   │   ├── NotificacionServiceImpl.java
│   │   │   │   ├── SecurityService.java   # Servicio de seguridad y autorización
│   │   │   │   ├── SecurityServiceImpl.java
│   │   │   │   ├── SseEmitterService.java # SSE para notificaciones
│   │   │   │   └── SseEmitterServiceImpl.java
│   │   │   ├── validation/                # Validadores personalizados
│   │   │   │   ├── Valid*.java            # Anotaciones
│   │   │   │   └── *Validator.java        # Implementaciones
│   │   │   └── BackendApplication.java    # Clase principal
│   │   └── resources/
│   │       ├── db/migration/              # Scripts Flyway
│   │       │   ├── V1__Creacion_inicial_del_esquema.sql
│   │       │   ├── V2__create_cuentabancaria_and_update_transaccion.sql
│   │       │   ├── V3__create_compracredito_and_cuotacredito_tarjeta.sql
│   │       │   ├── V4__create_resumenes_table.sql
│   │       │   ├── V5__Optimizacion_Indices_Rendimiento.sql
│   │       │   ├── V6__create_gastos_ingresos_mensuales.sql
│   │       │   ├── V7__drop_notificaciones_presupuestos_tables.sql
│   │       │   ├── V8__add_unique_constraints_motivos_contactos.sql
│   │       │   ├── V9__unique_constraints_workspace_account_card.sql
│   │       │   ├── V10__add_audit_fields_to_entities.sql
│   │       │   ├── V11__migrate_usuario_to_uuid.sql
│   │       │   ├── V12__migrate_espacio_trabajo_to_uuid.sql
│   │       │   ├── V13__convert_real_to_numeric.sql
│   │       │   ├── V14__create_notificaciones_table.sql # Sistema de notificaciones
│   │       │   ├── V15__add_indexes_notificaciones.sql  # Índices optimizados
│   │       │   ├── V16__create_agente_audit_log.sql     # Auditoría Agente IA
│   │       │   └── V17__add_credito_columns_gastos_ingresos_mensuales.sql # Tracking crédito dashboard
│   │       ├── application.properties      # Configuración común
│   │       ├── application-dev.properties  # Perfil desarrollo
│   │       ├── application-prod.properties # Perfil producción
│   │       └── logback-spring.xml          # Configuración logging
│   └── test/
│       ├── java/                           # Tests unitarios
│       └── resources/
│           └── application.properties      # Configuración para tests
├── target/                                 # Artefactos compilados
├── Dockerfile                              # Imagen Docker multi-stage
├── pom.xml                                 # Configuración Maven
├── mvnw                                    # Maven Wrapper (Unix)
├── mvnw.cmd                                # Maven Wrapper (Windows)
└── README.md                               # Este archivo
```

---

## 🗄 Modelo de Datos

### Entidades Principales

#### Usuario
Representa a los usuarios del sistema que se autentican mediante OAuth2.
- **Atributos**: id, nombre, email, fotoPerfil, proveedor, idProveedor, rol, activo, fechaRegistro, fechaUltimoAcceso
- **Relaciones**: 
  - Administra múltiples EspaciosTrabajo
  - Participa en múltiples EspaciosTrabajo

#### EspacioTrabajo
Contexto colaborativo donde se gestionan las finanzas de un grupo.
- **Atributos**: id, nombre, saldo, usuarioAdmin, usuariosParticipantes
- **Métodos**: actualizarSaldoNuevaTransaccion(), actualizarSaldoEliminarTransaccion()
- **Relaciones**: 
  - Contiene CuentasBancarias, Transacciones, Motivos, Contactos, Tarjetas, ComprasCredito, GastosIngresosMensuales
  - Genera SolicitudesPendientes para invitar nuevos miembros

#### SolicitudPendienteEspacioTrabajo
Solicitudes de colaboración para unirse a un espacio de trabajo.
- **Atributos**: id, espacioTrabajo, usuarioInvitado, fechaCreacion
- **Flujo**: 
  1. Administrador invita usuario por email
  2. Sistema crea solicitud pendiente
  3. Usuario invitado recibe notificación
  4. Usuario puede aceptar o rechazar la solicitud
  5. Al aceptar, se agrega como participante del espacio
- **Relaciones**:
  - Pertenece a un EspacioTrabajo
  - Asociada a un Usuario (usuario invitado)

#### Transaccion
Registro de movimientos financieros (ingresos/gastos).
- **Atributos**: id, tipo, monto, fecha, descripcion, nombreCompletoAuditoria, fechaCreacion, espacioTrabajo, motivo, contacto, cuentaBancaria
- **Auditoría**: Incluye nombre del usuario y timestamp de creación

#### CuentaBancaria
Representa cuentas bancarias o billeteras virtuales.
- **Atributos**: id, nombre, entidadFinanciera, saldoActual, espacioTrabajo
- **Métodos**: actualizarSaldoNuevaTransaccion(), actualizarSaldoEliminarTransaccion()

#### Tarjeta
Tarjetas de crédito con configuración de ciclos de facturación.
- **Atributos**: id, numeroTarjeta (últimos 4 dígitos), entidadFinanciera, redDePago, diaCierre, diaVencimientoPago, espacioTrabajo

#### CompraCredito
Compras realizadas en cuotas con tarjeta de crédito.
- **Atributos**: id, fechaCompra, montoTotal, cantidadCuotas, cuotasPagadas, descripcion, nombreCompletoAuditoria, fechaCreacion, espacioTrabajo, motivo, comercio, tarjeta
- **Métodos**: pagarCuota()

#### CuotaCredito
Cuotas individuales de una compra a crédito.
- **Atributos**: id, numeroCuota, fechaVencimiento, montoCuota, pagada, compraCredito, resumenAsociado
- **Métodos**: pagarCuota()

#### Resumen
Resumen mensual de tarjeta generado automáticamente.
- **Atributos**: id, anio, mes, fechaVencimiento, estado, montoTotal, tarjeta, transaccionAsociada
- **Estados**: ABIERTO, CERRADO, PAGADO, PAGADO_PARCIAL
- **Métodos**: asociarTransaccion()

#### GastosIngresosMensuales
Tabla agregada para optimización de consultas de dashboard.
- **Atributos**: id, anio, mes, gastos, ingresos, **comprasCredito**, **pagoResumen**, espacioTrabajo
- **Métodos**: actualizarGastos(), actualizarIngresos(), eliminarGastos(), eliminarIngresos(), actualizarComprasCredito(), eliminarComprasCredito(), actualizarPagoResumen()
- **Actualización**: Cada método auxiliar recibe la fecha real de la transacción/compra para calcular el anio/mes correcto, evitando registros incorrectos con operaciones backdated

#### Notificacion
Notificaciones en tiempo real para eventos del sistema.
- **Atributos**: id, tipo, mensaje, leida, fechaCreacion, usuario, espacioTrabajo
- **Tipos**: CIERRE_TARJETA, VENCIMIENTO_RESUMEN, INVITACION_ESPACIO, MIEMBRO_AGREGADO, SISTEMA
- **Delivery**: SSE (Server-Sent Events)

#### AgenteAuditLog
Auditoría completa de interacciones con el Agente IA.
- **Atributos**: id, userId, workspaceId, userMessage, agentResponse, functionsCalled, timestamp, tokensUsed, success, errorMessage
- **Propósito**: Compliance, análisis de uso, debugging y control de costos
- **Relaciones**:
  - Pertenece a un Usuario
  - Asociada a un EspacioTrabajo

### Diagrama de Clases

El diagrama UML completo se encuentra en `/docs/DiagramaDeClasesUML.puml` y puede visualizarse con PlantUML.

---

## 🔔 Sistema de Notificaciones en Tiempo Real

### Arquitectura de Eventos

El sistema de notificaciones está implementado con una **arquitectura dirigida por eventos** usando el patrón **Publish/Subscribe**.

**Componentes Principales**:
```
Servicio → ApplicationEventPublisher → NotificacionEvent
                                              ↓
                                  NotificacionEventListener (@Async)
                                              ↓
                                    [Persiste en BD]
                                              ↓
                                   SseEmitterService
                                              ↓
                              Frontend (EventSource SSE)
```

### Componentes del Sistema

#### 1. NotificacionEvent
**Archivo**: `event/NotificacionEvent.java`

Evento que representa una notificación a generar.

```java
@Getter
public class NotificacionEvent extends ApplicationEvent {
    private final UUID idUsuario;
    private final TipoNotificacion tipo;
    private final String mensaje;
}
```

#### 2. NotificacionEventListener
**Archivo**: `event/NotificacionEventListener.java`

Listener asíncrono que captura eventos y procesa notificaciones.

```java
@Component
@RequiredArgsConstructor
public class NotificacionEventListener {
    @Async
    @EventListener
    @Transactional
    public void handleNotificacionEvent(NotificacionEvent event) {
        // 1. Buscar usuario
        // 2. Crear notificación
        // 3. Guardar en BD
        // 4. Enviar via SSE (si está conectado)
    }
}
```

**Características**:
- ✅ Procesamiento asíncrono con `@Async`
- ✅ Transaccional para garantizar persistencia
- ✅ No bloquea el hilo principal
- ✅ Try-catch para no propagar errores

#### 3. SseEmitterService
**Archivo**: `service/SseEmitterServiceImpl.java`

Gestiona conexiones SSE persistentes con clientes.

```java
@Service
public class SseEmitterServiceImpl implements SseEmitterService {
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    public SseEmitter crearEmitter(UUID idUsuario) {
        SseEmitter emitter = new SseEmitter(1 hora);
        // Configurar handlers: onCompletion, onTimeout, onError
        emitters.put(idUsuario, emitter);
        return emitter;
    }
    
    public void enviarNotificacion(UUID idUsuario, Notificacion notificacion) {
        // Enviar via SSE si el usuario está conectado
    }
}
```

**Ventajas de SSE vs WebSocket**:
- ✅ Más simple de implementar (HTTP estándar)
- ✅ Reconexión automática del navegador
- ✅ Menor consumo de recursos
- ✅ Suficiente para notificaciones unidireccionales

#### 4. NotificacionController
**Archivo**: `controller/NotificacionController.java`

Endpoints REST + SSE para gestión de notificaciones.

**Endpoints**:
- `GET /api/notificaciones` - Listar (últimas 50)
- `GET /api/notificaciones/no-leidas/count` - Contador
- `PUT /api/notificaciones/{id}/leer` - Marcar como leída
- `PUT /api/notificaciones/marcar-todas-leidas` - Marcar todas
- `DELETE /api/notificaciones/{id}` - Eliminar
- `GET /api/notificaciones/stream` - **SSE Stream** (requiere token JWT como query param)

### Tipos de Notificaciones

```java
public enum TipoNotificacion {
    CIERRE_TARJETA,          // Cierre automático de resúmenes
    VENCIMIENTO_RESUMEN,     // Recordatorio de vencimiento
    INVITACION_ESPACIO,      // Invitación a workspace
    MIEMBRO_AGREGADO,        // Nuevo miembro agregado
    SISTEMA                  // Notificaciones del sistema
}
```

### Cómo Agregar Notificaciones

En cualquier servicio, inyecta `ApplicationEventPublisher` y publica eventos:

```java
@Service
@RequiredArgsConstructor
public class MiServicio {
    private final ApplicationEventPublisher eventPublisher;
    
    public void miMetodo() {
        // ... tu lógica de negocio ...
        
        try {
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                idUsuarioDestinatario,
                TipoNotificacion.SISTEMA,
                "Mensaje descriptivo"
            ));
        } catch (Exception e) {
            // No propagar errores de notificaciones
            logger.error("Error al publicar notificación", e);
        }
    }
}
```

**Buenas Prácticas**:
- ✅ Siempre usar try-catch al publicar eventos
- ✅ Mensajes descriptivos y útiles
- ✅ Tipo de notificación apropiado
- ✅ Notificar al usuario correcto
- ❌ No incluir información sensible

### Limpieza Automática

**NotificacionScheduler** ejecuta tareas de mantenimiento:

- **Diario (3:00 AM)**: Elimina notificaciones leídas > 3 días
- **Mensual (1st día, 4:00 AM)**: Elimina notificaciones no leídas > 15 días

### Autenticación SSE con JWT

El endpoint SSE acepta el token JWT como **query parameter** en lugar de header:

```
GET /api/notificaciones/stream?token=eyJhbGciOiJIUzUx...
```

**¿Por qué query parameter?**
- ✅ EventSource nativo no soporta headers personalizados
- ✅ Mayor compatibilidad con navegadores
- ✅ No requiere polyfills

**Implementación en JwtAuthenticationFilter**:

```java
private String getJwtFromRequest(HttpServletRequest request) {
    // 1. Intentar primero con header Authorization (estándar)
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
    }
    
    // 2. Si no está en header, buscar en query parameter (para SSE)
    String tokenParam = request.getParameter("token");
    if (StringUtils.hasText(tokenParam)) {
        return tokenParam;
    }
    
    return null;
}
```

### Documentación Adicional

Ver guía completa para desarrolladores: `SistemaNotificaciones_GuiaDesarrolladores.md`

---

## ⚙️ Configuración y Requisitos

### Requisitos Previos

- **Java**: JDK 21 o superior
- **Maven**: incluido Maven Wrapper
- **PostgreSQL**: 14 o superior (para entorno de desarrollo/producción)
- **Docker**: para ejecución en contenedores
- **Git**: Para control de versiones

### Variables de Entorno

#### Desarrollo Local

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/campito_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres123

# OAuth2 - Google
GOOGLE_CLIENT_ID=tu_client_id_google
GOOGLE_CLIENT_SECRET=tu_client_secret_google

# Agente IA (Opcional)
AGENTE_IA_ENABLED=true
GROQ_API_KEY=tu_groq_api_key

# Frontend URL
FRONTEND_URL=http://localhost:3100

# Perfil activo
SPRING_PROFILES_ACTIVE=dev
```

### Configuración de OAuth2

#### Google OAuth2

1. Acceder a [Google Cloud Console](https://console.cloud.google.com/)
2. Crear un nuevo proyecto o seleccionar uno existente
3. Habilitar la API de Google+
4. Ir a "Credenciales" → "Crear credenciales" → "ID de cliente de OAuth 2.0"
5. Configurar pantalla de consentimiento
6. Añadir URIs autorizados:
   - Desarrollo: `http://localhost:8080/login/oauth2/code/google`
   - Producción: `https://tu-dominio.com/login/oauth2/code/google`
7. Copiar Client ID y Client Secret

#### Groq API (Agente IA)

**Nota**: El Agente IA es **opcional**. Si no lo activas, el resto del sistema funciona normalmente.

1. Acceder a [Groq Console](https://console.groq.com/)
2. Crear una cuenta gratuita (no requiere tarjeta de crédito)
3. Ir a "API Keys" → "Create API Key"
4. Copiar la API Key generada
5. Configurar variables de entorno:
   ```bash
   AGENTE_IA_ENABLED=true
   GROQ_API_KEY=gsk_tu_api_key_aqui
   ```

**Características de Groq:**
- ✅ **Gratuito**: Sin necesidad de facturación ni tarjeta de crédito
- ✅ **Ultra-rápido**: Inferencia optimizada con LPUs (Language Processing Units)
- ✅ **Compatible**: API 100% compatible con OpenAI
- ✅ **Modelo**: Llama 3.3 70B Versatile (Meta)
- ✅ **Rate Limits**: 30 req/min, 7000 tokens/min (Tier gratuito)

---

## 🚀 Instalación y Ejecución

### Opción 1: Ejecución Local con Maven

#### 1. Clonar el repositorio
```bash
git clone <url-repositorio>
cd ProyectoGastos/backend
```

#### 2. Configurar variables de entorno
```bash
# Linux/Mac
export GOOGLE_CLIENT_ID=tu_client_id
export GOOGLE_CLIENT_SECRET=tu_client_secret
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/campito_db
export SPRING_DATASOURCE_USERNAME=campito_user
export SPRING_DATASOURCE_PASSWORD=campito_pass

# Agente IA (Opcional)
export AGENTE_IA_ENABLED=true
export GROQ_API_KEY=gsk_tu_groq_api_key

# Windows (CMD)
set GOOGLE_CLIENT_ID=tu_client_id
set GOOGLE_CLIENT_SECRET=tu_client_secret
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/campito_db
set SPRING_DATASOURCE_USERNAME=campito_user
set SPRING_DATASOURCE_PASSWORD=campito_pass
set AGENTE_IA_ENABLED=true
set GROQ_API_KEY=gsk_tu_groq_api_key

# Windows (PowerShell)
$env:GOOGLE_CLIENT_ID="tu_client_id"
$env:GOOGLE_CLIENT_SECRET="tu_client_secret"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/campito_db"
$env:SPRING_DATASOURCE_USERNAME="campito_user"
$env:SPRING_DATASOURCE_PASSWORD="campito_pass"
$env:AGENTE_IA_ENABLED="true"
$env:GROQ_API_KEY="gsk_tu_groq_api_key"
```

#### 3. Compilar el proyecto
```bash
# Con Maven instalado
mvn clean package -DskipTests

# Con Maven Wrapper (recomendado)
./mvnw clean package -DskipTests    # Linux/Mac
.\mvnw.cmd clean package -DskipTests # Windows
```

#### 4. Ejecutar la aplicación
```bash
# Abrir consola PowerShell del editor de código
docker-compose up -d --build

# Para detener
docker-compose down

# Detener y borrar volúmenes
docker-compose down -v
```

#### 5. Verificar la ejecución
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator: http://localhost:8080/actuator (si está habilitado)

---

## 📡 API Endpoints

### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/auth/status` | Obtener estado de autenticación | ✅ |
| GET | `/login/oauth2/code/google` | Callback OAuth2 Google | ❌ |
| POST | `/logout` | Cerrar sesión | ✅ |

### Usuario

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/usuario/me` | Obtener información del usuario actual | ✅ |

### Espacios de Trabajo

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/espaciotrabajo/registrar` | Crear nuevo espacio de trabajo | ✅ |
| PUT | `/api/espaciotrabajo/compartir/{email}/{idEspacioTrabajo}` | Enviar invitación para compartir espacio (crea solicitud pendiente) | ✅ |
| PUT | `/api/espaciotrabajo/solicitud/responder/{idSolicitud}/{aceptada}` | Responder solicitud de colaboración (aceptar/rechazar) | ✅ |
| GET | `/api/espaciotrabajo/solicitudes/pendientes` | Obtener solicitudes pendientes del usuario autenticado | ✅ |
| GET | `/api/espaciotrabajo/listar` | Listar espacios del usuario autenticado | ✅ |
| GET | `/api/espaciotrabajo/miembros/{idEspacioTrabajo}` | Obtener miembros de un espacio | ✅ |

### Transacciones

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/transaccion/registrar` | Registrar nueva transacción | ✅ |
| DELETE | `/api/transaccion/remover/{id}` | Eliminar transacción | ✅ |
| POST | `/api/transaccion/buscar` | Buscar transacciones con filtros | ✅ |
| GET | `/api/transaccion/buscarRecientes/{idEspacio}` | Obtener transacciones recientes | ✅ |

### Motivos y Contactos

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/transaccion/motivo/registrar` | Crear nuevo motivo | ✅ |
| GET | `/api/transaccion/motivo/listar/{idEspacioTrabajo}` | Listar motivos | ✅ |
| POST | `/api/transaccion/contacto/registrar` | Crear nuevo contacto | ✅ |
| GET | `/api/transaccion/contacto/listar/{idEspacioTrabajo}` | Listar contactos | ✅ |

### Cuentas Bancarias

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/cuentaBancaria/crear` | Crear nueva cuenta bancaria | ✅ |
| GET | `/api/cuentaBancaria/listar/{idEspacioTrabajo}` | Listar cuentas | ✅ |
| PUT | `/api/cuentaBancaria/transaccion/{idOrigen}/{idDestino}/{monto}` | Transferir entre cuentas | ✅ |

### Compras a Crédito

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/compraCredito/registrar` | Registrar compra a crédito | ✅ |
| DELETE | `/api/compraCredito/{id}` | Eliminar compra a crédito | ✅ |
| GET | `/api/compraCredito/pendientes/{idEspacioTrabajo}` | Listar compras con cuotas pendientes | ✅ |
| GET | `/api/compraCredito/buscar/{idEspacioTrabajo}` | Buscar todas las compras | ✅ |

### Tarjetas

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/compraCredito/registrarTarjeta` | Registrar nueva tarjeta | ✅ |
| PUT | `/api/compraCredito/tarjeta/{id}` | Modificar día de cierre y vencimiento de tarjeta | ✅ |
| DELETE | `/api/compraCredito/tarjeta/{id}` | Eliminar tarjeta | ✅ |
| GET | `/api/compraCredito/tarjetas/{idEspacioTrabajo}` | Listar tarjetas | ✅ |
| GET | `/api/compraCredito/cuotas/{idTarjeta}` | Listar cuotas por tarjeta | ✅ |

### Resúmenes de Tarjeta

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/compraCredito/pagar-resumen` | Pagar resumen de tarjeta | ✅ |
| GET | `/api/compraCredito/resumenes/tarjeta/{idTarjeta}` | Listar resúmenes por tarjeta | ✅ |
| GET | `/api/compraCredito/resumenes/espacio/{idEspacioTrabajo}` | Listar resúmenes por espacio | ✅ |

### Dashboard

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/dashboard/stats/{idEspacio}` | Obtener estadísticas del dashboard | ✅ |

### Notificaciones

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/notificaciones` | Obtener notificaciones del usuario (últimas 50) | ✅ |
| GET | `/api/notificaciones/no-leidas/count` | Contar notificaciones no leídas | ✅ |
| PUT | `/api/notificaciones/{id}/leer` | Marcar notificación como leída | ✅ |
| PUT | `/api/notificaciones/marcar-todas-leidas` | Marcar todas como leídas | ✅ |
| DELETE | `/api/notificaciones/{id}` | Eliminar notificación | ✅ |
| GET | `/api/notificaciones/stream` | **SSE Stream** para notificaciones en tiempo real (requiere token como query param) | ✅ |

**Nota SSE**: El endpoint SSE acepta el token JWT como query parameter (`?token=xxx`) para compatibilidad con EventSource nativo del navegador.

### Agente IA

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/agente/chat` | Enviar mensaje al agente IA y recibir respuesta completa | ✅ |
| GET | `/api/agente/chat/stream` | Chat con streaming SSE (respuesta token por token) | ✅ |
| GET | `/api/agente/rate-limit/status` | Consultar tokens restantes de rate limit | ✅ |

**Características del Agente:**
- **LLM**: Llama 3.3 70B Versatile vía Groq API
- **Function Calling**: Puede ejecutar herramientas para obtener datos reales del workspace
- **Rate Limit**: 60 mensajes/minuto con burst capacity de 10
- **Auditoría**: Todas las interacciones se registran en `agente_audit_log`
- **Activación**: Requiere `AGENTE_IA_ENABLED=true` y `GROQ_API_KEY`

**Ejemplo de Request:**
```json
{
  "message": "¿Cuál es mi saldo actual?",
  "workspaceId": "uuid-del-workspace",
  "conversationHistory": []
}
```

**Ejemplo de Response:**
```json
{
  "response": "Tu saldo total es de $3,234.27",
  "functionsCalled": ["obtenerDashboardFinanciero"],
  "tokensUsed": 4351
}
```

### Documentación API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

---

## 🔒 Seguridad y Autenticación

### Estrategia de Seguridad

El sistema implementa un modelo de seguridad moderno y robusto basado en:

1. **JWT (JSON Web Tokens)**: Autenticación sin estado (stateless)
2. **OAuth2**: Autenticación delegada a proveedores externos (Google)
3. **Spring Security**: Gestión de autorización y protección de endpoints
4. **CORS**: Configuración para arquitecturas distribuidas (frontend en un hosting, backend en otro hosting)
5. **HTTPS**: Obligatorio en producción

---

### Autenticación JWT

#### ¿Por qué JWT en lugar de Sesiones?

En arquitecturas distribuidas modernas (frontend en un hosting, backend en otro hosting), las **sesiones basadas en cookies NO funcionan** debido a:
- Políticas de **SameSite** que bloquean cookies cross-domain
- Cookies generadas en hosting backend no accesibles desde el hosting frontend.
- Complejidad de configuración CORS para cookies

**JWT resuelve estos problemas:**
- ✅ **Stateless**: No requiere almacenamiento de sesiones en el servidor
- ✅ **Cross-domain**: Funciona perfectamente entre dominios diferentes
- ✅ **Escalable**: Ideal para múltiples instancias de backend
- ✅ **Seguro**: Tokens firmados digitalmente que no pueden ser modificados

#### Componentes de la Implementación JWT

##### 1. JwtTokenProvider

**Ubicación**: `src/main/java/com/campito/backend/security/JwtTokenProvider.java`

Componente responsable de generar y validar tokens JWT.

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs; // 7 días por defecto
    
    // Genera token JWT para un usuario
    public String generateToken(UUID userId, String email) {
        Date expiryDate = new Date(now + jwtExpirationMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    // Valida token JWT
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException ex) {
            logger.error("Token inválido o expirado");
            return false;
        }
    }
}
```

**Características:**
- Firma tokens con algoritmo **HS256** (HMAC-SHA256)
- Claims incluidos: `subject` (userId), `email`, `issuedAt`, `expiration`
- Expiry configurable (por defecto 7 días)
- Validación robusta con manejo de excepciones

##### 2. JwtAuthenticationFilter

**Ubicación**: `src/main/java/com/campito/backend/security/JwtAuthenticationFilter.java`

Filtro que intercepta todas las peticiones HTTP y valida el token JWT.

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        try {
            // 1. Extraer JWT del header Authorization
            String jwt = getJwtFromRequest(request);
            
            // 2. Validar token
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);
                
                // 3. Buscar usuario en BD
                Usuario usuario = usuarioRepository.findById(userId).orElse(null);
                
                // 4. Verificar que esté activo
                if (usuario != null && Boolean.TRUE.equals(usuario.getActivo())) {
                    // 5. Crear principal personalizado
                    CustomOAuth2User customUser = new CustomOAuth2User(
                        Collections.emptyMap(), "sub", usuario
                    );
                    
                    // 6. Establecer autenticación en contexto de Spring Security
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            customUser, null, 
                            Collections.singletonList(new SimpleGrantedAuthority(usuario.getRol()))
                        );
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("No se pudo autenticar usuario", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        // 1. Intentar primero con header Authorization (REST API estándar)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remover "Bearer "
        }
        
        // 2. Si no está en header, buscar en query parameter (SSE)
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            logger.debug("Token JWT extraído de query parameter (SSE)");
            return tokenParam;
        }
        
        return null;
    }
}
```

**Flujo del Filtro:**
1. Extrae el token del header `Authorization: Bearer <token>` (endpoints REST estándar)
2. Si no está en header, busca en query parameter `?token=xxx` (endpoints SSE)
3. Valida el token (firma, expiry)
4. Extrae el userId del token
5. Busca el usuario en la base de datos
6. Verifica que el usuario esté activo
7. Establece la autenticación en el contexto de Spring Security

**🔑 Dual Authentication Support:**

Este filtro soporta **dos métodos de autenticación JWT**:

| Método | Uso | Formato |
|--------|-----|---------|
| **Header** | REST API estándar | `Authorization: Bearer eyJhbGciOiJI...` |
| **Query Param** | SSE stream | `GET /api/notificaciones/stream?token=eyJhbGciOiJI...` |

**¿Por qué query parameter para SSE?**

La API nativa `EventSource` del navegador **NO permite enviar headers personalizados**, lo que imposibilita usar `Authorization: Bearer`. Las alternativas son:

1. ❌ **EventSourcePolyfill**: Funciona pero es menos confiable, más pesado, y requiere dependencias adicionales
2. ✅ **Query Parameter**: Funciona nativamente con `EventSource`, sin polyfills
3. ❌ **Cookies**: No funcionan bien en arquitecturas cross-domain (SameSite policy)

**Implementación Frontend:**
```typescript
const token = localStorage.getItem('auth_token');
const eventSource = new EventSource(
    `${API_URL}/api/notificaciones/stream?token=${encodeURIComponent(token)}`
);
```

**Seguridad del Query Parameter:**
- ✅ Token firmado digitalmente con HMAC-SHA256
- ✅ Conexión HTTPS en producción (token encriptado en tránsito)
- ✅ Token con expiry (7 días por defecto)
- ✅ Validación estricta igual que headers
- ⚠️ Solo usar para SSE, no para API REST estándar

##### 3. OAuth2AuthenticationSuccessHandler

**Ubicación**: `src/main/java/com/campito/backend/security/OAuth2AuthenticationSuccessHandler.java`

Handler que captura el éxito de OAuth2 y genera el token JWT.

```java
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        // 1. Obtener usuario autenticado de OAuth2
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        
        // 2. Generar token JWT
        String token = jwtTokenProvider.generateToken(
            customUser.getUsuario().getId(),
            customUser.getUsuario().getEmail()
        );
        
        // 3. Redirigir al frontend con el token en la URL
        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/oauth-callback")
                .queryParam("token", token)
                .build()
                .toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

**¿Por qué pasar el token en la URL?**
- Es una redirección de servidor (backend → frontend)
- El frontend no tiene acceso a cookies cross-domain
- La URL es el único canal seguro para transferir el token en esta redirección
- El frontend inmediatamente lo guarda en `localStorage` y limpia la URL

##### 4. SecurityConfig - Configuración STATELESS

**Ubicación**: `src/main/java/com/campito/backend/config/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            // ⚡ CLAVE: Sesiones STATELESS (sin estado)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/**")
                .permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
            )
            // ⚡ Agregar filtro JWT ANTES del filtro de autenticación de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Cambios clave:**
- `SessionCreationPolicy.STATELESS`: No crea ni usa sesiones HTTP
- JWT Filter agregado antes del filtro de autenticación estándar
- OAuth2 ahora usa el `OAuth2AuthenticationSuccessHandler` personalizado

#### Configuración en application.properties

##### application-prod.properties

```properties
# JWT Configuration
jwt.secret=${JWT_SECRET:produccion_jwt_secret_temporal_cambiar_urgente_minimo_256_bits_1234567890}
jwt.expiration=604800000  # 7 días en milisegundos
```

**⚠️ IMPORTANTE:**
- `JWT_SECRET` **DEBE** configurarse como variable de entorno en producción (Render, AWS, etc.)
- El valor por defecto es temporal y **NO seguro** para producción
- Generar secret seguro con: `openssl rand -base64 32` (Linux/Mac) o `[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))` (PowerShell)
- Mínimo 256 bits (32 bytes) para HS256

##### application-dev.properties

```properties
# JWT Configuration (desarrollo)
jwt.secret=${JWT_SECRET:desarrollo_secreto_jwt_super_seguro_minimo_256_bits_12345678901234567890}
jwt.expiration=604800000
```

#### Flujo Completo de Autenticación OAuth2 + JWT

```
1. Usuario → /login → Click "Continuar con Google"
   ↓
2. Frontend → Redirige a: /oauth2/authorization/google
   ↓
3. Backend → Redirige a: Google OAuth2
   ↓
4. Usuario → Autoriza en Google
   ↓
5. Google → Callback a: /login/oauth2/code/google
   ↓
6. Backend → CustomOidcUserService:
   ├─ Busca/crea usuario en PostgreSQL
   ├─ Actualiza fecha de último acceso
   └─ Devuelve CustomOAuth2User
   ↓
7. Backend → OAuth2AuthenticationSuccessHandler:
   ├─ Genera token JWT (firmado con secret)
   └─ Redirige a: frontend/oauth-callback?token=<JWT>
   ↓
8. Frontend → Captura token de URL
   ├─ Guarda en localStorage: auth_token
   └─ Redirige al dashboard
   ↓
9. Frontend → Todas las peticiones subsecuentes:
   ├─ Incluye header: Authorization: Bearer <token>
   └─ Backend valida con JwtAuthenticationFilter
   ↓
10. ✅ Usuario autenticado durante 7 días (o hasta logout)
```

#### Endpoints Protegidos

Todos los endpoints excepto los explícitamente públicos requieren un JWT válido:

**Públicos (sin autenticación):**
- `/api/auth/**` - Verificación de estado de autenticación
- `/oauth2/**` - OAuth2 authorization
- `/login/oauth2/**` - OAuth2 callback

**Protegidos (requieren JWT):**
- `/swagger-ui/**` - Documentación API (Swagger UI)
- `/v3/api-docs/**` - OpenAPI spec
- `/api/transaccion/**`
- `/api/espaciotrabajo/**`
- `/api/dashboard/**`
- `/api/tarjetas/**`
- `/api/compras-credito/**`
- Y todos los demás endpoints de la API

#### Seguridad del Token JWT

**Almacenamiento:**
- Backend: Secret en variable de entorno `JWT_SECRET`
- Frontend: Token en `localStorage` (no en cookies para evitar problemas cross-domain)

**Validaciones:**
- Firma digital (HMAC-SHA256)
- Fecha de expiración (7 días)
- Usuario existe y está activo
- Token no ha sido modificado

**Rotación:**
- Se recomienda rotar `JWT_SECRET` cada 3-6 meses
- Al cambiar el secret, todos los tokens anteriores se invalidan

**Revocación:**
- Logout: Frontend elimina el token de `localStorage`
- Desactivación: Backend marca usuario como `activo=false`

#### Variables de Entorno Requeridas

**Producción (Render, AWS, etc.):**
```bash
# OAuth2
GOOGLE_CLIENT_ID=<client_id_de_google_console>
GOOGLE_CLIENT_SECRET=<client_secret_de_google_console>
FRONTEND_URL=https://tu-frontend.com

# JWT (CRÍTICO)
JWT_SECRET=<generar_con_openssl_rand_base64_32>

# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

**Desarrollo (local):**
```bash
# .env o docker-compose.yml
GOOGLE_CLIENT_ID=<tu_client_id>
GOOGLE_CLIENT_SECRET=<tu_client_secret>
FRONTEND_URL=http://localhost:3100
# JWT_SECRET usa valor por defecto de application-dev.properties
```

#### Troubleshooting JWT

**Error: "Could not resolve placeholder 'JWT_SECRET'"**
- **Causa**: Variable de entorno `JWT_SECRET` no configurada en producción
- **Solución**: Agregar `JWT_SECRET` en el panel de variables de entorno del hosting

**Error: "Token JWT inválido"**
- **Causa**: Secret no coincide o token modificado
- **Solución**: Verificar que `JWT_SECRET` sea el mismo que se usó para generar el token

**Usuario se desautentica después de 7 días**
- **Causa**: Token expiró (comportamiento esperado)
- **Solución**: Usuario debe volver a hacer login

**Token no se envía en peticiones**
- **Causa**: Problema en el frontend (ver README_FRONTEND.md)
- **Solución**: Verificar que el token esté en `localStorage` y se agregue al header

---

### Protección contra Vulnerabilidades IDOR

El sistema implementa **protección completa contra IDOR (Insecure Direct Object Reference)**, una vulnerabilidad crítica que permitiría a usuarios autenticados acceder a recursos de otros usuarios mediante la manipulación de IDs.

#### ¿Qué es IDOR?

IDOR ocurre cuando una aplicación expone referencias directas a objetos internos (IDs) sin validar que el usuario autenticado tiene permiso para acceder a ellos. 

**Ejemplo de ataque:**
```
Usuario A tiene EspacioTrabajo ID: a3b8c9d4-...
Usuario B intenta: GET /api/transaccion/buscar?espacioId=a3b8c9d4-...
Sin protección: Usuario B accede a datos financieros de Usuario A ❌
Con protección: Sistema rechaza la petición con error 403 ✅
```

#### Implementación de la Protección

##### 1. SecurityService - Validación Centralizada

Se implementó `SecurityService` como componente central de autorización:

```java
@Service
public interface SecurityService {
    // Obtener usuario autenticado desde el contexto de Spring Security
    UUID getAuthenticatedUserId();
    
    // Validar acceso a espacio de trabajo (participante)
    void validateWorkspaceAccess(UUID workspaceId);
    
    // Validar permisos de administrador
    void validateWorkspaceAdmin(UUID workspaceId);
    
    // Validar ownership de recursos específicos
    void validateTransactionOwnership(Long transactionId);
    void validateCompraCreditoOwnership(Long compraCreditoId);
    void validateCuentaBancariaOwnership(Long cuentaBancariaId);
    void validateTarjetaOwnership(Long tarjetaId);
}
```

##### 2. Cambio de IDs Secuenciales a UUIDs

Para prevenir ataques de enumeración, se cambió el tipo de ID de `Long` (secuencial) a `UUID` (no predecible) en las entidades principales:

- **Usuario**: `UUID` (128 bits, 2^122 combinaciones posibles)
- **EspacioTrabajo**: `UUID` (imposible de adivinar)

**Antes (Vulnerable):**
```
GET /api/espaciotrabajo/listar/1
GET /api/espaciotrabajo/listar/2  ← Fácil de enumerar
GET /api/espaciotrabajo/listar/3
```

**Después (Seguro):**
```
GET /api/espaciotrabajo/listar
↳ Solo devuelve espacios del usuario autenticado
```

##### 3. Validación en Controladores

Todos los endpoints críticos validan permisos ANTES de procesar la petición:

```java
@PostMapping("/registrar")
public ResponseEntity<?> registrarTransaccion(@RequestBody TransaccionDTORequest dto) {
    // Validar que el usuario tiene acceso al espacio de trabajo
    securityService.validateWorkspaceAccess(dto.idEspacioTrabajo());
    
    // Procesar solo si tiene permisos
    return ResponseEntity.ok(transaccionService.registrarTransaccion(dto));
}

@DeleteMapping("/remover/{id}")
public ResponseEntity<Void> removerTransaccion(@PathVariable Long id) {
    // Validar que la transacción pertenece a un espacio del usuario
    securityService.validateTransactionOwnership(id);
    
    transaccionService.removerTransaccion(id);
    return ResponseEntity.ok().build();
}
```

##### 4. Eliminación de Parámetros Inseguros

Se eliminaron endpoints que aceptaban IDs de usuario como parámetros:

**Antes (Vulnerable):**
```java
@GetMapping("/listar/{idUsuario}")
public ResponseEntity<?> listar(@PathVariable Long idUsuario) {
    // ❌ Cualquier usuario autenticado puede pedir datos de otro
    return ResponseEntity.ok(service.listarPorUsuario(idUsuario));
}
```

**Después (Seguro):**
```java
@GetMapping("/listar")
public ResponseEntity<?> listarMisEspacios() {
    // ✅ Solo obtiene espacios del usuario autenticado
    UUID userId = securityService.getAuthenticatedUserId();
    return ResponseEntity.ok(service.listarPorUsuario(userId));
}
```

##### 5. Validación en Múltiples Capas

**Capa Controller:**
- Validación inicial de acceso al espacio de trabajo
- Obtención del usuario autenticado desde SecurityContext

**Capa Service:**
- Validación adicional antes de operaciones críticas
- Verificación de ownership en eliminaciones/modificaciones

**Capa Repository:**
- Queries que incluyen filtros por `usuariosParticipantes`
- Joins automáticos para validar pertenencia

##### 6. Manejo de Excepciones de Seguridad

```java
@RestControllerAdvice
public class ControllerAdvisor {
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionInfo> handleUnauthorized(UnauthorizedException ex) {
        // 401: Usuario no autenticado
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ExceptionInfo(ex.getMessage()));
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ExceptionInfo> handleForbidden(ForbiddenException ex) {
        // 403: Usuario autenticado pero sin permisos
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ExceptionInfo(ex.getMessage()));
    }
}
```

#### Endpoints Protegidos

Todos estos endpoints están protegidos contra IDOR:

| Endpoint | Tipo Validación | Método SecurityService |
|----------|-----------------|------------------------|
| `POST /api/transaccion/registrar` | Workspace Access | `validateWorkspaceAccess()` |
| `DELETE /api/transaccion/remover/{id}` | Transaction Ownership | `validateTransactionOwnership()` |
| `GET /api/espaciotrabajo/listar` | User Context | `getAuthenticatedUserId()` |
| `PUT /api/espaciotrabajo/compartir/{email}/{id}` | Admin Rights | `validateWorkspaceAdmin()` |
| `POST /api/compracredito/registrar` | Workspace Access | `validateWorkspaceAccess()` |
| `DELETE /api/compracredito/{id}` | Compra Ownership | `validateCompraCreditoOwnership()` |
| `GET /api/cuentabancaria/listar/{idEspacio}` | Workspace Access | `validateWorkspaceAccess()` |
| `GET /api/dashboard/stats/{idEspacio}` | Workspace Access | `validateWorkspaceAccess()` |

#### Beneficios de la Implementación

✅ **Prevención de Acceso No Autorizado**: Usuarios no pueden acceder a recursos de otros usuarios  
✅ **Auditoría Completa**: Todos los intentos de acceso no autorizado son registrados en logs  
✅ **Código Centralizado**: Lógica de seguridad en un solo lugar (SecurityService)  
✅ **Reutilizable**: Métodos de validación compartidos entre controladores  
✅ **Mensajes Claros**: Errores 401/403 con mensajes descriptivos para debugging  
✅ **Enumeración Prevista**: UUIDs impiden adivinar IDs válidos  
✅ **Compliance**: Cumple con OWASP Top 10 (A01: Broken Access Control)  

#### Referencias Técnicas

- **OWASP A01:2021 – Broken Access Control**: https://owasp.org/Top10/A01_2021-Broken_Access_Control/
- **CWE-639**: Authorization Bypass Through User-Controlled Key
- **IDOR Prevention Cheat Sheet**: https://cheatsheetseries.owasp.org/cheatsheets/Insecure_Direct_Object_Reference_Prevention_Cheat_Sheet.html

### Validaciones Personalizadas

El sistema incluye validadores personalizados para:

- **ValidNombre**: Nombres no vacíos y con formato válido
- **ValidMonto**: Montos positivos y con máximo 2 decimales
- **ValidDescripcion**: Descripciones con longitud controlada
- **ValidSaldoActual**: Saldos iniciales válidos

---

## � Observabilidad y Métricas

### Introducción

El sistema implementa una **estrategia completa de observabilidad** para monitoreo proactivo en producción. Permite detectar problemas antes de que afecten a los usuarios, optimizar el rendimiento y tomar decisiones basadas en datos reales.

### ¿Por qué es crítico en este proyecto?

1. **Aplicación Financiera**: Requiere alta confiabilidad
2. **Recursos Limitados**: Desplegado en servidores con 1GB RAM, necesita monitoreo constante
3. **Automatización Crítica**: El scheduler de resúmenes debe funcionar sin fallos
4. **Multi-Tenant**: Detectar problemas específicos por espacio de trabajo

### Stack de Observabilidad

```
Spring Boot App → Actuator → Micrometer → Prometheus → Grafana
     ↓              ↓           ↓             ↓           ↓
 Instrumenta   Expone en   Convierte a   Almacena    Visualiza
  el código   /actuator   formato std   series      dashboards
```

### Métricas Implementadas

#### 1. Métricas de Lógica de Negocio

**Transacciones**:
- `negocio_transacciones_creadas_total`: Contador de transacciones registradas
  - Tags: `tipo` (GASTO, INGRESO, TRANSFERENCIA), `espacio_trabajo_id`
- `negocio_transacciones_eliminadas_total`: Contador de transacciones eliminadas
  - Tags: `tipo`, `espacio_trabajo_id`

**Compras a Crédito**:
- `negocio_compras_credito_creadas_total`: Contador de compras en cuotas registradas
  - Tags: `cuotas`, `tarjeta_id`, `espacio_trabajo_id`
- `negocio_cuotas_pagadas_total`: Contador de cuotas individuales pagadas
  - Tags: `tarjeta_id`, `espacio_trabajo_id`
- `negocio_resumenes_pagados_total`: Contador de resúmenes de tarjeta pagados
  - Tags: `tarjeta_id`, `espacio_trabajo_id`

**Resúmenes (Scheduler)**:
- `negocio_resumenes_generados_total`: Contador de resúmenes cerrados automáticamente
  - Tags: `tarjeta_id`
- `negocio_resumenes_errores_total`: Contador de errores en el scheduler
  - Tags: `tarjeta_id`
- `negocio_resumenes_duracion_seconds`: Timer de duración del proceso de cierre

**Notificaciones**:
- `negocio_notificaciones_enviadas_total`: Contador de notificaciones enviadas
  - Tags: `tipo` (CIERRE_TARJETA, VENCIMIENTO_RESUMEN, INVITACION_ESPACIO, etc.)
- `negocio_notificaciones_leidas_total`: Contador de notificaciones marcadas como leídas
  - Tags: `tipo`

**Conexiones Tiempo Real**:
- `negocio_sse_conexiones_activas`: Gauge de conexiones SSE activas
- `negocio_cuotas_pendientes`: Gauge de cuotas pendientes de pago
  - Tags: `espacio_trabajo_id`

#### 2. Métricas del Sistema (Automáticas)

**JVM**:
- `jvm_memory_used_bytes`: Memoria heap usada
- `jvm_memory_max_bytes`: Memoria heap máxima
- `jvm_gc_pause_seconds`: Tiempo de pausa por Garbage Collection
- `jvm_threads_live_threads`: Threads activos

**HTTP**:
- `http_server_requests_seconds`: Latencia de endpoints
  - Tags: `method`, `uri`, `status`
- `http_server_requests_seconds_count`: Total de peticiones
- `http_server_requests_seconds_max`: Latencia máxima

**Base de Datos**:
- `hikaricp_connections_active`: Conexiones activas al pool
- `hikaricp_connections_pending`: Peticiones esperando conexión
- `hikaricp_connections`: Total de conexiones

### Implementación Técnica

#### MetricsConfig.java

Clase de configuración centralizada que define constantes y Gauges:

```java
@Configuration
public class MetricsConfig {
    
    // Constantes para nombres de métricas
    public static class MetricNames {
        public static final String TRANSACCIONES_CREADAS = "negocio_transacciones_creadas";
        public static final String COMPRAS_CREDITO_CREADAS = "negocio_compras_credito_creadas";
        public static final String RESUMENES_GENERADOS = "negocio_resumenes_generados";
        // ...
    }
    
    // Constantes para tags
    public static class TagNames {
        public static final String TIPO_TRANSACCION = "tipo";
        public static final String ESPACIO_TRABAJO = "espacio_trabajo_id";
        // ...
    }
    
    // Gauges para métricas en tiempo real
    @Bean
    public AtomicInteger cuotasPendientesGauge(MeterRegistry registry) {
        return registry.gauge("negocio_cuotas_pendientes", new AtomicInteger(0));
    }
    
    @Bean
    public AtomicInteger sseConexionesActivasGauge(MeterRegistry registry) {
        return registry.gauge("negocio_sse_conexiones_activas", new AtomicInteger(0));
    }
}
```

#### Instrumentación en Servicios

**Ejemplo: TransaccionServiceImpl**
```java
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {
    
    private final MeterRegistry meterRegistry;
    
    @Override
    @Transactional
    public TransaccionDTOResponse registrarTransaccion(TransaccionDTORequest request) {
        // Lógica de negocio...
        Transaccion saved = repository.save(transaccion);
        
        // Incrementar métrica
        Counter.builder(MetricNames.TRANSACCIONES_CREADAS)
            .tag(TagNames.TIPO_TRANSACCION, saved.getTipo().name())
            .tag(TagNames.ESPACIO_TRABAJO, saved.getIdEspacioTrabajo().toString())
            .register(meterRegistry)
            .increment();
        
        return mapper.toResponse(saved);
    }
}
```

**Ejemplo: ResumenScheduler con Timer**
```java
@Scheduled(cron = "0 0 0 * * *")  // Ejecuta diariamente a medianoche
public void cerrarResumenesDiarios() {
    Timer.Sample sample = Timer.start(meterRegistry);
    
    try {
        // Lógica de cierre de resúmenes
        boolean exito = cerrarResumenTarjeta(tarjeta);
        
        if (exito) {
            Counter.builder(MetricNames.RESUMENES_GENERADOS)
                .tag(TagNames.TARJETA_ID, tarjeta.getId().toString())
                .register(meterRegistry)
                .increment();
        }
    } catch (Exception e) {
        Counter.builder(MetricNames.RESUMENES_ERRORES)
            .tag(TagNames.TARJETA_ID, tarjeta.getId().toString())
            .register(meterRegistry)
            .increment();
    } finally {
        sample.stop(Timer.builder(MetricNames.RESUMENES_TIMER).register(meterRegistry));
    }
}
```

### Endpoints de Actuator

**Salud de la aplicación**:
```bash
GET /actuator/health
# Respuesta: {"status":"UP"}
```

**Métricas en formato Prometheus**:
```bash
GET /actuator/prometheus
# Respuesta:
# TYPE negocio_transacciones_creadas_total counter
negocio_transacciones_creadas_total{tipo="GASTO",espacio_trabajo_id="123"} 150.0
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 450000000.0
```

**Lista de todas las métricas**:
```bash
GET /actuator/metrics
# Lista nombres de métricas disponibles
```

**Detalle de métrica específica**:
```bash
GET /actuator/metrics/negocio_transacciones_creadas_total
# JSON con valor actual y tags
```

### Configuración

**application.properties**:
```properties
# Habilitar Actuator y métricas
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true

# Seguridad de endpoints (solo en producción)
management.server.port=9090  # Puerto separado para métricas
```

### Integración con Grafana

#### Grafana Cloud (Recomendado para recursos limitados)

1. **Registro**: Cuenta gratuita en [grafana.com](https://grafana.com)
2. **Grafana Agent**: Instalación en servidor (consume ~30 MB RAM)
3. **Configuración**:
   ```yaml
   scrape_configs:
     - job_name: 'spring-boot-backend'
       static_configs:
         - targets: ['backend:8080']
       metrics_path: '/actuator/prometheus'
   ```

#### Dashboards Prediseñados

Se incluye documentación completa de 11 paneles en:
- **[GuiaPanelesGrafana.md](../docs/Observabilidad/GuiaPanelesGrafana.md)**

**Dashboards disponibles**:
1. Transacciones por minuto (rate)
2. Top 5 Espacios de Trabajo más activos
3. Compras a Crédito por cuotas
4. Resúmenes Generados vs Errores
5. Conexiones SSE Activas
6. Notificaciones Enviadas por tipo
7. Ratio de Eliminación de Transacciones
8. Cuotas Pagadas por Espacio
9. Latencia del Scheduler
10. Notificaciones Leídas vs No Leídas
11. Tasa de Lectura de Notificaciones

### Alertas Recomendadas

**Críticas**:
- ❗ Scheduler con errores: `resumenes_errores > 0`
- ❗ Memoria JVM alta: `jvm_memory_used / jvm_memory_max > 0.85`
- ❗ Latencia alta: `http_server_requests_seconds > 3s`

**Advertencias**:
- ⚠️ Ratio de eliminación alto: `transacciones_eliminadas / transacciones_creadas > 0.25`
- ⚠️ Pool de conexiones saturado: `hikaricp_connections_active / hikaricp_connections > 0.9`

### Consideraciones de Rendimiento

**Impacto de Métricas**:
- **RAM adicional**: ~5-10 MB (1-2% del heap)
- **CPU adicional**: <0.1% (solo en incrementos)
- **Latencia**: <1ms por operación instrumentada
- **Almacenamiento**: 0 bytes (métricas en memoria)
- **Red**: ~10 KB/s de tráfico de scraping

**Optimizaciones implementadas**:
- ✅ Tags con cardinalidad limitada (evita explosión de series)
- ✅ Lazy registration (métricas se crean bajo demanda)
- ✅ Contadores sin sincronización (thread-safe sin locks)
- ✅ Gauges con AtomicInteger (lecturas sin bloqueo)

### Referencias Técnicas

- **Micrometer**: https://micrometer.io/
- **Spring Boot Actuator**: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- **Prometheus**: https://prometheus.io/docs/introduction/overview/
- **Grafana Dashboards**: https://grafana.com/docs/grafana/latest/dashboards/

---

## �🗃 Migraciones de Base de Datos

### Flyway

El proyecto utiliza Flyway para gestionar el versionado y evolución del esquema de base de datos.

### Scripts de Migración

#### V1: Creación Inicial del Esquema
- Tablas principales: usuarios, espacios_trabajo, transacciones, motivos, contactos, presupuestos, notificaciones
- Relaciones y constraints iniciales

#### V2: Cuentas Bancarias
- Tabla: cuentas_bancarias
- Actualización de transacciones para soportar cuentas bancarias

#### V3: Sistema de Crédito
- Tablas: tarjetas, compras_credito, cuotas_credito
- Gestión completa de compras en cuotas

#### V4: Resúmenes de Tarjeta
- Tabla: resumenes
- Estados y relaciones con cuotas y transacciones

#### V5: Optimización de Índices
- Índices en fechas y foreign keys
- Mejora de rendimiento en consultas frecuentes

#### V6: Tabla de Agregación
- Tabla: gastos_ingresos_mensuales
- Optimización de cálculos de dashboard

#### V14: Sistema de Notificaciones
- Tabla: notificaciones
- SSE (Server-Sent Events) para tiempo real
- Tipos: CIERRE_TARJETA, VENCIMIENTO_RESUMEN, INVITACION_ESPACIO, etc.

#### V15: Índices de Notificaciones
- Índices optimizados en usuario_id, workspace_id, fecha
- Mejora de rendimiento en consultas de notificaciones

#### V16: Auditoría del Agente IA
- Tabla: agente_audit_log
- Registro completo de interacciones con el LLM
- Tracking de funciones llamadas, tokens consumidos y errores
- Compliance y análisis de uso

### Ejecución de Migraciones

```bash
# Flyway ejecuta automáticamente al iniciar la aplicación
# spring.flyway.enabled=true (por defecto)

# Verificar estado de migraciones
./mvnw flyway:info

# Ejecutar migraciones pendientes
./mvnw flyway:migrate

# Reparar migraciones (si hay problemas)
./mvnw flyway:repair

# Limpiar base de datos (CUIDADO en producción)
./mvnw flyway:clean
```

### Convenciones

- **Nomenclatura**: `V{VERSION}__{DESCRIPCION}.sql`
- **Ejemplo**: `V7__add_index_transacciones_fecha.sql`
- **Versionado**: Secuencial (V1, V2, V3...)
- **Descripción**: Snake_case, descriptiva

---

## 🧪 Testing

### Estructura de Tests

```
src/test/
├── java/com/campito/backend/
│   ├── controller/          # Tests de controladores
│   ├── service/             # Tests de servicios
│   └── repository/          # Tests de repositorios
└── resources/
    └── application.properties # Configuración H2 para tests
```

### Ejecución de Tests

```bash
# Todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest=TransaccionServiceTest

# Con coverage
./mvnw clean test jacoco:report

# Sin tests (para build rápido)
./mvnw clean package -DskipTests
```

### Configuración de Testing

- **Base de Datos**: H2 en memoria
- **Framework**: JUnit 5 + Spring Boot Test
- **Mocking**: Mockito
- **Assertions**: AssertJ + JUnit Assertions

---

## � CI/CD - Integración y Despliegue Continuo

### Visión General

El proyecto implementa un pipeline completo de **CI/CD (Continuous Integration / Continuous Deployment)** utilizando **GitHub Actions** para automatizar testing, construcción y despliegue en producción.

### 🎯 Objetivos

- ✅ **Calidad Automatizada**: Ejecutar tests en cada cambio de código
- ✅ **Despliegue Rápido**: Reducir tiempo de despliegue de ~15 minutos a ~5-7 minutos
- ✅ **Cero Errores Humanos**: Eliminar pasos manuales propensos a fallos
- ✅ **Trazabilidad**: Registro completo de cada despliegue
- ✅ **Rollback Fácil**: Revertir a versión anterior con un simple revert del commit

### 📋 Workflows Implementados

#### 1. CI - Continuous Integration ([ci.yml](../.github/workflows/ci.yml))

**Trigger**: Push o Pull Request a `develop` o `main`

**Acciones**:
1. Checkout del código
2. Configuración de Java 21 (Temurin)
3. Caché de dependencias Maven
4. Ejecución de todos los tests
5. Reporte de resultados

**Propósito**: Validar que los cambios no rompan funcionalidad existente.

```yaml
# Flujo simplificado
Checkout → Setup Java 21 → Maven Cache → Run Tests → Report
```

#### 2. CD - Continuous Deployment ([cd.yml](../.github/workflows/cd.yml))

**Trigger**: Push a `main` (solo después de que CI pase)

**Fases**:

##### Fase 1: Testing
- Ejecuta todos los tests de Maven
- Si fallan, el pipeline se detiene

##### Fase 2: Build & Push
- Construye la imagen Docker (en servidores de GitHub)
- Usa cache de Docker para builds más rápidos
- Push automático a Docker Hub
- Tag: `usuario/proyecto-gastos-backend:latest`

##### Fase 3: Deploy
- Conexión SSH al servidor Oracle Cloud
- Pull de la nueva imagen desde Docker Hub
- Reinicio del contenedor de backend
- Limpieza de imágenes antiguas
- Verificación de logs de arranque

```yaml
# Flujo completo
Tests → Build Docker Image → Push to Docker Hub → SSH to Server → Pull & Restart → Verify
```

### 🔐 Seguridad: GitHub Secrets

Todo el flujo funciona sin exponer credenciales gracias a **GitHub Secrets**:

| Secret | Descripción | Uso |
|--------|-------------|-----|
| `DOCKERHUB_USERNAME` | Usuario de Docker Hub | Login en Docker Hub |
| `DOCKERHUB_TOKEN` | Token de acceso (no contraseña) | Autenticación segura |
| `ORACLE_SSH_HOST` | IP del servidor Oracle Cloud | Conexión SSH |
| `ORACLE_SSH_USERNAME` | Usuario SSH (normalmente `ubuntu`) | Autenticación SSH |
| `ORACLE_SSH_KEY` | Clave privada SSH completa | Conexión segura al servidor |

**Ventajas de GitHub Secrets**:
- 🔒 Cifrado de extremo a extremo
- 🙈 No aparecen en logs (reemplazados por `***`)
- 🚫 No accesibles después de guardarlos (ni siquiera por ti)
- ✅ Solo disponibles durante la ejecución del workflow

### 📊 Comparación: Antes vs Después

| Aspecto | Antes (Manual) | Ahora (CI/CD) |
|---------|----------------|---------------|
| **Tests** | Ejecutar localmente (opcional) | Automáticos en cada push |
| **Build** | `docker build` en PC local | Build en GitHub servidores |
| **Push a Registry** | `docker push` manual | Automático tras tests exitosos |
| **Deploy** | SSH + comandos manuales | Automático en `main` |
| **Tiempo Total** | ~15 minutos (tu tiempo) | ~5-7 minutos (sin tu intervención) |
| **Riesgo de Error** | Alto (pasos olvidados) | Mínimo (proceso estandarizado) |
| **Auditoría** | Ninguna | Completa en GitHub Actions |

### 🚀 Flujo de Trabajo para Desarrolladores

#### Desarrollo Normal (feature branches)
```bash
git checkout -b feature/nueva-funcionalidad
# ... hacer cambios ...
git commit -m "feat: agregar nueva funcionalidad"
git push origin feature/nueva-funcionalidad
# → Crear Pull Request a 'develop'
# → CI se ejecuta automáticamente
# → Revisar resultados antes de merge
```

#### Despliegue a Producción
```bash
# Merge de develop a main
git checkout main
git merge develop
git push origin main
# → CI ejecuta tests
# → CD construye imagen Docker
# → CD despliega automáticamente a Oracle Cloud
# → Backend actualizado en ~7 minutos
```

### 📝 Logs y Monitoreo

**Ver ejecuciones del workflow**:
1. Ir a la pestaña **Actions** en GitHub
2. Seleccionar el workflow (`CI - Tests` o `CD - Deploy a Producción`)
3. Ver logs en tiempo real de cada paso

**Estados posibles**:
- 🟢 **Success**: Todo correcto
- 🔴 **Failure**: Algún paso falló (ver logs para detalles)
- 🟡 **In Progress**: Ejecutándose actualmente
- ⚪ **Skipped**: No se ejecutó (ej: CD se salta si CI falla)

### 🛠️ Mantenimiento del Pipeline

**Actualizar secretos**:
- GitHub → Settings → Secrets and variables → Actions
- Update/Add secret según sea necesario
- Los workflows tomarán los nuevos valores automáticamente

**Modificar workflows**:
- Editar archivos en `.github/workflows/`
- Los cambios aplican en el siguiente push
- Probar en rama de feature antes de merge a main

### 📚 Recursos de Configuración

Para configurar los secretos necesarios, consulta:
- 📘 [Guía de Configuración de Secrets](../docs/ConfiguracionSecretsCD.md)
- 🐳 [Guía de Despliegue en Producción](../docs/DespliegueProduccion.md)

### ⚠️ Notas Importantes

1. **Solo `main` despliega**: Los cambios en `develop` ejecutan solo CI (tests)
2. **Zero Downtime**: Durante el reinicio del contenedor (~10-20s) habrá un error 502 temporal
3. **Rollback**: Si algo falla, revert el commit y push para redesplegar la versión anterior
4. **Costos**: GitHub Actions es gratuito para repositorios públicos (2000 min/mes para privados)

---

## �🐳 Despliegue con Docker

### Dockerfile Multi-Stage

El proyecto utiliza un Dockerfile optimizado con dos etapas:

#### Etapa 1: Builder
- Imagen base: `maven:3.9-eclipse-temurin-21`
- Maven Wrapper para independencia de versión
- Descarga de dependencias (cacheadas)
- Compilación del proyecto
- Generación del JAR

#### Etapa 2: Runner
- Imagen base: `eclipse-temurin:21-jre-alpine` (ligera y segura)
- Solo copia el JAR compilado
- Expone puerto 8080
- Ejecuta la aplicación

**Ventajas de Alpine:**
- ✅ **Tamaño reducido**: ~150MB menos que Debian
- ✅ **Seguridad**: Superficie de ataque mínima, menos vulnerabilidades
- ✅ **Compatible**: Spring AI con Groq usa solo HTTP REST (sin librerías nativas)
- ✅ **Producción**: Óptima para despliegue en la nube

### Construcción de Imagen

```bash
# Construcción básica
docker build -t campito-backend:latest .

# Con etiqueta específica
docker build -t campito-backend:1.0.0 .

# Sin caché (build completo)
docker build --no-cache -t campito-backend:latest .
```

### Ejecución del Contenedor

```bash
# Ejecución básica
docker run -p 8080:8080 campito-backend:latest

# Con variables de entorno
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/db \
  -e GOOGLE_CLIENT_ID=client_id \
  -e GOOGLE_CLIENT_SECRET=client_secret \
  campito-backend:latest

# En segundo plano
docker run -d -p 8080:8080 --name campito-backend campito-backend:latest

# Ver logs
docker logs -f campito-backend
```

### Docker Compose

Archivo `docker-compose.yml` en la raíz del proyecto incluye:
- Backend (Spring Boot)
- Base de datos PostgreSQL
- Red interna
- Volúmenes para persistencia

```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

---

## ✨ Mejores Prácticas Implementadas

### Código Limpio

- ✅ **Nombres descriptivos**: Variables, métodos y clases con nombres significativos
- ✅ **Funciones pequeñas**: Métodos con responsabilidad única
- ✅ **Comentarios JavaDoc**: Documentación en interfaces y métodos públicos
- ✅ **Constantes**: Magic numbers y strings en constantes

### Arquitectura

- ✅ **Separación de capas**: Controller → Service → Repository
- ✅ **DTOs**: Separación modelo dominio vs transferencia
- ✅ **Inyección de dependencias**: Constructor injection con Lombok
- ✅ **Interfaces**: Programación orientada a interfaces en servicios

### Seguridad

- ✅ **OAuth2**: Autenticación delegada segura
- ✅ **Validaciones**: Bean Validation en todos los DTOs
- ✅ **Auditoría**: Registro de usuario y timestamp en operaciones críticas
- ✅ **Sensibilidad de datos**: Solo últimos 4 dígitos de tarjetas

### Persistencia

- ✅ **Transacciones**: @Transactional en operaciones compuestas
- ✅ **Migraciones**: Flyway para control de versiones del esquema
- ✅ **Índices**: Optimización de consultas frecuentes
- ✅ **Lazy Loading**: Carga diferida de relaciones

### Rendimiento

- ✅ **Pool de conexiones**: HikariCP configurado
- ✅ **Caché agregado**: Tabla gastos_ingresos_mensuales
- ✅ **Consultas optimizadas**: Queries específicas en repositorios
- ✅ **DTOs proyectados**: Solo datos necesarios en respuestas

### Mantenibilidad

- ✅ **Logging**: Logback con niveles configurables
- ✅ **Manejo de errores**: ControllerAdvisor centralizado
- ✅ **Documentación**: Swagger/OpenAPI automático
- ✅ **Profiles**: Configuraciones por entorno (dev/prod)

### DevOps

- ✅ **Docker**: Contenerización con multi-stage build
- ✅ **CI/CD**: GitHub Actions para integración y despliegue continuo
- ✅ **Automatización**: Testing y deployment automáticos
- ✅ **Docker Hub**: Registro centralizado de imágenes
- ✅ **Maven Wrapper**: Independencia de versión de Maven
- ✅ **Variables de entorno**: Configuración externalizada
- ✅ **Health checks**: Actuator para monitoring

---

## 🤖 Agente IA Conversacional

### Descripción

Sistema de asistente inteligente integrado mediante **Spring AI + Groq API** que permite a los usuarios consultar y analizar sus datos financieros mediante lenguaje natural.

**LLM Utilizado**: **Llama 3.3 70B Versatile** (Meta) vía Groq - inferencia ultra-rápida y gratuita.

### Características

#### 🎯 Capacidades del Agente

- **Consultas Financieras**: Responde preguntas sobre saldos, gastos, ingresos y deuda
- **Análisis de Datos**: Genera reportes y análisis de patrones de gastos
- **Function Calling**: Accede a datos actualizados en tiempo real llamando a servicios del backend
- **Streaming (SSE)**: Respuestas token por token para mejor UX
- **Multi-tenant Seguro**: Todas las herramientas validan permisos del workspace
- **Contexto Argentino**: Comprende terminología y modelos financieros locales

#### 🔐 Seguridad y Estabilidad

- **Rate Limiting**: 60 mensajes/minuto por usuario (burst capacity: 10)
- **Autenticación**: Requiere JWT válido en todas las requests
- **Audit Log**: Registro completo de interacciones para compliance
- **Validación Multi-tenant**: Cada herramienta verifica acceso al workspace
- **Retry automático 429**: Backoff exponencial (2s → 4s, máx 2 reintentos) en caso de rate limit de Groq
- **Validación de entrada**: Máximo 400 caracteres por mensaje

#### 📊 Audit Log

Todas las interacciones se registran en la tabla `agente_audit_log`:
- Usuario, workspace, mensaje, respuesta
- Funciones llamadas, tokens consumidos
- Timestamp, éxito/error

### Endpoints

#### POST /api/agente/chat
Envía un mensaje al agente y recibe respuesta completa.

**Request:**
```json
{
  "message": "¿Cuánto gasté este mes en supermercado?",
  "workspaceId": "uuid-del-workspace",
  "conversationHistory": [
    {
      "role": "user",
      "content": "mensaje anterior"
    },
    {
      "role": "assistant",
      "content": "respuesta anterior"
    }
  ]
}
```

> **`message`**: Máximo **400 caracteres**. El campo acepta lenguaje natural en español.

**Response:**
```json
{
  "response": "📊 Este mes gastaste $4,500 en Supermercado...",
  "functionsCalled": ["buscarTransacciones"],
  "tokensUsed": 450
}
```

**Códigos de Respuesta:**
- `200 OK`: Respuesta exitosa
- `400 BAD_REQUEST`: Validación fallida
- `401 UNAUTHORIZED`: Token inválido o expirado
- `403 FORBIDDEN`: Sin acceso al workspace
- `429 TOO_MANY_REQUESTS`: Rate limit excedido

#### GET /api/agente/chat/stream
Streaming SSE para respuestas token por token.

**Parámetros:**
- `message` (query): Mensaje del usuario
- `workspaceId` (query): UUID del workspace

**Response:** `text/event-stream`

#### GET /api/agente/rate-limit/status
Consulta tokens restantes para el usuario actual.

**Response:**
```json
{
  "tokensRemaining": 45
}
```

### Herramientas (Tools) Disponibles

El agente puede llamar automáticamente a estas funciones cuando necesita datos:

| Función | Descripción | Cuándo se usa | Límite |
|---------|-------------|---------------|--------|
| `obtenerDashboardFinanciero` | Estado financiero completo | "¿cuál es mi saldo?", "¿cuánto debo?" | — |
| `buscarTransacciones` | Buscar transacciones con filtros | "gastos de enero", "pagos a Juan Pérez", "supermercado 2025" | 20 items |
| `listarTarjetasCredito` | Lista de tarjetas de crédito | "¿qué tarjetas tengo?" | — |
| `listarResumenesTarjetas` | Resúmenes mensuales por espacio de trabajo | "¿cuánto debo en tarjetas este mes?" | — |
| `listarResumenesPorTarjeta` | Historial de resúmenes de una tarjeta | "¿cómo evolucionaron los gastos de mi Visa?" | 6 items |
| `listarCuotasPorTarjeta` | Cuotas del período de una tarjeta | "¿qué cuotas vencen en mi Mastercard?" | 24 items |
| `buscarTodasComprasCredito` | Historial completo de compras en cuotas | "¿qué compré en cuotas?" | 20 items |
| `listarComprasCreditoPendientes` | Compras con cuotas pendientes | "¿qué cuotas me faltan pagar?" | 20 items |
| `listarCuentasBancarias` | Cuentas con saldos actuales | "saldo de cuenta bancaria" | — |
| `listarMotivosTransacciones` | Categorías de gastos/ingresos | "¿qué categorías hay?" | — |
| `listarContactosTransaccion` | Contactos de transferencia | "¿a quién le transferí dinero?" | — |

### Configuración

#### Variables de Entorno Requeridas

```bash
# Groq API (LLM - Opcional)
AGENTE_IA_ENABLED=true
GROQ_API_KEY=gsk_tu_groq_api_key

# Rate Limiting (opcional, valores por defecto)
AGENTE_RATE_LIMIT_REQUESTS_PER_MINUTE=60
AGENTE_RATE_LIMIT_BURST_CAPACITY=10
```

#### application.properties

```properties
# Habilitar Agente IA (opcional)
agente.ia.enabled=${AGENTE_IA_ENABLED:false}

# Groq API (Compatible con OpenAI)
spring.ai.openai.api-key=${GROQ_API_KEY:dummy-key}
spring.ai.openai.base-url=https://api.groq.com/openai
spring.ai.openai.chat.options.model=llama-3.3-70b-versatile
spring.ai.openai.chat.options.temperature=0.3
spring.ai.openai.chat.options.max-tokens=2048
spring.ai.openai.chat.options.top-p=0.95

# Rate Limiting
agente.rate-limit.requests-per-minute=60
agente.rate-limit.burst-capacity=10
```

**Características de Groq:**
- ✅ **Gratuito**: Sin tarjeta de crédito, sin facturación
- ✅ **Ultra-rápido**: LPUs (Language Processing Units) 
- ✅ **Compatible**: API 100% compatible con OpenAI
- ✅ **Sin Setup**: No requiere configuración de cloud ni service accounts
- ✅ **Modelo**: Llama 3.3 70B Versatile (Meta)

### Arquitectura Técnica

```
┌─────────────────────────────────────────────┐
│          AgenteIAController                 │
│     (REST API + Rate Limiting)              │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         AgenteIAService                     │
│  (Orquestación LLM + Audit Log)            │
└──────────────────┬──────────────────────────┘
                   │
                   ├──────────────┬────────────────┐
                   │              │                │
           ┌───────▼─────┐  ┌────▼──────┐  ┌─────▼──────┐
           │ ChatClient  │  │   Audit   │  │  Security  │
           │  (Spring AI)│  │ Repository│  │   Service  │
           └───────┬─────┘  └───────────┘  └────────────┘
                   │
           ┌───────▼─────────────────────────┐
           │    Groq API (HTTPS REST)        │
           │ Llama 3.3 70B Versatile (Meta)  │
           └───────┬─────────────────────────┘
                   │
           ┌───────▼─────────────────────┐
           │   Function Calling          │
           │   (AgenteToolsService)      │
           └─────────────────────────────┘
                   │
         ┌─────────┼─────────┬──────────┐
         │         │         │          │
    ┌────▼───┐ ┌──▼────┐ ┌──▼─────┐ ┌──▼────────┐
    │Dashboard│ │Transac│ │Compras│ │CuentaBanc │
    │ Service │ │Service│ │Service│ │  Service  │
    └─────────┘ └───────┘ └────────┘ └───────────┘
```

### Flujo de Ejecución

1. **Request del Usuario**: Frontend envía mensaje + workspaceId
2. **Rate Limiting**: Valida que el usuario no exceda 60 req/min
3. **Validación de Seguridad**: SecurityService verifica acceso al workspace
4. **Construcción del Prompt**: Sistema prompt + historial + mensaje actual
5. **Llamada al LLM**: ChatClient envía prompt a Groq con funciones habilitadas
6. **Function Calling**: Si el LLM necesita datos, llama a AgenteToolsService
7. **Validación de Tools**: Cada tool valida permisos multi-tenant nuevamente
8. **Generación de Respuesta**: LLM procesa resultados y genera respuesta natural
9. **Audit Log**: Se registra toda la interacción (mensaje, respuesta, funciones, tokens)
10. **Response al Usuario**: Respuesta formateada con emojis y contexto argentino

### System Prompt

El agente opera bajo las siguientes instrucciones:

- **Identidad**: FinanceAgent AI, asistente financiero experto
- **Dominio**: Solo finanzas del workspace actual (no temas generales)
- **Limitaciones**: Solo lectura (no crea transacciones ni transfiere dinero)
- **Estilo**: Formal pero cercano, con emojis, formatos de moneda
- **Contexto**: Comprende modelo argentino de tarjetas de crédito
- **Ética**: Nunca inventa datos, siempre usa funciones para información real

### Ejemplo de Interacción

**Usuario:** *"¿Cuánto gasté este mes y en qué?"*

**Agente (internamente):**
1. Detecta que necesita datos actualizados
2. Llama a `obtenerDashboardFinanciero(workspaceId)`
3. Llama a `buscarTransacciones(workspaceId, mes=2, anio=2026)`
4. Analiza los resultados

**Agente (responde):**
```
📊 En febrero gastaste $12,400 en total. Aquí está el desglose:

💳 Principales categorías:
• Supermercado: $4,500 (36%)
• Transporte: $2,300 (19%)
• Alquiler: $3,500 (28%)
• Otros: $2,100 (17%)

⚠️ Nota: Tus gastos aumentaron 15% respecto a enero. 
Te recomendaría revisar los gastos en Transporte para el próximo mes.
```

### Testing

#### Test Manual con cURL

```bash
# Obtener token JWT
TOKEN="tu-jwt-token"

# Enviar mensaje
curl -X POST http://localhost:8080/api/agente/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "¿Cuánto debo en total?",
    "workspaceId": "uuid-del-workspace"
  }'

# Streaming
curl -N http://localhost:8080/api/agente/chat/stream?message=Hola&workspaceId=uuid \
  -H "Authorization: Bearer $TOKEN"

# Verificar rate limit
curl http://localhost:8080/api/agente/rate-limit/status \
  -H "Authorization: Bearer $TOKEN"
```

#### Verificar Logs de Auditoría

```sql
SELECT user_id, user_message, functions_called, tokens_used, success
FROM agente_audit_log
ORDER BY timestamp DESC
LIMIT 10;
```

### Troubleshooting

#### Error: "Google Cloud credentials not found"
```bash
# Configurar credenciales
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/service-account.json"
export GOOGLE_CLOUD_PROJECT_ID="tu-proyecto-id"
```

#### Error: Rate limit excedido (429)
- Espera 1 minuto para que se recarguen los tokens
- O aumenta el límite en `application.properties`:
  ```properties
  agente.rate-limit.requests-per-minute=120
  ```

#### El agente no llama a funciones
- Verifica que las funciones estén registradas como beans
- Revisa logs: `grep "Agente llamando tool" logs/spring-boot.log`
- Asegúrate de que el system prompt esté configurado

#### Respuestas lentas
- Gemini Flash es rápido (~2-3 segundos)
- Si es más lento, verifica latencia de red con Google Cloud
- Considera usar streaming para mejor percepción de velocidad

### Roadmap Futuro

**Fase 2 - Operaciones de Escritura** (Próxima versión):
- Crear transacciones con confirmación explícita
- Registrar compras a crédito
- Transferir entre cuentas
- Dry-run mode para simular operaciones

**Fase 3 - Análisis Predictivo**:
- Proyecciones de saldo futuro
- Detección de anomalías en gastos
- Recomendaciones personalizadas de ahorro

**Fase 4 - Multimodal**:
- Foto de ticket → extracción automática
- Gráficos generados por el agente

---

## 📚 Recursos Adicionales

### Documentación Técnica

- [Diagrama de Clases UML](../docs/DiagramaDeClasesUML.puml)
- [Historias de Usuario](../docs/HistoriasDeUsuario.md)
- [Problemas y Soluciones](../docs/ProblemasSoluciones.md)
- [Guía Docker](../docs/GuiaDocker.md)
- [Despliegue en Producción](../docs/DespliegueProduccion.md)
- [Configuración de Secrets para CI/CD](../docs/ConfiguracionSecretsCD.md)

### Enlaces Útiles

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [MapStruct Reference](https://mapstruct.org/documentation/stable/reference/html/)

---

## 📧 Contacto

Para consultas o soporte relacionado con el backend:
- **Repositorio**: [GitHub](https://github.com/niclesanti/ProyectoGastos)
- **Issues**: [GitHub Issues](https://github.com/niclesanti/ProyectoGastos/issues)

---

**Versión del documento**: 1.1.0  
**Última actualización**: Febrero 2026  
**Mantenido por**: Nicle Santiago
