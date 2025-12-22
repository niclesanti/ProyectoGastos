# 🔌 Guía de Integración con Backend Spring Boot

Esta guía te ayudará a conectar el frontend React con tu backend Spring Boot.

## 🚀 Estado Actual

El frontend está configurado con:
- **Datos Mock**: Los componentes muestran datos de ejemplo
- **Servicios Listos**: Todos los servicios API están implementados
- **Tipos Definidos**: Las interfaces TypeScript coinciden con tus entidades Java

## 📋 Checklist de Integración

### 1. ✅ Verificar Backend

Asegúrate de que tu backend Spring Boot esté corriendo:

```bash
# En la carpeta backend
mvn spring-boot:run
```

Verifica que esté disponible en: `http://localhost:8080`

### 2. ✅ Configurar CORS en Spring Boot

En tu backend, asegúrate de tener CORS configurado para aceptar requests desde el frontend:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

### 3. ✅ Conectar Dashboard con Backend Real

Abre: `src/pages/DashboardPage.tsx`

Reemplaza los datos mock con llamadas reales:

```typescript
import { useEffect, useState } from 'react'
import { useAppStore } from '@/store/app-store'
import { dashboardService } from '@/services/dashboard.service'
import type { DashboardInfoDTO } from '@/types'

export function DashboardPage() {
  const { currentWorkspace } = useAppStore()
  const [dashboardData, setDashboardData] = useState<DashboardInfoDTO | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadDashboardData = async () => {
      if (!currentWorkspace) return
      
      try {
        const data = await dashboardService.getDashboardInfo(currentWorkspace.id)
        setDashboardData(data)
      } catch (error) {
        console.error('Error loading dashboard:', error)
      } finally {
        setLoading(false)
      }
    }

    loadDashboardData()
  }, [currentWorkspace])

  if (loading) return <div>Cargando...</div>
  if (!dashboardData) return <div>No hay datos</div>

  return (
    <div className="space-y-6">
      {/* Pasar los datos reales a los componentes */}
      <DashboardStats data={dashboardData} />
      <MonthlyCashflow data={dashboardData.ingresosGastos} />
      <SpendingByCategory data={dashboardData.distribucionGastos} />
      {/* ... resto de componentes */}
    </div>
  )
}
```

### 4. ✅ Conectar Movimientos con Backend Real

Abre: `src/pages/MovimientosPage.tsx`

```typescript
import { useEffect, useState } from 'react'
import { transaccionService } from '@/services/transaccion.service'
import { useAppStore } from '@/store/app-store'
import type { Transaccion } from '@/types'

export function MovimientosPage() {
  const { currentWorkspace } = useAppStore()
  const [transactions, setTransactions] = useState<Transaccion[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadTransactions = async () => {
      if (!currentWorkspace) return
      
      try {
        const response = await transaccionService.getAll(currentWorkspace.id)
        setTransactions(response.content)
      } catch (error) {
        console.error('Error loading transactions:', error)
      } finally {
        setLoading(false)
      }
    }

    loadTransactions()
  }, [currentWorkspace])

  // ... resto del componente
}
```

### 5. ✅ Agregar Manejo de Errores

Crea un hook personalizado para manejar errores de API:

Archivo: `src/hooks/useApi.ts`

```typescript
import { useState, useCallback } from 'react'
import { ApiError } from '@/services/api'

export function useApi<T>() {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const execute = useCallback(async (apiCall: () => Promise<T>) => {
    setLoading(true)
    setError(null)
    
    try {
      const result = await apiCall()
      setData(result)
      return result
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.data?.message || err.message)
      } else {
        setError('Ocurrió un error inesperado')
      }
      throw err
    } finally {
      setLoading(false)
    }
  }, [])

  return { data, loading, error, execute }
}
```

### 6. ✅ Implementar Autenticación

Si tu backend usa OAuth2 (Google), actualiza el store:

Archivo: `src/store/app-store.ts`

```typescript
import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Usuario, EspacioTrabajo } from '@/types'

interface AppState {
  user: Usuario | null
  currentWorkspace: EspacioTrabajo | null
  workspaces: EspacioTrabajo[]
  isAuthenticated: boolean
  setUser: (user: Usuario | null) => void
  setCurrentWorkspace: (workspace: EspacioTrabajo | null) => void
  setWorkspaces: (workspaces: EspacioTrabajo[]) => void
  logout: () => void
}

export const useAppStore = create<AppState>()(
  persist(
    (set) => ({
      user: null,
      currentWorkspace: null,
      workspaces: [],
      isAuthenticated: false,
      setUser: (user) => set({ user, isAuthenticated: !!user }),
      setCurrentWorkspace: (workspace) => set({ currentWorkspace: workspace }),
      setWorkspaces: (workspaces) => set({ workspaces }),
      logout: () => set({ user: null, currentWorkspace: null, workspaces: [], isAuthenticated: false }),
    }),
    {
      name: 'app-storage',
    }
  )
)
```

### 7. ✅ Crear Componente de Loading

Archivo: `src/components/Loading.tsx`

```typescript
import { Card } from '@/components/ui/card'

export function LoadingSpinner() {
  return (
    <div className="flex h-screen items-center justify-center">
      <div className="h-32 w-32 animate-spin rounded-full border-b-2 border-primary"></div>
    </div>
  )
}

export function LoadingCard() {
  return (
    <Card className="animate-pulse">
      <div className="h-32 bg-muted"></div>
    </Card>
  )
}
```

### 8. ✅ Endpoints del Backend

Asegúrate de que tu backend tenga estos endpoints:

```
GET    /api/espacios-trabajo                    - Listar espacios
GET    /api/espacios-trabajo/{id}               - Obtener espacio
POST   /api/espacios-trabajo                    - Crear espacio
PUT    /api/espacios-trabajo/{id}               - Actualizar espacio
DELETE /api/espacios-trabajo/{id}               - Eliminar espacio

GET    /api/transacciones/espacio/{id}          - Listar transacciones
GET    /api/transacciones/{id}                  - Obtener transacción
POST   /api/transacciones                       - Crear transacción
PUT    /api/transacciones/{id}                  - Actualizar transacción
DELETE /api/transacciones/{id}                  - Eliminar transacción
GET    /api/transacciones/dashboard/{id}        - Dashboard info

GET    /api/cuentas-bancarias/espacio/{id}      - Listar cuentas
POST   /api/cuentas-bancarias                   - Crear cuenta
PUT    /api/cuentas-bancarias/{id}              - Actualizar cuenta
DELETE /api/cuentas-bancarias/{id}              - Eliminar cuenta
```

## 🧪 Probar la Integración

### 1. Test Manual

1. Inicia el backend: `mvn spring-boot:run`
2. Inicia el frontend: `npm run dev`
3. Abre http://localhost:3000
4. Abre DevTools (F12) → Network tab
5. Navega por la app y verifica las llamadas API

### 2. Test con cURL

```bash
# Probar que el backend responde
curl http://localhost:8080/api/espacios-trabajo

# Desde el frontend, verifica el proxy
curl http://localhost:3000/api/espacios-trabajo
```

## 🐛 Solución de Problemas Comunes

### CORS Error
**Síntoma**: "Access to fetch blocked by CORS policy"
**Solución**: Agrega la configuración CORS en Spring Boot (ver paso 2)

### Network Error
**Síntoma**: "Failed to fetch" o "Network request failed"
**Solución**: 
- Verifica que el backend esté corriendo
- Revisa la URL en `.env`: `VITE_API_URL=http://localhost:8080/api`
- Reinicia el servidor de desarrollo

### 401 Unauthorized
**Síntoma**: Las requests devuelven 401
**Solución**: 
- Implementa el manejo de autenticación
- Asegúrate de que las credenciales se envíen (credentials: 'include')

### Datos no se actualizan
**Síntoma**: Los cambios no se reflejan en la UI
**Solución**: 
- Agrega un sistema de revalidación
- Usa React Query o SWR para cache automático

## 📚 Siguientes Pasos

1. **Implementar formularios**: Crea modales para agregar/editar transacciones
2. **Agregar validación**: Usa Zod o Yup para validar formularios
3. **Optimistic Updates**: Actualiza la UI antes de que responda el backend
4. **Error Boundaries**: Maneja errores globalmente
5. **Toast Notifications**: Muestra mensajes de éxito/error
6. **Infinite Scroll**: Para listas largas de transacciones

## 🎯 Librerías Recomendadas

Para mejorar la integración:

```bash
# React Query (cache y sincronización de datos)
npm install @tanstack/react-query

# Formularios con validación
npm install react-hook-form zod @hookform/resolvers

# Notificaciones toast
npm install sonner

# Gestión de fechas
npm install date-fns
```

## ✅ Checklist Final

- [ ] Backend corriendo en http://localhost:8080
- [ ] CORS configurado en Spring Boot
- [ ] Frontend corriendo en http://localhost:3000
- [ ] Servicios API conectados
- [ ] Manejo de errores implementado
- [ ] Loading states agregados
- [ ] Autenticación funcionando (si aplica)
- [ ] Datos mock reemplazados por datos reales

---

¡Tu aplicación está lista para trabajar con datos reales! 🚀
