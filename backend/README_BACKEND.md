# Backend - Sistema de Gestión de Gastos Personales

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Problema que Resuelve](#-problema-que-resuelve)
- [Funcionalidades Principales](#-funcionalidades-principales)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Modelo de Datos](#-modelo-de-datos)
- [Configuración y Requisitos](#%EF%B8%8F-configuración-y-requisitos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [API Endpoints](#-api-endpoints)
- [Seguridad y Autenticación](#-seguridad-y-autenticación)
- [Migraciones de Base de Datos](#-migraciones-de-base-de-datos)
- [Testing](#-testing)
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
- Sistema de permisos (administrador/participante)
- Compartir espacios entre múltiples usuarios
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
- Optimización mediante tabla agregada para evitar recálculos

### 7. Automatización
- Cierre automático diario de resúmenes de tarjetas (scheduler)
- Actualización automática de saldos
- Cálculo incremental de estadísticas

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

### Documentación
- **SpringDoc OpenAPI 2.8.8**: Generación automática de documentación API
- **Swagger UI**: Interfaz interactiva para testing de endpoints

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
- **Multi-stage Build**: Optimización de imágenes Docker

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
│  - Mappers (MapStruct)                      │
│  - Validadores (Bean Validation)            │
│  - DTOs (Data Transfer Objects)             │
│  - Schedulers (Tareas Programadas)          │
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
│   │   │   │   ├── ResumenRepository.java
│   │   │   │   ├── TarjetaRepository.java
│   │   │   │   ├── TransaccionRepository.java
│   │   │   │   └── UsuarioRepository.java
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── *DTORequest.java       # DTOs para peticiones
│   │   │   │   ├── *DTOResponse.java      # DTOs para respuestas
│   │   │   │   └── *BusquedaDTO.java      # DTOs para búsquedas
│   │   │   ├── exception/                 # Manejo de excepciones
│   │   │   │   ├── ControllerAdvisor.java
│   │   │   │   └── ExceptionInfo.java
│   │   │   ├── mapper/                    # MapStruct Mappers
│   │   │   │   ├── config/
│   │   │   │   │   └── MapstructConfig.java
│   │   │   │   └── *Mapper.java
│   │   │   ├── model/                     # Entidades JPA
│   │   │   │   ├── CompraCredito.java
│   │   │   │   ├── ContactoTransferencia.java
│   │   │   │   ├── CuentaBancaria.java
│   │   │   │   ├── CuotaCredito.java
│   │   │   │   ├── CustomOAuth2User.java
│   │   │   │   ├── EspacioTrabajo.java
│   │   │   │   ├── EstadoResumen.java     # Enum
│   │   │   │   ├── GastosIngresosMensuales.java
│   │   │   │   ├── MotivoTransaccion.java
│   │   │   │   ├── Notificacion.java
│   │   │   │   ├── Presupuesto.java
│   │   │   │   ├── ProveedorAutenticacion.java # Enum
│   │   │   │   ├── Resumen.java
│   │   │   │   ├── Tarjeta.java
│   │   │   │   ├── TipoTransaccion.java   # Enum
│   │   │   │   ├── Transaccion.java
│   │   │   │   └── Usuario.java
│   │   │   ├── scheduler/                 # Tareas programadas
│   │   │   │   └── ResumenScheduler.java
│   │   │   ├── service/                   # Capa de servicios
│   │   │   │   ├── *Service.java          # Interfaces
│   │   │   │   └── *ServiceImpl.java      # Implementaciones
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
│   │       │   └── V6__create_gastos_ingresos_mensuales.sql
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
  - Recibe Notificaciones

#### EspacioTrabajo
Contexto colaborativo donde se gestionan las finanzas de un grupo.
- **Atributos**: id, nombre, saldo, usuarioAdmin, usuariosParticipantes
- **Métodos**: actualizarSaldoNuevaTransaccion(), actualizarSaldoEliminarTransaccion()
- **Relaciones**: 
  - Contiene CuentasBancarias, Transacciones, Motivos, Contactos, Presupuestos, Tarjetas, ComprasCredito, GastosIngresosMensuales

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
- **Atributos**: id, anio, mes, gastos, ingresos, espacioTrabajo
- **Métodos**: actualizarGastos(), actualizarIngresos(), eliminarGastos(), eliminarIngresos()

### Diagrama de Clases

El diagrama UML completo se encuentra en `/docs/DiagramaDeClasesUML.puml` y puede visualizarse con PlantUML.

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

# Windows (CMD)
set GOOGLE_CLIENT_ID=tu_client_id
set GOOGLE_CLIENT_SECRET=tu_client_secret
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/campito_db
set SPRING_DATASOURCE_USERNAME=campito_user
set SPRING_DATASOURCE_PASSWORD=campito_pass

# Windows (PowerShell)
$env:GOOGLE_CLIENT_ID="tu_client_id"
$env:GOOGLE_CLIENT_SECRET="tu_client_secret"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/campito_db"
$env:SPRING_DATASOURCE_USERNAME="campito_user"
$env:SPRING_DATASOURCE_PASSWORD="campito_pass"
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
| POST | `/api/espacioTrabajo/registrar` | Crear nuevo espacio de trabajo | ✅ |
| PUT | `/api/espacioTrabajo/compartir/{email}/{idEspacioTrabajo}/{idUsuarioAdmin}` | Compartir espacio con otro usuario | ✅ |
| GET | `/api/espacioTrabajo/listar/{idUsuario}` | Listar espacios del usuario | ✅ |
| GET | `/api/espacioTrabajo/miembros/{idEspacioTrabajo}` | Obtener miembros de un espacio | ✅ |

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

### Documentación API

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

---

## 🔒 Seguridad y Autenticación

### Estrategia de Seguridad

El sistema implementa un modelo de seguridad basado en:

1. **OAuth2**: Autenticación delegada a proveedores externos
2. **Spring Security**: Gestión de sesiones y autorización
3. **CORS**: Configuración para permitir peticiones del frontend
4. **HTTPS**: Recomendado en producción

### Flujo de Autenticación

```
1. Usuario → Botón "Login con Google"
2. Frontend → Redirige a: backend/oauth2/authorization/google
3. Backend → Redirige a: Google OAuth2
4. Usuario → Autoriza en Google
5. Google → Callback a: backend/login/oauth2/code/google
6. Backend → Procesa usuario (crea/actualiza en BD)
7. Backend → Establece sesión
8. Backend → Redirige a: frontend/
9. Frontend → Usuario autenticado
```

### Validaciones Personalizadas

El sistema incluye validadores personalizados para:

- **ValidNombre**: Nombres no vacíos y con formato válido
- **ValidMonto**: Montos positivos y con máximo 2 decimales
- **ValidDescripcion**: Descripciones con longitud controlada
- **ValidSaldoActual**: Saldos iniciales válidos

---

## 🗃 Migraciones de Base de Datos

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

## 🐳 Despliegue con Docker

### Dockerfile Multi-Stage

El proyecto utiliza un Dockerfile optimizado con dos etapas:

#### Etapa 1: Builder
- Imagen base: `maven:3.9-eclipse-temurin-21`
- Maven Wrapper para independencia de versión
- Descarga de dependencias (cacheadas)
- Compilación del proyecto
- Generación del JAR

#### Etapa 2: Runner
- Imagen base: `eclipse-temurin:21-jre-alpine` (ligera)
- Solo copia el JAR compilado
- Expone puerto 8080
- Ejecuta la aplicación

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
- ✅ **Maven Wrapper**: Independencia de versión de Maven
- ✅ **Variables de entorno**: Configuración externalizada
- ✅ **Health checks**: Actuator para monitoring

---

## 📚 Recursos Adicionales

### Documentación Técnica

- [Diagrama de Clases UML](../docs/DiagramaDeClasesUML.puml)
- [Historias de Usuario](../docs/HistoriasDeUsuario.md)
- [Problemas y Soluciones](../docs/ProblemasSoluciones.md)
- [Guía Docker](../docs/GuiaDocker.md)

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

**Versión del documento**: 1.0.0  
**Última actualización**: Enero 2026  
**Mantenido por**: Nicle Santiago
