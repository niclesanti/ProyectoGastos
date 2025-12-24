Como experto en UX/UI y Frontend, mi objetivo para el rediseño de la sección **/Movimientos** es transformar una búsqueda tradicional basada en formularios en una **experiencia de exploración de datos fluida y reactiva**.

En las aplicaciones financieras modernas, el usuario quiere ver sus datos inmediatamente. El problema del diseño actual es que los campos de búsqueda ocupan demasiado espacio vertical, obligando a hacer scroll para ver los resultados.

Aquí tienes la propuesta de **"Smart Ledger" (Libro Contable Inteligente)**.

---

### 1. Concepto UX: "Data-First Navigation"

La idea central es integrar los filtros directamente en una **Barra de Herramientas (Toolbar)** unificada con la tabla. Esto permite que el contenido principal (tus transacciones) suba en la jerarquía visual y los totales actúen como un resumen dinámico que cambia mientras filtras.

#### Distribución Espacial:

* **Header Dinámico:** El título "Movimientos" se acompaña de un resumen visual de los totales filtrados.
* **Filter Bar Compacta:** Una sola fila de selectores inteligentes que reemplaza el bloque de búsqueda actual.
* **Interactive Table:** Una tabla con scroll independiente y estados visuales mejorados.

---

### 2. Elementos del Rediseño Profesional

#### A. Resumen de Flujo (Header)

En lugar de KPI Cards grandes como en el Dashboard, usaremos una **"Summary Bar"** minimalista justo debajo del título.

* **Componentes:** `Badge` y `Separator`.
* **Diseño:** Un texto sutil que diga: *"Mostrando [X] resultados: [Monto Verde] Ingresos | [Monto Rojo] Gastos"*. Esto da feedback inmediato sin distraer de la tabla.

#### B. La "Smart Toolbar" (Filtros Facetados)

Eliminamos el "Card" de búsqueda. Usaremos una barra horizontal con los siguientes componentes de **shadcn/ui**:

1. **Month/Year Selector:** Un `Popover` con un calendario minimalista o un `Select` de dos columnas.
2. **Faceted Filters (Motivo y Contacto):** Usaremos el componente `Command` dentro de un `Popover`. Esto permite al usuario escribir para buscar el motivo o contacto rápidamente en lugar de navegar una lista larga.
3. **Botón "Limpiar":** Un botón `Ghost` que solo aparece cuando hay filtros activos para resetear la vista con un solo clic.

#### C. Data Table de Alto Rendimiento

Utilizaremos **TanStack Table** con componentes de shadcn/ui:

* **Filas con Hover:** Efecto de resaltado sutil al pasar el ratón.
* **Celdas de Monto:** El monto debe estar alineado a la derecha (estándar contable) y usar una fuente monoespaciada para que los números queden alineados verticalmente.
* **Acciones:** Un botón de "Tres puntos" (`DropdownMenu`) al final de cada fila para editar o eliminar rápidamente.

---

### 3. Solución de Diseño Visual

Así se vería la estructura jerárquica:

```text
[Icono] App / Movimientos (Breadcrumb)                  [Notificaciones] [Avatar]
---------------------------------------------------------------------------------
MOVIMIENTOS                                             [Total Ingresos: +$60k]
Explora y filtra tu historial financiero                [Total Gastos:   -$28k]
---------------------------------------------------------------------------------
[ Buscar por contacto... ] [ Mes/Año v ] [ Motivo v ] [ Contacto v ] [X Limpiar]
---------------------------------------------------------------------------------
TABLA DE TRANSACCIONES (Scrollable)
TIPO      | MOTIVO     | CUENTA      | CONTACTO    | FECHA      | MONTO
[Ingreso] | Salario    | Principal   | Empresa ABC | 14/01/2025 | $45.000,00
...

```

---

### 4. Componentes shadcn/ui para Copilot

Indícale a Copilot que utilice estos componentes específicos para asegurar la consistencia:

* **`DataTable`**: El núcleo de la sección.
* **`Command` + `Popover**`: Para los filtros de búsqueda "facetada" (Motive/Contact).
* **`Select`**: Para la selección simple de mes y año.
* **`Badge`**: Para las etiquetas de "Ingreso" y "Gasto" que ya tienes, pero con un estilo más minimalista.
* **`Separator`**: Para dividir sutilmente el header de los filtros.
* **`Skeleton`**: Para mostrar un estado de carga profesional mientras la API de Spring Boot devuelve los datos.

---

### 5. Prompt para GitHub Copilot (Desarrollo Frontend)

Para que Copilot implemente esto de forma profesional, usa este prompt:

> "I need to build a high-fidelity 'Movements' section in React 19 using TypeScript and shadcn/ui.
> 1. **Layout**: Use a 'Data-first' approach. The page should have a header with a `Breadcrumb` (App / Movements) and a dynamic summary bar showing 'Total Income' and 'Total Expenses' based on filtered results.
> 2. **Filter Toolbar**: Create a single-row horizontal toolbar above the table.
> * Add a global search input for 'Description' or 'Contact'.
> * Implement faceted filters using shadcn `Command` and `Popover` for 'Motive' and 'Contact'.
> * Use a `Select` component for 'Month' and 'Year'.
> * Add a 'Clear Filters' ghost button that only shows when filters are active.
> 
> 
> 3. **Data Table**: Use TanStack Table with the design from `image_c3fddf.png`.
> * Align 'Monto' values to the right with a monospace font.
> * Rows should have a subtle hover effect and an action menu (`DropdownMenu`) at the end.
> * Implement pagination at the bottom.
> 
> 
> 4. **Logic**: The totals must update in real-time as filters are applied. Ensure the Zinc dark theme is applied consistently (bg-zinc-950, border-zinc-800)."
> 
> 
