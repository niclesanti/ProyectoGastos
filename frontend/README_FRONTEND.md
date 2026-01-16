# Frontend - Sistema de Gestión de Gastos Personales

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Características Principales](#-características-principales)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura y Patrones](#-arquitectura-y-patrones)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Sistema de Diseño](#-sistema-de-diseño)
- [Componentes UI](#-componentes-ui)
- [Páginas y Features](#-páginas-y-features)
- [Gestión de Estado](#-gestión-de-estado)
- [Servicios y API](#-servicios-y-api)
- [Enrutamiento](#-enrutamiento)
- [Configuración y Requisitos](#%EF%B8%8F-configuración-y-requisitos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Progressive Web App (PWA)](#-progressive-web-app-pwa)
- [Responsive Design](#-responsive-design)
- [Optimización y Performance](#-optimización-y-performance)
- [Despliegue con Docker](#-despliegue-con-docker)
- [Mejores Prácticas](#-mejores-prácticas)

---

## 🎯 Descripción General

Aplicación web moderna y responsiva desarrollada con React 18 y TypeScript que proporciona una interfaz de usuario intuitiva y profesional para la gestión de finanzas personales. La aplicación ofrece una experiencia fluida con soporte completo para dispositivos móviles y de escritorio, diseño oscuro elegante, componentes reutilizables y optimización de rendimiento.

### Características Destacadas

- ✅ **Interfaz Moderna**: Diseño oscuro profesional con componentes shadcn/ui
- ✅ **Totalmente Responsiva**: Optimizada para móviles, tablets y escritorio
- ✅ **PWA**: Instalable como aplicación nativa en cualquier plataforma
- ✅ **TypeScript**: Tipado estático para mayor seguridad y mantenibilidad
- ✅ **Performance**: Code splitting, lazy loading y optimizaciones avanzadas
- ✅ **Gestión de Estado**: Zustand con caché inteligente
- ✅ **Componentes Accesibles**: Basados en Radix UI con ARIA completo
- ✅ **Visualización de Datos**: Gráficos interactivos con Recharts

---

## 🌟 Características Principales

### Dashboard Interactivo
- **KPIs en Tiempo Real**: Balance total, gastos mensuales, resumen de tarjetas, deuda pendiente
- **Gráficos Dinámicos**: 
  - Flujo de caja mensual (ingresos vs gastos)
  - Distribución de gastos por categoría (donut chart)
- **Actividad Reciente**: Lista de transacciones más recientes
- **Cuentas Bancarias**: Resumen de saldos actuales
- **Compras Pendientes**: Seguimiento de compras con cuotas pendientes de pago.

### Gestión de Transacciones
- Registro rápido con modal intuitivo
- Filtros avanzados por fecha, tipo, motivo, contacto y cuenta
- Búsqueda en tiempo real
- Visualización detallada de cada transacción
- Edición y eliminación con confirmación

### Tarjetas de Crédito
- Vista de tarjetas tipo "card" con información de cierre/vencimiento
- Gestión de compras en cuotas
- Pago de resúmenes mensuales
- Seguimiento de cuotas pendientes
- Cálculo automático de resúmenes

### Configuración
- Gestión de espacios de trabajo
- Invitación de miembros

### Experiencia de Usuario
- Navegación fluida sin recargas (SPA)
- Notificaciones toast elegantes
- Modales y diálogos accesibles
- Drag & drop para reorganización
- Modo oscuro nativo
- FAB (Floating Action Button) en móviles
- Animaciones suaves y transiciones

---

## 🛠 Stack Tecnológico

### Core Framework y Lenguaje
- **React 18.3.1**: Librería principal con Concurrent Features
- **TypeScript 5.3.3**: Tipado estático y mejoras de DX
- **Vite 5.0.11**: Build tool ultra-rápido con HMR

### UI y Estilos
- **Tailwind CSS 3.4.0**: Framework CSS utility-first
- **shadcn/ui**: Sistema de componentes basado en Radix UI
- **Radix UI**: Primitivos accesibles y sin estilos
  - Dialog, Dropdown, Popover, Select, Switch, Tabs, Tooltip, etc.
- **Lucide React 0.307.0**: Biblioteca de iconos SVG
- **Vaul 1.1.2**: Drawer component para móviles
- **class-variance-authority**: Variantes de componentes tipadas
- **tailwind-merge**: Merge inteligente de clases Tailwind
- **tailwindcss-animate**: Animaciones predefinidas

### Gestión de Estado y Datos
- **Zustand 4.4.7**: Estado global minimalista y performante
- **@tanstack/react-query 5.90.12**: Server state y caché
- **@tanstack/react-table 8.11.2**: Tablas de datos potentes

### Formularios y Validación
- **React Hook Form 7.69.0**: Manejo de formularios performante
- **Zod 4.2.1**: Validación de esquemas TypeScript-first
- **@hookform/resolvers 5.2.2**: Integración RHF + Zod

### Enrutamiento y Navegación
- **React Router DOM 6.21.0**: Routing declarativo con lazy loading

### Visualización de Datos
- **Recharts 2.15.4**: Gráficos y charts interactivos
  - Bar charts, Line charts, Pie charts, Donut charts

### Utilidades de Fecha
- **date-fns 3.6.0**: Manipulación y formato de fechas
- **react-day-picker 9.13.0**: Selector de fechas accesible

### HTTP y Comunicación
- **Axios 1.13.2**: Cliente HTTP con interceptors

### Interacción
- **@dnd-kit**: Drag and drop accesible
  - @dnd-kit/core, @dnd-kit/sortable, @dnd-kit/utilities
- **cmdk 1.1.1**: Command palette (⌘K)

### Notificaciones
- **Sonner 2.0.7**: Toast notifications elegantes

### Dev Tools
- **ESLint**: Linting con reglas TypeScript y React
- **PostCSS**: Procesamiento CSS
- **Autoprefixer**: Vendor prefixes automáticos
- **vite-plugin-svgr**: Importación de SVGs como componentes

---

## 🏗 Arquitectura y Patrones

### Arquitectura General

```
┌─────────────────────────────────────────────┐
│           PRESENTACIÓN (UI)                 │
│  - Páginas (Pages)                          │
│  - Componentes (Components)                 │
│  - Layouts                                  │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│        LÓGICA DE NEGOCIO (Features)         │
│  - Dashboard components                     │
│  - Workspace management                     │
│  - Selectors                                │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         GESTIÓN DE ESTADO (State)           │
│  - Zustand Store (app-store.ts)             │
│  - Caché con Map y timestamps               │
│  - React Query para server state            │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│          SERVICIOS (Services)               │
│  - API Services (*.service.ts)              │
│  - API Client (Axios)                       │
│  - Transformación de datos                  │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│             BACKEND API                     │
│  - REST Endpoints                           │
│  - Autenticación OAuth2                     │
└─────────────────────────────────────────────┘
```

### Patrones Implementados

1. **Component Composition**: Componentes pequeños y reutilizables
2. **Custom Hooks**: Lógica reutilizable encapsulada
3. **Service Layer**: Separación de llamadas API
4. **Smart/Dumb Components**: Componentes contenedores vs presentacionales
5. **Feature-based Structure**: Organización por funcionalidad
6. **Render Props**: Para componentes flexibles
7. **Compound Components**: Para componentes complejos (Card, Dialog)
8. **State Colocation**: Estado cerca de donde se usa

---

## 📁 Estructura del Proyecto

```
frontend/
├── public/                          # Archivos estáticos
│   ├── manifest.json               # PWA manifest
│   ├── icons/                      # Iconos multi-resolución
│   │   ├── android/                # Android Chrome icons
│   │   ├── apple/                  # iOS Apple Touch icons
│   │   ├── favicon/                # Favicons desktop
│   │   └── windows/                # Windows tiles
│   └── logo.png                    # Logo principal
├── src/
│   ├── assets/                     # Recursos estáticos
│   │   └── [images, svgs]
│   ├── components/                 # Componentes compartidos
│   │   ├── AccountTransferModal.tsx
│   │   ├── CardPaymentModal.tsx
│   │   ├── CreditPurchaseDetailsModal.tsx
│   │   ├── CreditPurchaseModal.tsx
│   │   ├── DeleteConfirmDialog.tsx
│   │   ├── Header.tsx
│   │   ├── MobileActionsFAB.tsx     # FAB para móviles
│   │   ├── PaymentProviderLogo.tsx
│   │   ├── ProtectedRoute.tsx       # HOC de autenticación
│   │   ├── Sidebar.tsx
│   │   ├── TransactionDetailsModal.tsx
│   │   ├── TransactionModal.tsx
│   │   └── ui/                      # Componentes shadcn/ui
│   │       ├── alert-dialog.tsx
│   │       ├── avatar.tsx
│   │       ├── badge.tsx
│   │       ├── breadcrumb.tsx
│   │       ├── button.tsx
│   │       ├── calendar.tsx
│   │       ├── card.tsx
│   │       ├── chart.tsx
│   │       ├── checkbox.tsx
│   │       ├── command.tsx
│   │       ├── data-table.tsx
│   │       ├── dialog.tsx
│   │       ├── drawer.tsx           # Mobile drawer
│   │       ├── dropdown-menu.tsx
│   │       ├── form.tsx
│   │       ├── input.tsx
│   │       ├── label.tsx
│   │       ├── pagination.tsx
│   │       ├── popover.tsx
│   │       ├── radio-group.tsx
│   │       ├── scroll-area.tsx
│   │       ├── select.tsx
│   │       ├── separator.tsx
│   │       ├── sheet.tsx
│   │       ├── sidebar.tsx
│   │       ├── skeleton.tsx
│   │       ├── switch.tsx
│   │       ├── table.tsx
│   │       ├── tabs.tsx
│   │       ├── textarea.tsx
│   │       ├── tooltip.tsx
│   │       └── visually-hidden.tsx
│   ├── contexts/                   # React Contexts
│   │   └── AuthContext.tsx         # Contexto de autenticación
│   ├── features/                   # Lógica por módulo
│   │   ├── dashboard/              # Componentes del Dashboard
│   │   │   ├── BankAccounts.tsx
│   │   │   ├── DashboardStats.tsx
│   │   │   ├── MonthlyCashflow.tsx
│   │   │   ├── RecentTransactions.tsx
│   │   │   ├── SpendingByCategory.tsx
│   │   │   ├── UpcomingPayments.tsx
│   │   │   └── WorkspacePlaceholder.tsx
│   │   ├── selectors/              # Componentes selectores
│   │   └── workspaces/             # Gestión de espacios
│   ├── hooks/                      # Custom hooks
│   │   ├── index.ts
│   │   ├── use-mobile.tsx          # Detección de móvil
│   │   ├── useDashboardCache.ts    # Caché del dashboard
│   │   └── useDashboardStats.ts    # Hook para stats
│   ├── layouts/                    # Layouts de página
│   │   └── DashboardLayout.tsx     # Layout principal con Sidebar
│   ├── lib/                        # Utilidades
│   │   ├── api-client.ts           # Cliente Axios configurado
│   │   └── utils.ts                # Funciones helper (cn, etc.)
│   ├── pages/                      # Páginas/Vistas
│   │   ├── ConfiguracionPage.tsx   # Configuración y ajustes
│   │   ├── CreditosPage.tsx        # Tarjetas y créditos
│   │   ├── DashboardPage.tsx       # Dashboard principal
│   │   ├── LoginPage.tsx           # Página de login OAuth2
│   │   └── MovimientosPage.tsx     # Transacciones
│   ├── services/                   # Servicios de API
│   │   ├── api.ts                  # Tipos y configuración
│   │   ├── authService.ts          # Autenticación
│   │   ├── compra-credito.service.ts
│   │   ├── contacto.service.ts
│   │   ├── cuenta-bancaria.service.ts
│   │   ├── dashboard.service.ts
│   │   ├── espacio-trabajo.service.ts
│   │   ├── motivo.service.ts
│   │   ├── tarjeta.service.ts
│   │   └── transaccion.service.ts
│   ├── store/                      # Estado global
│   │   └── app-store.ts            # Zustand store principal
│   ├── types/                      # TypeScript types
│   │   └── index.ts                # Interfaces y tipos
│   ├── App.tsx                     # Componente raíz con Router
│   ├── main.tsx                    # Entry point
│   └── index.css                   # Estilos globales y variables CSS
├── components.json                 # Configuración shadcn/ui
├── Dockerfile                      # Imagen Docker multi-stage
├── Dockerfile.dev                  # Imagen para desarrollo
├── generate-icons.js               # Script generación de iconos
├── index.html                      # HTML principal
├── nginx.conf                      # Configuración Nginx
├── package.json                    # Dependencias
├── postcss.config.js               # Configuración PostCSS
├── README.md                       # Este archivo
├── tailwind.config.js              # Configuración Tailwind
├── tsconfig.json                   # Configuración TypeScript
├── tsconfig.node.json              # TS config para Node
└── vite.config.ts                  # Configuración Vite
```

---

## 🎨 Sistema de Diseño

### Paleta de Colores

El sistema utiliza un esquema de colores basado en **variables CSS** para soporte completo de tema oscuro.

#### Tema Oscuro (Dark Mode)

```css
--background: 0 0% 3.9%          /* Fondo principal muy oscuro */
--foreground: 0 0% 98%           /* Texto principal blanco */
--card: 240 5.9% 10%             /* Fondo de cards */
--primary: 0 0% 98%              /* Color primario (blanco) */
--secondary: 0 0% 14.9%          /* Color secundario gris oscuro */
--muted: 0 0% 14.9%              /* Elementos apagados */
--accent: 0 0% 14.9%             /* Color de acento */
--destructive: 0 62.8% 30.6%     /* Rojo para acciones destructivas */
--border: 0 0% 14.9%             /* Bordes sutiles */

/* Colores para gráficos */
--chart-1: 217 72% 58%           /* Azul */
--chart-2: 160 76% 52%           /* Verde */
--chart-3: 30 78% 56%            /* Naranja */
--chart-4: 260 74% 58%           /* Púrpura */
--chart-5: 340 76% 58%           /* Rosa */

/* Sidebar */
--sidebar-background: 240 5.9% 10%
--sidebar-foreground: 240 4.8% 95.9%
--sidebar-primary: 0 0% 98%
--sidebar-accent: 240 3.7% 15.9%
--sidebar-border: 240 3.7% 15.9%
```

### Tipografía

#### Fuente Principal: **Inter**

```css
font-family: "Inter", system-ui, -apple-system, sans-serif;
```

- **Pesos disponibles**: 100 - 900
- **Variable Font**: Soporte completo de OpenType
- **Features**: 
  - `rlig` (ligaduras contextuales)
  - `calt` (alternativas contextuales)

#### Fuente Monospace

```css
font-family: ui-monospace, SFMono-Regular, "SF Mono", Menlo, Monaco, Consolas, monospace;
```

### Espaciado y Border Radius

```javascript
borderRadius: {
  lg: 'var(--radius)',       // 0.5rem (8px)
  md: 'calc(var(--radius) - 2px)',  // 6px
  sm: 'calc(var(--radius) - 4px)',  // 4px
}
```

### Breakpoints Responsivos

```javascript
screens: {
  'sm': '640px',
  'md': '768px',
  'lg': '1024px',
  'xl': '1280px',
  '2xl': '1400px'  // Container max-width
}
```

### Animaciones

El proyecto incluye animaciones suaves con **tailwindcss-animate**:

- `accordion-down` / `accordion-up`
- `fade-in` / `fade-out`
- `slide-in` / `slide-out`
- `scale` transformaciones
- Transiciones suaves en hover/focus

---

## 🧩 Componentes UI

### Biblioteca: shadcn/ui

El proyecto utiliza **shadcn/ui**, un sistema de componentes basado en:
- **Radix UI**: Primitivos accesibles y sin estilos
- **Tailwind CSS**: Estilos utility-first
- **class-variance-authority**: Variantes tipadas

#### Configuración

```json
{
  "style": "new-york",
  "rsc": false,
  "tsx": true,
  "tailwind": {
    "baseColor": "neutral",
    "cssVariables": true
  },
  "iconLibrary": "lucide"
}
```

### Componentes Disponibles

#### Formularios y Entrada
- **Input**: Campo de texto con variantes
- **Textarea**: Área de texto multi-línea
- **Select**: Selector con búsqueda
- **Checkbox**: Checkbox accesible
- **Radio Group**: Grupo de opciones radio
- **Switch**: Toggle switch
- **Calendar**: Selector de fechas
- **Command**: Command palette (⌘K)

#### Navegación
- **Breadcrumb**: Migas de pan
- **Pagination**: Paginación de tablas
- **Tabs**: Pestañas
- **Sidebar**: Navegación lateral

#### Feedback
- **Alert Dialog**: Diálogos de confirmación
- **Dialog**: Modal general
- **Drawer**: Drawer deslizable (móvil)
- **Sheet**: Slide-over panel
- **Tooltip**: Tooltips informativos
- **Toast (Sonner)**: Notificaciones

#### Visualización
- **Card**: Contenedor de contenido
- **Avatar**: Imagen de perfil
- **Badge**: Etiqueta de estado
- **Separator**: Separador visual
- **Skeleton**: Placeholders de carga
- **Scroll Area**: Área scrolleable personalizada

#### Datos
- **Table**: Tabla básica
- **Data Table**: Tabla avanzada con sorting, filtering, pagination
- **Chart**: Wrappers para Recharts

#### Utilidades
- **Popover**: Popup contextual
- **Dropdown Menu**: Menú desplegable
- **Visually Hidden**: Ocultar visualmente pero accesible

---

## 📄 Páginas y Features

### 1. Login Page (`/login`)

**Archivo**: `src/pages/LoginPage.tsx`

- Autenticación OAuth2 con Google
- Diseño minimalista y profesional
- Logo y branding
- Información de privacidad
- Redirección automática si ya está autenticado

### 2. Dashboard Page (`/`)

**Archivo**: `src/pages/DashboardPage.tsx`

**Features**:
- `DashboardStats`: 4 KPIs principales (Balance, Gastos, Resumen, Deuda)
- `MonthlyCashflow`: Gráfico de barras con ingresos vs gastos
- `SpendingByCategory`: Gráfico donut de distribución de gastos
- `BankAccounts`: Tabla de cuentas bancarias con saldos
- `UpcomingPayments`: Compras con cuotas pendientes
- `RecentTransactions`: Últimas 6 transacciones

**Optimizaciones**:
- Caché de datos con Zustand (5 minutos)
- Skeleton loaders durante carga
- Actualización selectiva de componentes

### 3. Movimientos Page (`/movimientos`)

**Archivo**: `src/pages/MovimientosPage.tsx`

**Features**:
- Filtros avanzados (mes, año, motivo, contacto)
- Búsqueda en tiempo real
- Data table con sorting
- Paginación
- Modal de detalle
- Modal de registro/edición
- Eliminación con confirmación

**Componentes**:
- `TransactionDetailsModal`: Vista detallada
- `DeleteConfirmDialog`: Confirmación de eliminación

### 4. Tarjetas de Crédito Page (`/creditos`)

**Archivo**: `src/pages/CreditosPage.tsx`

**Features**:
- Vista de tarjetas tipo "card"
- Indicador de días hasta cierre


### 5. Configuración Page (`/configuracion`)

**Archivo**: `src/pages/ConfiguracionPage.tsx`

**Features**:
- Tabs: Espacio de Trabajo, Preferencias, Notificaciones, Cuenta
- Gestión de espacios de trabajo
- Invitación de miembros
- Gestión de motivos y contactos
- Configuración de cuentas bancarias

---

## 🗂 Gestión de Estado

### Zustand Store

**Archivo**: `src/store/app-store.ts`

#### Estado Global

```typescript
interface AppState {
  user: Usuario | null
  currentWorkspace: EspacioTrabajo | null
  workspaces: EspacioTrabajo[]
  
  // Caché con Maps
  recentTransactions: Map<number, DashboardCache>
  bankAccounts: Map<number, CuentasCache>
  comprasPendientes: Map<number, ComprasPendientesCache>
  dashboardStats: Map<number, DashboardStatsCache>
}
```

#### Sistema de Caché

**Duración**: 5 minutos

```typescript
const CACHE_DURATION = 5 * 60 * 1000

interface DashboardCache {
  data: TransaccionDTOResponse[]
  timestamp: number
}
```

**Validación**:

```typescript
const isCacheValid = (timestamp: number): boolean => {
  return Date.now() - timestamp < CACHE_DURATION
}
```

#### Actions

```typescript
// Cargar con caché
loadRecentTransactions(idEspacio, forceRefresh?)
loadBankAccounts(idEspacio, forceRefresh?)
loadComprasPendientes(idEspacio, forceRefresh?)
loadDashboardStats(idEspacio, forceRefresh?)

// Invalidar caché
invalidateRecentTransactions(idEspacio)
invalidateBankAccounts(idEspacio)
invalidateComprasPendientes(idEspacio)
invalidateDashboardStats(idEspacio)
invalidateDashboardCache(idEspacio) // Invalida todo
```

#### Ventajas del Sistema

1. **Performance**: Reduce llamadas innecesarias a la API
2. **UX**: Datos instantáneos en navegación
3. **Granularidad**: Caché por workspace
4. **Invalidación**: Control fino de qué invalidar
5. **Actualización**: Force refresh cuando se necesita

---

## 🌐 Servicios y API

### API Client

**Archivo**: `src/lib/api-client.ts`

```typescript
export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  withCredentials: true,  // Cookies OAuth2
})
```

### Servicios Disponibles

#### Authentication Service
```typescript
authService.checkAuthStatus()
authService.logout()
```

#### Transaction Service
```typescript
transaccionService.registrarTransaccion(data)
transaccionService.removerTransaccion(id)
transaccionService.buscarTransacciones(filters)
transaccionService.buscarTransaccionesRecientes(idEspacio)
```

#### Dashboard Service
```typescript
dashboardService.obtenerStats(idEspacio)
```

#### Workspace Service
```typescript
espacioTrabajoService.registrar(data)
espacioTrabajoService.compartir(email, idEspacio, idAdmin)
espacioTrabajoService.listarPorUsuario(idUsuario)
espacioTrabajoService.obtenerMiembros(idEspacio)
```

#### Bank Account Service
```typescript
cuentaBancariaService.crear(data)
cuentaBancariaService.listarCuentas(idEspacio)
cuentaBancariaService.transferirEntreCuentas(origen, destino, monto)
```

#### Credit Service
```typescript
compraCreditoService.registrarCompra(data)
compraCreditoService.registrarTarjeta(data)
compraCreditoService.listarTarjetas(idEspacio)
compraCreditoService.listarCuotasPorTarjeta(idTarjeta)
compraCreditoService.pagarResumen(request)
compraCreditoService.listarResumenes(idEspacio)
```

#### Contact & Motive Services
```typescript
contactoService.registrar(data)
contactoService.listar(idEspacio)

motivoService.registrar(data)
motivoService.listar(idEspacio)
```

---

## 🛣 Enrutamiento

### React Router v6

**Archivo**: `src/App.tsx`

```typescript
const router = createBrowserRouter([
  { 
    path: '/login', 
    element: <LoginPage /> 
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <DashboardLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'movimientos', element: <MovimientosPage /> },
      { path: 'creditos', element: <CreditosPage /> },
      { path: 'configuracion', element: <ConfiguracionPage /> },
    ],
  },
])
```

### Protected Route

**Archivo**: `src/components/ProtectedRoute.tsx`

- Verifica autenticación con backend
- Carga datos del usuario
- Redirección automática a `/login` si no autenticado
- Loading state durante verificación

---

## ⚙️ Configuración y Requisitos

### Requisitos Previos

- **Node.js**: 18.x o superior
- **npm**: 9.x o superior
- **Backend**: Backend del proyecto corriendo en `localhost:8080`

### Variables de Entorno

**Archivo**: `.env` (opcional)

```bash
# API Backend (si difiere del proxy de Vite)
VITE_API_URL=http://localhost:8080/api

# Entorno
VITE_ENV=development
```

### Configuración Vite

**Archivo**: `vite.config.ts`

```typescript
{
  server: {
    port: 3000,
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
}
```

---

## 🚀 Instalación y Ejecución

### Opción 1: Desarrollo Local

#### 1. Clonar e instalar

```bash
cd ProyectoGastos/frontend
npm install
```

#### 2. Ejecutar en modo desarrollo

```bash
npm run dev
```

La aplicación estará disponible en: http://localhost:3000

**Features en Dev**:
- Hot Module Replacement (HMR)
- Proxy automático a backend
- Source maps completos
- Error overlay

#### 3. Compilar para producción

```bash
npm run build
```

**Output**: `dist/` con archivos optimizados

#### 4. Preview del build

```bash
npm run preview
```

### Opción 2: Docker

#### Dockerfile para Desarrollo

```bash
docker build -f Dockerfile.dev -t finanzas-frontend:dev .
docker run -p 3000:3000 -v $(pwd):/app finanzas-frontend:dev
```

#### Dockerfile para Producción

```bash
docker build -t finanzas-frontend:prod .
docker run -p 80:80 finanzas-frontend:prod
```

### Opción 3: Docker Compose

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Ver logs del frontend
docker-compose logs -f frontend
```

---

## 📱 Progressive Web App (PWA)

### Características PWA

- ✅ **Instalable**: En iOS, Android, Windows, macOS, Linux
- ✅ **Standalone**: Sin barras de navegador
- ✅ **Iconos Multi-resolución**: Optimizados por plataforma
- ✅ **Splash Screens**: Pantalla de carga personalizada
- ✅ **Theme Color**: Color de tema para navegadores
- ✅ **Manifest completo**: Toda la metadata PWA

### Manifest

**Archivo**: `public/manifest.json`

```json
{
  "name": "Finanzas - Gestión de Gastos",
  "short_name": "Finanzas",
  "description": "Gestión inteligente de finanzas personales",
  "theme_color": "#0a0a0a",
  "background_color": "#0a0a0a",
  "display": "standalone",
  "orientation": "portrait",
  "start_url": "/",
  "icons": [...]
}
```

### Generación de Iconos

**Script**: `generate-icons.js`

```bash
npm run generate-icons
```

Genera automáticamente:
- Android: 192x192, 512x512
- iOS: 180x180, 152x152, 120x120, 76x76
- Windows: 144x144, 150x150, 310x310
- Favicons: 16x16, 32x32, 48x48

### Instalación en Dispositivos

#### iOS/Safari
1. Abrir en Safari
2. Tocar el botón "Compartir"
3. Seleccionar "Agregar a pantalla de inicio"

#### Android/Chrome
1. Abrir en Chrome
2. Menú → "Agregar a pantalla de inicio"
3. O banner de instalación automático

#### Desktop
1. Chrome: Ícono de instalación en barra de direcciones
2. Edge: Menú → "Apps" → "Instalar esta aplicación"

---

## 📱 Responsive Design

### Estrategia Mobile-First

El diseño se construye primero para móviles y se expande con breakpoints.

### Breakpoints

```javascript
sm: '640px'   // Móvil grande / Tablet pequeña
md: '768px'   // Tablet
lg: '1024px'  // Laptop
xl: '1280px'  // Desktop
2xl: '1400px' // Desktop grande
```

### Componentes Responsivos

#### Sidebar
- **Desktop**: Sidebar lateral fija
- **Móvil**: Drawer deslizable

#### Dashboard
- **Desktop**: Grid de 2-3 columnas
- **Tablet**: Grid de 2 columnas
- **Móvil**: Stack vertical

#### Tablas
- **Desktop**: Tabla completa
- **Móvil**: Cards con información clave

#### Modales
- **Desktop**: Dialog centrado
- **Móvil**: Drawer desde abajo (Vaul)

### Mobile-Specific Features

#### Floating Action Button (FAB)

**Archivo**: `src/components/MobileActionsFAB.tsx`

- Botón flotante solo en móvil
- Drawer con acciones rápidas:
  - Registrar transacción
  - Movimiento entre cuentas
  - Compra con crédito
  - Pagar resumen tarjeta

#### Touch Interactions
- Swipe gestures en drawers
- Tap areas optimizadas (min 44x44px)
- Touch feedback en botones

### Hook de Detección

**Archivo**: `src/hooks/use-mobile.tsx`

```typescript
export function useIsMobile() {
  // Breakpoint: 768px
  // Returns: boolean
}
```

---

## ⚡ Optimización y Performance

### Code Splitting

**Archivo**: `vite.config.ts`

```typescript
build: {
  rollupOptions: {
    output: {
      manualChunks: {
        'react-vendor': ['react', 'react-dom', 'react-router-dom'],
        'ui-vendor': ['@radix-ui/react-dialog', '@radix-ui/react-dropdown-menu'],
      },
    },
  },
}
```

**Beneficios**:
- Caché más efectivo
- Carga inicial más rápida
- Actualizaciones más pequeñas

### Lazy Loading

```typescript
const DashboardPage = lazy(() => import('@/pages/DashboardPage'))
```

### Memoización

```typescript
const MemoizedComponent = React.memo(ExpensiveComponent)

const memoizedValue = useMemo(() => {
  return computeExpensiveValue(a, b)
}, [a, b])

const memoizedCallback = useCallback(() => {
  doSomething(a, b)
}, [a, b])
```

### Virtualización

Para listas largas:
- `@tanstack/react-virtual`
- Renderiza solo elementos visibles

### Imágenes

```typescript
// SVG como componentes (tree-shakeable)
import { Logo } from '@/assets/logo.svg?react'

// Lazy loading de imágenes
<img loading="lazy" src="..." alt="..." />
```

### Bundle Size

**Optimizaciones**:
- Tree shaking automático
- Minificación
- Compresión gzip/brotli (Nginx)
- Chunks optimizados

**Análisis**:
```bash
npm run build -- --mode analyze
```

### Caché de Datos

- Zustand store con caché de 5 minutos
- Invalidación granular
- Force refresh opcional
- Reduce llamadas API en 70-80%

---

## 🐳 Despliegue con Docker

### Dockerfile Multi-Stage

**Archivo**: `Dockerfile`

#### Stage 1: Builder

```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
```

#### Stage 2: Nginx

```dockerfile
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Configuración Nginx

**Archivo**: `nginx.conf`

```nginx
server {
  listen 80;
  root /usr/share/nginx/html;
  index index.html;
  
  # Gzip compression
  gzip on;
  gzip_types text/css application/javascript application/json;
  
  # SPA fallback
  location / {
    try_files $uri $uri/ /index.html;
  }
  
  # Cache static assets
  location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
  }
}
```

### Build y Deploy

```bash
# Build imagen
docker build -t finanzas-frontend:1.0.0 .

# Run contenedor
docker run -d -p 80:80 --name finanzas-frontend finanzas-frontend:1.0.0

# Con Docker Compose
docker-compose up -d frontend
```

---

## ✨ Mejores Prácticas

### Código Limpio

- ✅ **Componentes pequeños**: Max 200 líneas
- ✅ **Nombres descriptivos**: Variables, funciones, componentes
- ✅ **Comentarios**: Solo cuando añaden valor
- ✅ **Constantes**: Magic numbers y strings en constantes

### TypeScript

- ✅ **Tipado estricto**: No usar `any` sin razón
- ✅ **Interfaces**: Para props y datos
- ✅ **Enums**: Para valores fijos
- ✅ **Type guards**: Validación de tipos en runtime

### React

- ✅ **Hooks personalizados**: Lógica reutilizable
- ✅ **Memoización**: React.memo, useMemo, useCallback
- ✅ **Keys únicas**: En listas
- ✅ **Error boundaries**: Manejo de errores
- ✅ **Lazy loading**: Componentes pesados

### Accesibilidad

- ✅ **Radix UI**: Componentes accesibles por defecto
- ✅ **ARIA labels**: En elementos interactivos
- ✅ **Keyboard navigation**: Tab, Enter, Escape
- ✅ **Focus visible**: Estados de foco claros
- ✅ **Contraste**: WCAG AA mínimo

### Performance

- ✅ **Code splitting**: Chunks por ruta
- ✅ **Lazy loading**: Componentes e imágenes
- ✅ **Caché**: Datos en Zustand y React Query
- ✅ **Debounce**: En búsquedas y filtros
- ✅ **Virtualización**: Listas largas

### Estilos

- ✅ **Tailwind CSS**: Utility-first
- ✅ **Variables CSS**: Para theming
- ✅ **Mobile-first**: Responsive design
- ✅ **Consistencia**: Design tokens

### Git

- ✅ **Commits semánticos**: feat, fix, docs, style, refactor
- ✅ **Branches**: feature/, fix/, hotfix/
- ✅ **Pull requests**: Code review obligatorio

---

## 📚 Recursos Adicionales

### Documentación Oficial

- [React](https://react.dev/)
- [TypeScript](https://www.typescriptlang.org/docs/)
- [Vite](https://vitejs.dev/)
- [Tailwind CSS](https://tailwindcss.com/docs)
- [shadcn/ui](https://ui.shadcn.com/)
- [Radix UI](https://www.radix-ui.com/primitives/docs/overview/introduction)
- [Zustand](https://zustand-demo.pmnd.rs/)
- [React Router](https://reactrouter.com/)
- [Recharts](https://recharts.org/)

### Componentes y Estilos

- [Lucide Icons](https://lucide.dev/)
- [Vaul Drawer](https://vaul.emilkowal.ski/)
- [Sonner Toast](https://sonner.emilkowal.ski/)
- [date-fns](https://date-fns.org/)

### Herramientas

- [TypeScript Playground](https://www.typescriptlang.org/play)
- [Tailwind Play](https://play.tailwindcss.com/)
- [Can I Use](https://caniuse.com/)

---

## 👥 Contribución

### Flujo de Trabajo

1. Fork del repositorio
2. Crear rama feature (`git checkout -b feature/nueva-caracteristica`)
3. Commit de cambios (`git commit -m 'feat: agregar nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Crear Pull Request

### Estándares

- Seguir la estructura de carpetas existente
- Usar TypeScript estricto
- Componentes funcionales con hooks
- Tests para nueva funcionalidad
- Documentar props con JSDoc

---

## 📧 Contacto

Para consultas o soporte relacionado con el frontend:
- **Repositorio**: [GitHub](https://github.com/niclesanti/ProyectoGastos)
- **Issues**: [GitHub Issues](https://github.com/niclesanti/ProyectoGastos/issues)

---

**Versión del documento**: 1.0.0  
**Última actualización**: Enero 2026  
**Mantenido por**: Equipo de Desarrollo
