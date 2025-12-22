# 🎨 Guía para Desarrolladores Java: Frontend con shadcn/ui

## 🤔 ¿Qué es shadcn/ui?

**Para un desarrollador Java:** Imagina que en lugar de agregar Spring Boot Starter como dependencia Maven, tú copias las clases de Spring directamente a tu proyecto y las modificas como quieras.

**shadcn/ui NO es:**
- ❌ Una librería npm como Bootstrap
- ❌ Un paquete que instalas con `npm install`
- ❌ Algo que se actualiza automáticamente

**shadcn/ui ES:**
- ✅ Una colección de componentes que **COPIAS** a tu proyecto
- ✅ Código fuente que se vuelve tuyo y puedes modificar
- ✅ Basado en Radix UI (que SÍ es una librería npm)

## 📁 Estructura de Componentes

```
src/components/ui/          ← Equivalente a tus @Component en Spring
├── button.tsx              ← Como una clase Button.java
├── card.tsx                ← Como una clase Card.java  
├── input.tsx               ← Como una clase Input.java
├── tabs.tsx                ← Como una clase Tabs.java
├── badge.tsx               ← Como una clase Badge.java
└── ...más componentes
```

## 🎯 Componentes que Tienes Ahora

### ✅ Componentes Básicos (Ya instalados)
1. **Button** - Botones con variantes
2. **Card** - Contenedores con header/footer
3. **Input** - Campos de texto
4. **Label** - Etiquetas
5. **Select** - Dropdowns
6. **Avatar** - Imágenes de perfil
7. **DropdownMenu** - Menús desplegables
8. **Separator** - Líneas divisorias

### ✅ Componentes Avanzados (Recién agregados)
9. **Tabs** - Pestañas (Last 3 months, Last 30 days)
10. **Badge** - Indicadores (+12.5%, Success, etc.)
11. **Dialog** - Modales/ventanas emergentes
12. **Table** - Tablas de datos
13. **Skeleton** - Estados de carga

## 🔧 Cómo Usar los Componentes

### Ejemplo 1: Badge (Indicadores de porcentaje)

```tsx
import { Badge } from '@/components/ui/badge'

// En tus componentes:
<Badge variant="success">+12.5%</Badge>
<Badge variant="warning">-20%</Badge>
<Badge variant="default">Active</Badge>
```

**Variantes disponibles:**
- `default` - Estilo principal
- `success` - Verde (para positivos)
- `warning` - Amarillo (para alertas)
- `destructive` - Rojo (para negativos)
- `outline` - Solo borde
- `secondary` - Estilo secundario

### Ejemplo 2: Tabs (Filtros de tiempo)

```tsx
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'

<Tabs defaultValue="6months">
  <TabsList>
    <TabsTrigger value="3months">Last 3 months</TabsTrigger>
    <TabsTrigger value="6months">Last 6 months</TabsTrigger>
    <TabsTrigger value="year">This year</TabsTrigger>
  </TabsList>
  
  <TabsContent value="3months">
    {/* Contenido para 3 meses */}
  </TabsContent>
  <TabsContent value="6months">
    {/* Contenido para 6 meses */}
  </TabsContent>
</Tabs>
```

### Ejemplo 3: Dialog (Modal para formularios)

```tsx
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'

<Dialog>
  <DialogTrigger asChild>
    <Button>Nueva Transacción</Button>
  </DialogTrigger>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Agregar Transacción</DialogTitle>
      <DialogDescription>
        Completa los datos de la transacción
      </DialogDescription>
    </DialogHeader>
    {/* Aquí va tu formulario */}
  </DialogContent>
</Dialog>
```

### Ejemplo 4: Table (Listas de datos)

```tsx
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

<Table>
  <TableHeader>
    <TableRow>
      <TableHead>Descripción</TableHead>
      <TableHead>Monto</TableHead>
      <TableHead>Fecha</TableHead>
    </TableRow>
  </TableHeader>
  <TableBody>
    {transacciones.map((t) => (
      <TableRow key={t.id}>
        <TableCell>{t.descripcion}</TableCell>
        <TableCell>{formatCurrency(t.monto)}</TableCell>
        <TableCell>{formatDate(t.fecha)}</TableCell>
      </TableRow>
    ))}
  </TableBody>
</Table>
```

## 🎨 Cómo Lograr el Look de los Ejemplos

### 1. Usa las Cards correctamente

```tsx
<Card>
  <CardHeader>
    <CardTitle>Total Revenue</CardTitle>
    <CardDescription>Descripción opcional</CardDescription>
  </CardHeader>
  <CardContent>
    {/* Tu contenido aquí */}
  </CardContent>
</Card>
```

### 2. Agrega Badges para métricas

```tsx
<div className="flex items-center gap-2">
  <span className="text-2xl font-bold">$24,500.00</span>
  <Badge variant="success">+12.5%</Badge>
</div>
```

### 3. Usa Tabs para filtros

Ya actualicé el componente `MonthlyCashflow` con Tabs. Revisa el código.

### 4. Mantén el espaciado consistente

```tsx
<div className="space-y-6">        {/* 24px entre secciones */}
  <div className="grid gap-4">     {/* 16px entre cards */}
    <Card>...</Card>
    <Card>...</Card>
  </div>
</div>
```

## 🚀 Próximos Pasos

### Para agregar más componentes de shadcn/ui:

1. Ve a https://ui.shadcn.com/docs/components
2. Busca el componente que necesitas (ej: "tooltip")
3. Copia el código del componente
4. Créalo en `src/components/ui/nombre.tsx`
5. ¡Úsalo en tu aplicación!

### Componentes útiles que podrías agregar:

- **Tooltip** - Para información adicional al hacer hover
- **Popover** - Para menús contextuales
- **Checkbox** - Para selecciones múltiples
- **Radio Group** - Para opciones únicas
- **Switch** - Para toggles on/off
- **Slider** - Para rangos de valores
- **Toast** - Para notificaciones

## 🎯 Tips para Desarrolladores Java

### 1. Composición vs Herencia
En React NO hay herencia. Todo es composición.

**Java (Herencia):**
```java
public class PrimaryButton extends Button {
    // Extiende funcionalidad
}
```

**React (Composición):**
```tsx
<Button variant="default">Click</Button>
<Button variant="secondary">Click</Button>
```

### 2. Props = Parámetros del Constructor

```java
// Java
Button button = new Button("Click me", ButtonVariant.PRIMARY);
```

```tsx
// React
<Button variant="primary">Click me</Button>
```

### 3. Estados = Variables de instancia

```java
// Java
private int count = 0;
public void increment() { count++; }
```

```tsx
// React
const [count, setCount] = useState(0)
const increment = () => setCount(count + 1)
```

## ✅ Checklist para Lograr el Look de shadcn/ui

- [x] Variables CSS configuradas (ya está)
- [x] Componentes UI básicos (ya están)
- [x] Componentes avanzados (recién agregados)
- [x] Tabs para filtros (actualizado)
- [x] Badges para métricas (actualizado)
- [ ] Usar consistentemente los componentes
- [ ] Mantener el espaciado correcto
- [ ] Agregar más componentes según necesites

## 📚 Recursos

- **Documentación oficial**: https://ui.shadcn.com
- **Ejemplos**: https://ui.shadcn.com/examples/dashboard
- **Componentes**: https://ui.shadcn.com/docs/components

## 🤝 Diferencias con Java

| Concepto Java | Equivalente React |
|--------------|-------------------|
| Class | Component (función) |
| Constructor | Props |
| Fields | State (useState) |
| Methods | Functions |
| Extends | Composition |
| @Autowired | Import |
| @Component | export function |

---

**En resumen:** shadcn/ui son componentes que **COPIAS** a tu proyecto, no los instalas. Ya tienes todos los componentes necesarios para lograr el look de los ejemplos. ¡Ahora solo úsalos! 🚀
