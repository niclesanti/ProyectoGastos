# 🎨 Sistema de Colores para Gráficos

## Problema Resuelto

Los colores de los gráficos ahora son:
- ✅ **Profesionales**: Baja saturación (30-40%) para un look fintech
- ✅ **Dinámicos**: Se generan automáticamente según la cantidad de categorías
- ✅ **Optimizados para Dark Mode**: Luminosidad perfecta (50-60%)

## 📦 Función Principal: `generateChartColors()`

### Ubicación
`frontend/src/lib/utils.ts`

### Uso Básico

```typescript
import { generateChartColors, hslToCSS } from '@/lib/utils'

// Generar 7 colores profesionales
const colors = generateChartColors(7)

// Resultado:
// [
//   "217 35% 50%",  // Azul apagado
//   "160 30% 53%",  // Verde esmeralda suave
//   "30 35% 50%",   // Naranja ámbar
//   "260 30% 53%",  // Violeta mate
//   "340 35% 50%",  // Rosa viejo
//   "0 30% 53%",    // Rojo suave
//   "180 35% 50%",  // Cian apagado
// ]
```

### Convertir a CSS

```typescript
const colors = generateChartColors(5)
const cssColor = hslToCSS(colors[0])
// Resultado: "hsl(217, 35%, 50%)"
```

## 🔧 Ejemplo Práctico: Gráfico de Dona

### Caso Real: Gastos por Categoría Dinámicos

```typescript
import { generateChartColors, hslToCSS } from '@/lib/utils'

export function SpendingByCategoryDynamic() {
  // Datos reales de la API (cantidad variable de categorías)
  const categories = [
    { name: 'Alimentación', value: 35 },
    { name: 'Transporte', value: 25 },
    { name: 'Vivienda', value: 20 },
    { name: 'Salud', value: 10 },
    { name: 'Ocio', value: 7 },
    { name: 'Educación', value: 3 },
  ]

  // 🎨 Generar colores dinámicamente
  const colors = generateChartColors(categories.length)

  // Mapear datos con colores generados
  const chartData = categories.map((cat, index) => ({
    category: cat.name,
    value: cat.value,
    fill: hslToCSS(colors[index]),
  }))

  return (
    <PieChart>
      <Pie data={chartData} dataKey="value" nameKey="category">
        {chartData.map((entry, index) => (
          <Cell key={`cell-${index}`} fill={entry.fill} />
        ))}
      </Pie>
    </PieChart>
  )
}
```

## 🎯 Ventajas del Sistema

### 1. **Escalable**
```typescript
// 3 categorías? ✅
generateChartColors(3)

// 15 categorías? ✅
generateChartColors(15)

// 100 categorías? ✅ (aunque no recomendado visualmente)
generateChartColors(100)
```

### 2. **Consistente**
- Misma saturación (30-40%) en todos los colores
- Luminosidad uniforme (50-60%) para legibilidad
- Distribución equilibrada en el círculo cromático

### 3. **Sin Conflictos**
- Evita amarillos/verdes brillantes (rango 80-120° excluido)
- Rotación inteligente para evitar colores adyacentes similares

## 🔄 Migración de Código Existente

### ❌ Antes (Colores fijos)
```typescript
const chartData = [
  { category: 'vivienda', fill: 'hsl(var(--chart-1))' },
  { category: 'transporte', fill: 'hsl(var(--chart-2))' },
  { category: 'alimentacion', fill: 'hsl(var(--chart-3))' },
  { category: 'ocio', fill: 'hsl(var(--chart-4))' },
]
```

### ✅ Después (Colores dinámicos)
```typescript
const categories = ['vivienda', 'transporte', 'alimentacion', 'ocio']
const colors = generateChartColors(categories.length)

const chartData = categories.map((cat, i) => ({
  category: cat,
  fill: hslToCSS(colors[i]),
}))
```

## 🎨 Paleta de Colores (Referencia)

Los colores se generan en estos rangos de tonalidad (Hue):

| Rango | Color | Saturación | Luminosidad |
|-------|-------|-----------|-------------|
| 200-240° | Azul | 30-35% | 50-53% |
| 150-180° | Verde esmeralda | 30-35% | 50-53% |
| 20-40° | Naranja/Ámbar | 30-35% | 50-53% |
| 260-290° | Violeta | 30-35% | 50-53% |
| 320-350° | Rosa/Magenta | 30-35% | 50-53% |
| 0-15° | Rojo | 30-35% | 50-53% |
| 180-200° | Cian | 30-35% | 50-53% |

## 🚨 Notas Importantes

1. **No usar amarillos brillantes**: El rango 80-120° está excluido porque los amarillos/verdes brillantes tienen mala legibilidad en dark mode.

2. **Máximo recomendado**: 12-15 categorías por gráfico para mantener legibilidad.

3. **Consistencia en la app**: Usa siempre `generateChartColors()` para todos los gráficos, no mezcles con colores manuales.

## 📊 Actualización de Variables CSS

En `frontend/src/index.css`, los colores base ahora son:

```css
.dark {
  /* Colores base apagados para charts */
  --chart-1: 217 35% 55%; /* Azul muted */
  --chart-2: 160 30% 50%; /* Esmeralda muted */
  --chart-3: 30 40% 55%;  /* Ámbar muted */
  --chart-4: 260 30% 55%; /* Violeta muted */
  --chart-5: 340 30% 55%; /* Rosa muted */
}
```

Estos son los colores de respaldo, pero lo ideal es usar `generateChartColors()` para gráficos con categorías dinámicas.

---

**Fecha**: Diciembre 2025  
**Versión**: 1.0  
**Autor**: Sistema de UI Profesional
