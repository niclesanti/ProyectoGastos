# Proyecto Gastos Frontend - Guía de Inicio Rápido

## Para Windows (Método más fácil)

1. Abre PowerShell o CMD en la carpeta `frontend`
2. Ejecuta:
```bash
.\setup.bat
```

Este script instalará todas las dependencias y arrancará el servidor automáticamente.

## Manual

1. Instalar dependencias:
```bash
npm install
```

2. Iniciar el servidor de desarrollo:
```bash
npm run dev
```

3. Abrir http://localhost:3000 en tu navegador

## Requisitos

- Node.js 18+ 
- npm 9+
- Backend corriendo en http://localhost:8080

## Estructura Creada

✅ Configuración base (Vite, TypeScript, Tailwind)
✅ Componentes UI de shadcn (Button, Card, Input, etc.)
✅ Tipos TypeScript basados en tus entidades Java
✅ Servicios API para comunicarse con Spring Boot
✅ Layouts con Sidebar y Header
✅ Dashboard con métricas, gráficos y tablas
✅ Página de Movimientos (transacciones)
✅ Página de Tarjetas de Crédito
✅ Estado global con Zustand
✅ Modo oscuro activado por defecto

## Próximos Pasos

1. **Instalar dependencias**: Ejecuta `npm install`
2. **Revisar el código**: Explora la estructura en `src/`
3. **Conectar con tu backend**: Asegúrate de que Spring Boot esté corriendo
4. **Personalizar**: Modifica colores, componentes según necesites

## Tecnologías

- React 19
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui
- Recharts (gráficos)
- Zustand (estado)
- React Router

## Dudas Frecuentes

**P: ¿Por qué no veo datos reales?**
R: La app usa datos mock por ahora. Conecta los servicios en `src/services/` con tu backend.

**P: ¿Cómo cambio la URL del backend?**
R: Edita el archivo `.env` y cambia `VITE_API_URL`

**P: ¿Cómo agrego nuevos componentes de shadcn/ui?**
R: Copia los componentes de https://ui.shadcn.com y pégalos en `src/components/ui/`

**P: ¿Puedo cambiar a modo claro?**
R: Sí, edita `src/main.tsx` y elimina la línea que agrega la clase 'dark'
