# Sistema de Caché del Dashboard

## Descripción General

Este documento describe el sistema de caché implementado para optimizar la carga de datos del dashboard, minimizando las llamadas a la base de datos y mejorando la experiencia del usuario.

## Arquitectura

### 1. Store Global (Zustand)

Ubicación: `src/store/app-store.ts`

El store maneja dos tipos de datos con caché:
- **Transacciones Recientes**: Últimas 6 transacciones del espacio de trabajo
- **Cuentas Bancarias**: Lista de cuentas bancarias con sus saldos actuales

#### Estructura del Caché

```typescript
interface DashboardCache {
  data: Transaccion[]
  timestamp: number
}

interface CuentasCache {
  data: CuentaBancaria[]
  timestamp: number
}
```

Cada caché incluye:
- `data`: Los datos en sí
- `timestamp`: Momento en que se cargaron los datos

#### Configuración

- **Duración del caché**: 5 minutos
- **Alcance**: Por espacio de trabajo (cada workspace tiene su propio caché)

### 2. Componentes Actualizados

#### BankAccounts.tsx
- Consume el endpoint: `GET /api/cuentabancaria/listar/{idEspacioTrabajo}`
- Muestra estados de carga (Skeleton) mientras se obtienen los datos
- Maneja errores con mensajes informativos
- Respeta la estructura de tipos de `CuentaBancaria`

#### RecentTransactions.tsx
- Consume el endpoint: `GET /api/transaccion/buscarRecientes/{idEspacio}`
- Formatea fechas de manera inteligente:
  - "Hoy, HH:mm" para transacciones del día actual
  - "Ayer, HH:mm" para transacciones de ayer
  - "d MMM" para fechas anteriores
- Muestra estados de carga y errores
- Respeta la estructura de tipos de `Transaccion`

### 3. Hook Personalizado: `useDashboardCache`

Ubicación: `src/hooks/useDashboardCache.ts`

Proporciona funciones para gestionar la invalidación del caché:

```typescript
const {
  refreshTransactions,    // Recargar solo transacciones
  refreshBankAccounts,    // Recargar solo cuentas bancarias
  refreshDashboard,       // Recargar todo el dashboard
  invalidateCache,        // Invalidar sin recargar
} = useDashboardCache()
```

## Flujo de Datos

### Carga Inicial

1. El usuario accede al dashboard
2. Los componentes `BankAccounts` y `RecentTransactions` se montan
3. Cada componente verifica si existe caché válido en el store
4. Si el caché existe y es válido (< 5 minutos):
   - Se usan los datos del caché inmediatamente
   - No se realiza llamada a la API
5. Si el caché no existe o expiró:
   - Se llama a la API correspondiente
   - Se actualiza el store con los nuevos datos
   - Se guarda el timestamp actual

### Actualización del Caché

El caché se invalida automáticamente cuando:

#### 1. Nueva Transacción (`TransactionModal`)
```typescript
await createTransaccionMutation.mutateAsync(transaccionData)
await refreshDashboard() // Actualiza transacciones Y cuentas
```

**Razón**: Una nueva transacción puede:
- Aparecer en "Transacciones Recientes"
- Modificar el saldo de una cuenta bancaria

#### 2. Transferencia entre Cuentas (`AccountTransferModal`)
```typescript
await transferenciaMutation.mutateAsync(...)
await refreshBankAccounts() // Solo actualiza cuentas
```

**Razón**: Una transferencia modifica los saldos de las cuentas involucradas

## Ventajas del Sistema

### Performance
- **Reducción de llamadas a BD**: Los datos se reutilizan durante 5 minutos
- **Carga instantánea**: Si el caché es válido, la UI se actualiza sin esperas
- **Paralelización**: Las actualizaciones forzadas se ejecutan en paralelo

### Experiencia de Usuario
- **Estados de carga claros**: Skeletons mientras se cargan los datos
- **Manejo de errores**: Mensajes informativos cuando algo falla
- **Actualización inteligente**: Solo se recarga lo necesario

### Mantenibilidad
- **Código centralizado**: Toda la lógica de caché en el store
- **Hook reutilizable**: `useDashboardCache` se puede usar en cualquier componente
- **Tipado fuerte**: TypeScript garantiza la consistencia de los datos

## Casos de Uso

### Escenario 1: Usuario navega al dashboard
```
1. Dashboard se carga
2. Componentes verifican caché (vacío)
3. Se llaman ambos endpoints
4. Datos se muestran y se cachean
5. Durante 5 minutos, cualquier visita al dashboard usa el caché
```

### Escenario 2: Usuario crea una transacción
```
1. Usuario completa el modal de transacción
2. Se registra la transacción en el backend
3. refreshDashboard() se ejecuta automáticamente
4. Ambas tablas se actualizan con datos frescos
5. Nuevo caché válido por 5 minutos más
```

### Escenario 3: Usuario realiza transferencia
```
1. Usuario transfiere dinero entre cuentas
2. Se ejecuta la transferencia en el backend
3. refreshBankAccounts() actualiza solo las cuentas
4. Transacciones recientes mantienen su caché (no cambiaron)
5. Optimización: solo se recarga lo necesario
```

## APIs Consumidas

### Transacciones Recientes
```
GET /api/transaccion/buscarRecientes/{idEspacio}
Response: List<TransaccionDTOResponse>
```

### Cuentas Bancarias
```
GET /api/cuentabancaria/listar/{idEspacioTrabajo}
Response: List<CuentaBancariaDTOResponse>
```

## Configuración Avanzada

Para ajustar la duración del caché, modifica la constante en `app-store.ts`:

```typescript
const CACHE_DURATION = 5 * 60 * 1000 // 5 minutos en milisegundos
```

Para invalidar manualmente el caché desde cualquier componente:

```typescript
import { useDashboardCache } from '@/hooks'

const { invalidateCache } = useDashboardCache()
invalidateCache() // Solo invalida, no recarga
```

## Próximas Mejoras Potenciales

1. **Caché persistente**: Usar localStorage para mantener datos entre sesiones
2. **Actualización en tiempo real**: WebSockets para actualizar cuando otros usuarios modifican datos
3. **Optimistic updates**: Actualizar la UI antes de confirmar con el servidor
4. **Políticas de caché por tipo**: Diferentes duraciones para diferentes datos
5. **Prefetching**: Cargar datos anticipadamente basándose en patrones de navegación

## Conclusión

Este sistema de caché proporciona un balance óptimo entre:
- Reducción de carga en el servidor
- Datos actualizados cuando importa
- Experiencia de usuario fluida
- Código mantenible y escalable
