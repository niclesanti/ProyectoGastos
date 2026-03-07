<div align="center">

# 💰💵 Sistema de Gestión de Finanzas Personales 💵💰

### Plataforma Full-Stack moderna para gestión financiera familiar y personal

[![Java](https://img.shields.io/badge/☕_Java-21-ED8B00?style=flat)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.3-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.3.1-61DAFB?style=flat&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.3.3-3178C6?style=flat&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![Prometheus](https://img.shields.io/badge/Metrics-Prometheus-E6522C?style=flat&logo=prometheus&logoColor=white)](https://prometheus.io/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=flat&logo=github-actions&logoColor=white)](https://github.com/features/actions)

[Características](#-características-principales) • [Tecnologías](#-stack-tecnológico) • [Arquitectura](#-arquitectura) • [Instalación](#-inicio-rápido) • [Documentación](#-documentación) • [Contribuir](CONTRIBUTING.md)

</div>

---

## 📖 Descripción

Sistema Full-Stack profesional para la gestión integral de finanzas personales y familiares. Ofrece control completo sobre el registro de transacciones, cuentas bancarias, tarjetas de crédito, compras en cuotas y análisis financiero mediante dashboards interactivos con excelente precisión en los datos que permite llevar todos tus números al día. Desarrollado con tecnologías modernas y siguiendo las mejores prácticas de la industria.

### ✨ ¿Por qué este proyecto?

- 🎯 **Solución Real**: Creado para resolver necesidades financieras reales de gestión familiar
- 🏗️ **Arquitectura Profesional**: Separación frontend/backend con arquitectura en capas
- 🔐 **Seguridad OAuth2**: Autenticación moderna con proveedores externos
- 📊 **Dashboard Interactivo**: Visualizaciones y análisis financiero en tiempo real
- 👥 **Multi-Tenant**: Espacios de trabajo colaborativos para gestión grupal
- 🔍 **Observabilidad y Telemetría**: Métricas de negocio y salud del sistema con Prometheus y Grafana para monitoreo proactivo
- 🚀 **Producción Ready**: Dockerizado y desplegable en cloud con un comando
- ⚙️ **CI/CD Automatizado**: Integración y despliegue continuo con GitHub Actions

---

## 🌟 Características Principales

### 💳 Gestión Financiera Completa
- **Transacciones**: Registro detallado de ingresos y gastos con categorización
- **Cuentas Bancarias**: Gestión de múltiples cuentas con actualización de saldos
- **Tarjetas de Crédito**: Control de tarjetas con ciclos de facturación configurables
- **Compras en Cuotas**: Seguimiento automático de cuotas y generación de resúmenes mensuales
- **Descuentos**: Registro de descuentos bancarios/comerciales organizados por día de la semana (Kanban 7 columnas)
- **Precisión Decimal**: Cálculos financieros con máxima exactitud

### 📊 Dashboard y Analytics
- **KPIs en Tiempo Real**: Balance total, gastos mensuales, deuda pendiente
- **Visualizaciones Interactivas**: 
  - Flujo de caja mensual (ingresos vs gastos)
  - Distribución de gastos por categoría
  - Tendencias y evolución temporal

### 👥 Colaboración y Multi-Tenant
- **Espacios de Trabajo**: Crear y compartir espacios entre usuarios
- **Roles y Permisos**: Sistema de administrador/participante
- **Gestión Familiar**: Ideal para finanzas compartidas

### ⚡ Automatización
- **Cierre Automático**: Resúmenes de tarjetas procesados automáticamente
- **Cálculo Incremental**: Actualización eficiente de estadísticas
- **Tareas Programadas**: Schedulers para operaciones periódicas

### 🔔 Notificaciones en Tiempo Real
- **SSE (Server-Sent Events)**: Push notifications
- **Arquitectura Asíncrona**: Event-driven con procesamiento no bloqueante
- **Alertas Inteligentes**: Cierres de tarjeta, vencimientos, invitaciones
- **Persistencia**: Historial completo de notificaciones con estado leído/no leído

### 📈 Observabilidad y Métricas
- **Instrumentación Completa**: Métricas de lógica de negocio con Micrometer
- **Monitoreo en Tiempo Real**: Formato Prometheus para visualización en Grafana
- **Métricas Clave**: Transacciones, compras a crédito, resúmenes, conexiones SSE
- **Production-Ready**: Configurado con Spring Boot Actuator para diagnóstico y análisis

---

## 🛠 Stack Tecnológico

<table>
<tr>
<td width="50%" valign="top">

### Backend

**Core**
- ☕ Java 21 (LTS)
- 🍃 Spring Boot 3.5.3
- 🗃️ Spring Data JPA + Hibernate
- 🐘 PostgreSQL 14

**Seguridad y Autenticación**
- 🔐 Spring Security + OAuth2
- 🔑 Google OAuth 2.0
- 🎫 JWT (JSON Web Tokens)

**Notificaciones en Tiempo Real**
- 📡 SSE (Server-Sent Events)
- 📢 Event-Driven Architecture
- ⚡ Procesamiento Asíncrono (@Async)

**Herramientas**
- 🗺️ MapStruct 1.5.5 (Mapeo DTO/Entidad)
- 🔨 Lombok (Reducción boilerplate)
- 📚 SpringDoc OpenAPI (Swagger)
- 🔄 Flyway (Migraciones BD)

**Observabilidad y Telemetría**
- 📊 Spring Boot Actuator
- 📈 Micrometer (Métricas)
- 🔍 Prometheus (Formato export)
- 📉 Grafana (Visualización)

**Testing & CI/CD**
- ✅ JUnit 5
- 🧪 Mockito
- 💾 H2 (BD en memoria)
- 🔄 GitHub Actions (CI/CD)

</td>
<td width="50%" valign="top">

### Frontend

**Core**
- ⚛️ React 18.3.1
- 📘 TypeScript 5.3.3
- ⚡ Vite 5.0.11

**UI/UX**
- 🎨 Tailwind CSS 3.4.0
- 🧩 shadcn/ui (Radix UI)
- 🎭 32+ componentes accesibles
- 📱 PWA (Progressive Web App)

**Estado y Datos**
- 🐻 Zustand 4.4.7
- 🔄 React Query
- 📝 React Hook Form + Zod

**Visualización**
- 📊 Recharts 2.15.4
- 📈 Gráficos interactivos

**Notificaciones**
- 🔔 EventSource API (nativa)
- 📬 SSE client-side
- 🎯 Sistema reactivo en tiempo real

</td>
</tr>
</table>

### Infraestructura & DevOps

- 🐳 **Docker & Docker Compose**: Contenerización completa
- 🛠️ **Maven**: Gestión de dependencias backend
- 📦 **npm**: Gestión de dependencias frontend
- 🔄 **GitHub Actions**: Workflows de CI/CD automatizados

---

## 🏗 Arquitectura

### Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                       FRONTEND (SPA)                        │
│  React + TypeScript + Vite + Tailwind CSS + shadcn/ui      │
│  - Componentes reutilizables y accesibles                   │
│  - Estado global con Zustand + caché inteligente           │
│  - Responsive design (móvil-first)                          │
└────────────────────────┬────────────────────────────────────┘
                         │ REST API (JSON)
                         │ OAuth2 Authentication
┌────────────────────────▼────────────────────────────────────┐
│                    BACKEND (Spring Boot)                    │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          Controllers (REST Endpoints)                │  │
│  └──────────────────────┬───────────────────────────────┘  │
│  ┌──────────────────────▼───────────────────────────────┐  │
│  │          Services (Lógica de Negocio)               │  │
│  └──────────────────────┬───────────────────────────────┘  │
│  ┌──────────────────────▼───────────────────────────────┐  │
│  │          Repositories (Acceso a Datos)              │  │
│  └──────────────────────┬───────────────────────────────┘  │
└─────────────────────────┼───────────────────────────────────┘
                          │ JPA/Hibernate
┌─────────────────────────▼───────────────────────────────────┐
│                  PostgreSQL Database                        │
│  - Migraciones con Flyway                                   │
│  - Índices optimizados                                      │
│  - Auditoría completa                                       │
└─────────────────────────────────────────────────────────────┘
```

### Estructura del Proyecto

```
ProyectoGastos/
├── backend/                    # API REST Spring Boot
│   ├── src/main/java/
│   │   └── com/campito/backend/
│   │       ├── config/        # Configuración (Security, CORS)
│   │       ├── controller/    # REST Controllers
│   │       ├── service/       # Lógica de negocio
│   │       ├── dao/           # Repositories JPA
│   │       ├── model/         # Entidades JPA
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── mapper/        # MapStruct mappers
│   │       ├── scheduler/     # Tareas programadas
│   │       ├── security/      # JWT y OAuth2
│   │       ├── validation/    # Validadores personalizados
│   │       └── exception/     # Manejo de errores
│   ├── src/main/resources/
│   │   └── db/migration/      # Scripts Flyway
│   └── pom.xml
│
├── frontend/                   # SPA React + TypeScript
│   ├── src/
│   │   ├── components/        # Componentes reutilizables
│   │   │   └── ui/           # shadcn/ui components
│   │   ├── pages/            # Páginas principales
│   │   ├── features/         # Lógica por módulo
│   │   ├── services/         # Servicios API
│   │   ├── store/            # Estado Zustand
│   │   ├── hooks/            # Custom hooks
│   │   └── lib/              # Utilidades
│   ├── public/               # Assets estáticos + PWA
│   └── package.json
│
├── docs/                       # Documentación
│   ├── DiagramaDeClasesUML.puml
│   ├── HistoriasDeUsuario.md
│   ├── GuiaDocker.md
|   ├── DespliegueProduccion.md
|   └── ProblemasSoluciones.md
│
├── docker-compose.yml          # Orquestación Docker
├── docker-compose.override.yml # Configuración desarrollo
├── docker-compose.prod.yml     # Configuración producción
└── .env                        # Variables de entorno
```

---

## 🚀 Inicio Rápido

### Prerrequisitos

- Docker y Docker Compose instalados
- Git
- (Opcional) Java 21 y Node.js 18+ para desarrollo local sin Docker

### Instalación

```bash
# 1. Clonar el repositorio
git clone https://github.com/niclesanti/ProyectoCampo.git
cd ProyectoCampo

# 2. Configurar variables de entorno
cp .env.example .env
# Editar .env con tus credenciales de Google OAuth2

# 3. Levantar todos los servicios
docker-compose up -d --build

# 4. Acceder a la aplicación
# Frontend: http://localhost:3100
# Backend: http://localhost:8080
# API Docs: http://localhost:8080/swagger-ui/index.html
# pgAdmin: http://localhost:5050
```

### Configuración de Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# Configuración de Base de Datos PostgreSQL
DB_NAME=campito_db
DB_USER=postgres
DB_PASSWORD=postgres123

# Configuración de pgAdmin
PGADMIN_EMAIL=admin@campito.com
PGADMIN_PASSWORD=admin123

# Spring Boot
SPRING_PROFILES_ACTIVE=dev

# Google OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

Para obtener credenciales de Google OAuth2, sigue la [guía oficial](https://support.google.com/cloud/answer/6158849).

---

## 📄 Documentación

### Documentación Técnica Completa

- 📘 **[Backend README](backend/README.md)**: Documentación completa del API REST
  - Arquitectura y patrones
  - Endpoints y ejemplos
  - Modelo de datos
  - Seguridad y autenticación
  - Migraciones de base de datos

- 📗 **[Frontend README](frontend/README_FRONTEND.md)**: Documentación completa del frontend
  - Estructura de componentes
  - Sistema de diseño (Tailwind + shadcn/ui)
  - Gestión de estado con Zustand
  - PWA y responsive design
  - Optimización y performance

### Otros Documentos

- 📐 [Diagrama de Clases UML](docs/DiagramaDeClasesUML.puml)
- 📋 [Historias de Usuario](docs/HistoriasDeUsuario.md)
- 🐛 [Problemas y Soluciones](docs/ProblemasSoluciones.md)
- 🐳 [Guía Docker](docs/GuiaDocker.md)

---

## 🧪 Testing

### Backend
```bash
cd backend
./mvnw test
```

---

## 🌐 Despliegue en Producción

El proyecto incluye configuración para despliegue en Google Cloud Run:

```bash
# Usar configuración de producción
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up --build
```

**Producción actual**: No hay aún

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Por favor, lee la [guía de contribución](CONTRIBUTING.md) antes de enviar un Pull Request.

### Proceso de Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 👨‍💻 Autor

**Santiago Nicle**

- 📧 Email: niclesantiago@gmail.com
- 💼 LinkedIn: [santiago-nicle](https://www.linkedin.com/in/santiago-nicle/)
- 🐙 GitHub: [@niclesanti](https://github.com/niclesanti)

---

## 🙏 Agradecimientos

Este proyecto fue desarrollado aplicando conocimientos adquiridos durante la formación académica en la carrera de Ingeniería en Sistemas de Información.

---

<div align="center">

**⭐ Si este proyecto te resulta útil, considera darle una estrella en GitHub ⭐**

</div>


