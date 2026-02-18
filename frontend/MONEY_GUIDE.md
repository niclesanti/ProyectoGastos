# Guía de Uso: Sistema MoneyDecimal

Esta guía documenta el sistema de manejo de valores monetarios implementado en el frontend de ProyectoGastos.

## 📋 Tabla de Contenidos

- [Resumen](#resumen)
- [Componentes](#componentes)
- [Hooks](#hooks)
- [Utilidades](#utilidades)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Mejores Prácticas](#mejores-prácticas)

---

## Resumen

El sistema MoneyDecimal proporciona manejo preciso de valores monetarios usando la librería `decimal.js`. Este sistema incluye:

- **MoneyDecimal**: Clase wrapper para operaciones monetarias precisas
- **MoneyDisplay**: Componente para mostrar valores monetarios
- **MoneyInput**: Input especializado para entrada de valores monetarios
- **useMoney**: Hook con operaciones monetarias comunes

### Arquitectura

```
┌─────────────────┐
│   API Layer     │ ← Transformación automática JSON ↔ MoneyDecimal
├─────────────────┤
│ Types & Models  │ ← MoneyDecimal para responses, MoneyValue para requests
├─────────────────┤
│   Components    │ ← MoneyDisplay, MoneyInput, useMoney
├─────────────────┤
│   Utilities     │ ← formatCurrency(), toNumber(), transformers
└─────────────────┘
```

---

## Componentes

### MoneyDisplay

Componente para mostrar valores monetarios con formato automático.

#### Props

| Prop | Tipo | Default | Descripción |
|------|------|---------|-------------|
| `value` | `MoneyValue \| null \| undefined` | - | Valor a mostrar (required) |
| `showCurrency` | `boolean` | `true` | Mostrar símbolo de moneda |
| `decimals` | `number` | `2` | Número de decimales |
| `fallback` | `string` | `"$0.00"` | Texto cuando value es null/undefined |
| `className` | `string` | `""` | Clases CSS adicionales |
| `colored` | `boolean` | `false` | Colorear según valor (verde/rojo) |

#### Ejemplos

```tsx
// Uso básico
<MoneyDisplay value={workspace.saldo} />
// Output: "$1,234.56"

// Sin símbolo de moneda
<MoneyDisplay value={transaction.monto} showCurrency={false} />
// Output: "1234.56"

// Con color según valor
<MoneyDisplay value={balance} colored />
// Verde si > 0, Rojo si < 0, Gris si = 0

// Con fallback personalizado
<MoneyDisplay value={null} fallback="N/A" />
// Output: "N/A"
```

---

### MoneyInput

Input especializado para entrada de valores monetarios con validación automática.

#### Props

| Prop | Tipo | Default | Descripción |
|------|------|---------|-------------|
| `value` | `number \| null` | - | Valor actual (required) |
| `onChange` | `(value: number \| null) => void` | - | Callback al cambiar valor (required) |
| `showPrefix` | `boolean` | `true` | Mostrar prefijo "$" |
| `min` | `number` | - | Valor mínimo permitido |
| `max` | `number` | - | Valor máximo permitido |
| `allowNegative` | `boolean` | `false` | Permitir valores negativos |

#### Ejemplo

```tsx
const [amount, setAmount] = useState<number | null>(null)

<MoneyInput
  value={amount}
  onChange={setAmount}
  min={0}
  max={1000000}
  placeholder="0.00"
/>
```

**Características:**
- Validación automática de formato numérico
- Filtrado de caracteres no numéricos
- Aplicación de restricciones min/max
- Soporte para valores decimales

---

## Hooks

### useMoney

Hook que proporciona operaciones comunes con valores monetarios.

#### API Completa

```tsx
const {
  // Comparaciones
  compare,
  isPositive,
  isNegative,
  isZero,
  isGreaterThan,
  isLessThan,
  
  // Aritmética
  add,
  subtract,
  multiply,
  divide,
  abs,
  
  // Agregaciones
  sum,
  average,
  max,
  min,
  
  // Utilidades
  asNumber,
} = useMoney()
```

#### Métodos

##### Comparaciones

```tsx
const { compare, isPositive, isLessThan } = useMoney()

// Comparar dos valores (-1, 0, o 1)
const result = compare(balance1, balance2)

// Verificar si es positivo
if (isPositive(balance)) {
  // ...
}

// Verificar si es menor que otro
if (isLessThan(saldoActual, montoRequerido)) {
  toast.error('Saldo insuficiente')
}
```

##### Aritmética

```tsx
const { add, subtract, multiply, divide } = useMoney()

// Sumar valores
const total = add(amount1, amount2, amount3)

// Restar valores
const remaining = subtract(saldoActual, montoGasto)

// Multiplicar por factor
const withTax = multiply(baseAmount, 1.21)

// Dividir
const montoPorCuota = divide(montoTotal, cantidadCuotas)
```

##### Agregaciones

```tsx
const { sum, average, max, min } = useMoney()

// Sumar array de valores
const totalGastos = sum(transacciones.map(t => t.monto))

// Promedio
const promedioMensual = average(gastosMensuales)

// Máximo/Mínimo
const mayorGasto = max(...gastos)
const menorIngreso = min(...ingresos)
```

##### Utilidades

```tsx
const { asNumber } = useMoney()

// Convertir a number para display o cálculos simples
const numericValue = asNumber(moneyDecimal)
```

---

## Utilidades

### formatCurrency()

Formatea un valor monetario como string con formato de moneda.

```tsx
import { formatCurrency } from '@/lib/utils'

formatCurrency(1234.56)           // "$1,234.56"
formatCurrency(moneyDecimal)      // "$1,234.56"
```

### toNumber()

Convierte MoneyValue (number | MoneyDecimal) a number de forma segura.

```tsx
import { toNumber } from '@/lib/utils'

const value1 = toNumber(123.45)           // 123.45
const value2 = toNumber(moneyDecimal)     // 123.45
```

**Importante:** Esta función es robusta y maneja datos cacheados que pueden ser numbers antiguos o nuevos MoneyDecimal.

---

## Ejemplos de Uso

### Ejemplo 1: Mostrar Balance en Dashboard

```tsx
import { MoneyDisplay } from '@/components/MoneyDisplay'

export function DashboardCard({ stats }) {
  return (
    <Card>
      <CardTitle>Balance Total</CardTitle>
      <CardContent>
        <MoneyDisplay 
          value={stats?.balanceTotal ?? 0} 
          colored 
        />
      </CardContent>
    </Card>
  )
}
```

### Ejemplo 2: Validar Saldo en Pago

```tsx
import { useMoney } from '@/hooks/useMoney'
import { MoneyDisplay } from '@/components/MoneyDisplay'

export function PaymentForm({ cuenta, monto }) {
  const { isLessThan, subtract } = useMoney()
  
  if (isLessThan(cuenta.saldoActual, monto)) {
    return (
      <Alert variant="destructive">
        <AlertTitle>Saldo insuficiente</AlertTitle>
        <AlertDescription>
          Disponible: <MoneyDisplay value={cuenta.saldoActual} />
          <br />
          Necesitas: <MoneyDisplay value={monto} />
        </AlertDescription>
      </Alert>
    )
  }
  
  const saldoRestante = subtract(cuenta.saldoActual, monto)
  
  return (
    <div>
      <p>Saldo después del pago:</p>
      <MoneyDisplay value={saldoRestante} colored />
    </div>
  )
}
```

### Ejemplo 3: Calcular Totales

```tsx
import { useMoney } from '@/hooks/useMoney'

export function TransactionList({ transactions }) {
  const { sum } = useMoney()
  
  const ingresos = transactions
    .filter(t => t.tipo === 'Ingreso')
    .map(t => t.monto)
  
  const gastos = transactions
    .filter(t => t.tipo === 'Gasto')
    .map(t => t.monto)
  
  const totalIngresos = sum(ingresos)
  const totalGastos = sum(gastos)
  
  return (
    <div>
      <p>Ingresos: <MoneyDisplay value={totalIngresos} colored /></p>
      <p>Gastos: <MoneyDisplay value={totalGastos} colored /></p>
    </div>
  )
}
```

### Ejemplo 4: Input de Monto

```tsx
import { MoneyInput } from '@/components/MoneyInput'

export function TransactionForm() {
  const [monto, setMonto] = useState<number | null>(null)
  
  return (
    <Form>
      <FormField
        name="monto"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Monto</FormLabel>
            <FormControl>
              <MoneyInput
                value={monto}
                onChange={setMonto}
                min={0}
                placeholder="0.00"
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />
    </Form>
  )
}
```

---

## Mejores Prácticas

### ✅ DO (Hacer)

1. **Usar MoneyDisplay para mostrar valores**
   ```tsx
   // ✅ Correcto
   <MoneyDisplay value={saldo} />
   ```

2. **Usar useMoney para operaciones**
   ```tsx
   // ✅ Correcto
   const { subtract, isLessThan } = useMoney()
   const remaining = subtract(saldo, monto)
   ```

3. **Usar MoneyInput para captura de valores**
   ```tsx
   // ✅ Correcto
   <MoneyInput value={amount} onChange={setAmount} />
   ```

4. **Usar null-coalescing para valores opcionales**
   ```tsx
   // ✅ Correcto
   <MoneyDisplay value={stats?.balance ?? 0} />
   ```

### ❌ DON'T (No hacer)

1. **No llamar .toNumber() directamente sin toNumber()**
   ```tsx
   // ❌ Incorrecto (puede fallar con cached data)
   <span>${value.toNumber()}</span>
   
   // ✅ Correcto
   <MoneyDisplay value={value} />
   ```

2. **No usar operadores aritméticos directos**
   ```tsx
   // ❌ Incorrecto
   const total = monto1 + monto2
   
   // ✅ Correcto
   const { add } = useMoney()
   const total = add(monto1, monto2)
   ```

3. **No comparar con < > directamente**
   ```tsx
   // ❌ Incorrecto
   if (saldo < monto) { }
   
   // ✅ Correcto
   const { isLessThan } = useMoney()
   if (isLessThan(saldo, monto)) { }
   ```

4. **No asumir que value es siempre MoneyDecimal**
   ```tsx
   // ❌ Incorrecto (cached data puede ser number)
   const num = value.toNumber()
   
   // ✅ Correcto
   const num = toNumber(value)
   ```

---

## Tipos

### MoneyValue

Union type que acepta tanto number como MoneyDecimal:

```typescript
type MoneyValue = number | MoneyDecimal
```

Este tipo se usa para backward compatibility durante la migración.

### Branded Types

Para type-safety adicional:

```typescript
type Monto = MoneyValue & { readonly __brand: 'monto' }
type Saldo = MoneyValue & { readonly __brand: 'saldo' }
type Total = MoneyValue & { readonly __brand: 'total' }
type Cuota = MoneyValue & { readonly __brand: 'cuota' }
```

---

## Transformación Automática

El sistema incluye transformación automática en la capa API:

```typescript
// Response: JSON → MoneyDecimal
api.get('/transacciones') 
// → { monto: MoneyDecimal }

// Request: MoneyDecimal → number
api.post('/transacciones', { monto: MoneyDecimal })
// → Serializa como number en JSON
```

**Campos transformados automáticamente:**
- `saldo`, `saldoActual`
- `monto`, `montoTotal`, `montoCuota`
- `ingresos`, `gastos`
- `balanceTotal`, `gastosMensuales`, `resumenMensual`, `deudaTotalPendiente`
- Y más... (ver `money-transformer.ts`)

---

## Migración desde Code Anterior

Si tienes código usando numbers directamente:

```tsx
// ANTES (Fase 1-2)
<span>${workspace.saldo.toFixed(2)}</span>

// DESPUÉS (Fase 4+)
<MoneyDisplay value={workspace.saldo} />
```

```tsx
// ANTES
if (saldo < monto) { }

// DESPUÉS
const { isLessThan } = useMoney()
if (isLessThan(saldo, monto)) { }
```

---

## Recursos Adicionales

- **decimal.js Documentation**: https://mikemcl.github.io/decimal.js/
- **MoneyDecimal Source**: `src/lib/money.ts`
- **Money Transformer**: `src/services/money-transformer.ts`
- **Type Definitions**: `src/types/money.ts`

---

## Troubleshooting

### Error: "toNumber is not a function"

**Causa:** Intentando llamar `.toNumber()` en un number (data cacheada pre-transformación)

**Solución:** Usar helper `toNumber()` de utils:
```tsx
import { toNumber } from '@/lib/utils'
const num = toNumber(value) // Funciona con number y MoneyDecimal
```

### Error: "Type 'MoneyDecimal' is not assignable to type 'ReactNode'"

**Causa:** Intentando renderizar MoneyDecimal directamente en JSX

**Solución:** Usar `MoneyDisplay`:
```tsx
<MoneyDisplay value={moneyValue} />
```

### Valores incorrectos en cálculos

**Causa:** Usando operadores aritméticos directos (+, -, *, /)

**Solución:** Usar `useMoney` hook:
```tsx
const { add, subtract } = useMoney()
const result = subtract(a, b)
```

---

**Última actualización:** Fase 4 - Component Optimization (Febrero 2026)
