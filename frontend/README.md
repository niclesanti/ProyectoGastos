# Proyecto Gastos - Frontend

Frontend de la aplicación de gestión de gastos construido con React, TypeScript, Vite, Tailwind CSS y shadcn/ui.

## 🚀 Stack Tecnológico

- **React 19** - Librería UI
- **TypeScript** - Tipado estático
- **Vite** - Build tool
- **Tailwind CSS** - Estilos
- **shadcn/ui** - Componentes UI (basado en Radix UI)
- **Recharts** - Gráficos y visualizaciones
- **Zustand** - Estado global
- **React Router** - Navegación

## 📁 Estructura del Proyecto

```
frontend/
├── src/
│   ├── components/       # Componentes compartidos
│   │   └── ui/          # Componentes de shadcn/ui
│   ├── features/        # Lógica de negocio por módulo
│   │   └── dashboard/   # Componentes del Dashboard
│   ├── layouts/         # Estructuras de página
│   ├── lib/             # Utilidades
│   ├── pages/           # Páginas/Vistas
│   ├── services/        # Clientes API
│   ├── store/           # Estado global (Zustand)
│   ├── types/           # Interfaces TypeScript
│   ├── App.tsx          # Enrutador principal
│   ├── main.tsx         # Punto de entrada
│   └── index.css        # Estilos globales
├── public/              # Archivos estáticos
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

## 🛠️ Instalación

1. Instalar dependencias:
```bash
npm install
```

2. Configurar variables de entorno (opcional):
```bash
# El archivo .env ya está creado con valores por defecto
VITE_API_URL=http://localhost:8080/api
```

## 💻 Comandos

```bash
# Desarrollo
npm run dev

# Build para producción
npm run build

# Preview del build
npm run preview

# Lint
npm run lint
```

## 🎨 Características

### Dashboard
- 📊 Métricas principales (Balance, Gastos, Deudas)
- 📈 Gráficos de flujo de caja (Ingresos vs Gastos)
- 🥧 Distribución de gastos por categoría
- 📋 Lista de cuotas próximas a vencer
- 🔄 Actividad reciente de transacciones

### Movimientos
- 📝 Listado completo de transacciones
- 🔍 Búsqueda y filtros
- ✅ Diferenciación visual entre ingresos y gastos

### Tarjetas de Crédito
- 💳 Gestión de compras a crédito
- 📊 Progreso de pagos de cuotas
- 📅 Fechas de vencimiento

## 🌙 Modo Oscuro

La aplicación viene configurada por defecto en **modo oscuro**. Los colores siguen la paleta de shadcn/ui para dark mode.

## 🔌 Integración con Backend

El frontend está configurado para conectarse al backend Spring Boot en `http://localhost:8080/api`.

La comunicación se realiza mediante:
- **Fetch API** con credenciales incluidas
- **Servicios separados** por entidad (transacciones, cuentas, etc.)
- **Tipos TypeScript** que mapean los DTOs del backend Java

## 📦 Componentes UI Disponibles

Todos los componentes base de shadcn/ui están listos para usar:
- Button
- Card
- Input
- Label
- Select
- Avatar
- DropdownMenu
- Separator

## 🧪 Próximos Pasos

1. Instalar dependencias: `npm install`
2. Iniciar el servidor de desarrollo: `npm run dev`
3. Abrir http://localhost:3000 en tu navegador
4. El backend debe estar corriendo en http://localhost:8080

## 📝 Notas para Desarrolladores Java

Si vienes del mundo Java/Spring Boot, esta es la equivalencia:

- **`types/`** = Tus DTOs y modelos
- **`services/`** = Tus Feign Clients o RestTemplate
- **`components/ui/`** = Librería de componentes (sin lógica de negocio)
- **`features/`** = Tus paquetes de dominio (dashboard, transacciones, etc.)
- **`store/`** = Similar a un cache o contexto de sesión
- **`pages/`** = Controladores (cada página es una ruta)

## 🎯 Tecnologías Clave

### React 19
Uso de componentes funcionales con hooks. Todo es TypeScript.

### Tailwind CSS
Estilos utility-first. Todas las clases CSS están inline.

### shadcn/ui
Componentes copiables y customizables. No es una librería npm, los componentes están en `components/ui/`.

### Zustand
Gestión de estado más simple que Redux. Similar a un singleton en Java.
