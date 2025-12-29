Para diseñar un modal de **"Detalles de transacción"** que se sienta profesional y de nivel SaaS, debemos aplicar una jerarquía visual clara. El usuario no debe "leer" el modal, debe "escanearlo".

Como diseñador UX/UI, mi propuesta es dividir la información en tres capas: **Resumen Visual (Hero)**, **Información de Negocio** y **Metadatos de Auditoría**.

---

### 1. Propuesta de Estructura UX

* **Cabecera:** Título claro y un subtítulo que contextualice (ej: "Consulta el historial completo de este movimiento").
* **Sección Hero (Impacto):** El monto y el tipo (Ingreso/Gasto) deben ser lo primero que se vea. Usaremos un tamaño de fuente grande y un `Badge` de color.
* **Cuerpo Principal:** Una cuadrícula (Grid) de 2 columnas para los datos operativos (Motivo, Contacto, Fecha, Cuenta).
* **Sección de Notas:** Espacio dedicado para la descripción, ya que puede ser extensa.
* **Pie de Auditoría:** Información técnica (creado por, fecha de creación, espacio de trabajo) en una sección visualmente separada y con tipografía más pequeña (`text-muted-foreground`).

---

### 2. Diseño Visual (Concepto)

---

### 3. Componentes shadcn/ui a utilizar

Para que Copilot lo desarrolle, usaremos:

* **`Dialog`**: El contenedor principal.
* **`Badge`**: Para el `TipoTransaccion`.
* **`Separator`**: Para dividir la información operativa de la auditoría.
* **`Label`**: Para los títulos de cada dato.
* **`ScrollArea`**: Por si la descripción es muy larga.
* **`Lucide Icons`**: Iconos sutiles al lado de los labels para mejorar el reconocimiento visual rápido.

---

### 4. Guía de Mapeo de Información (DTO a UI)

| Sección | Datos del DTO | Estilo Sugerido |
| --- | --- | --- |
| **Hero** | `monto` + `tipo` | Texto extra grande (`text-3xl`) y Badge (Emerald/Rose). |
| **Grid Principal** | `nombreMotivo`, `fecha`, `nombreContacto`, `nombreCuentaBancaria` | Grid de 2x2 con labels en `zinc-400`. |
| **Descripción** | `descripcion` | Bloque de texto con fondo sutil (`bg-zinc-900/50`). |
| **Auditoría** | `nombreEspacioTrabajo`, `nombreCompletoAuditoria`, `fechaCreacion`, `id` | Texto pequeño (`text-xs`) en el pie del modal. |

---

### 5. Prompt para GitHub Copilot (Implementación)

Copia este prompt para generar el componente profesionalmente:

> **"Role: Senior Frontend Developer & UI Designer. Build a 'TransactionDetailsModal' using React 19, TypeScript, and shadcn/ui.**
> **1. Header:** Title 'Detalles de transacción' and subtitle 'Resumen completo del movimiento registrado'.
> **2. Hero Section:** Display the `monto` formatted as `$XX.XXX,XX` in a large, bold font. Next to it, show a `Badge` for `tipo` (Gasto/Ingreso) using semantic colors (emerald/rose).
> **3. Details Grid:** Create a 2-column grid using shadcn components to show:
> * **Motivo:** `nombreMotivo` (with Tag icon)
> * **Fecha:** `fecha` formatted (with Calendar icon)
> * **Contacto:** `nombreContacto` or 'Sin contacto' (with User icon)
> * **Cuenta:** `nombreCuentaBancaria` or 'Sin cuenta' (with Landmark icon)
> 
> 
> **4. Description:** If `descripcion` exists, show it in a dedicated section with a `Separator` above it and a subtle background.
> **5. Audit Footer:** At the bottom, add a gray-toned section (text-xs) separated by another `Separator`. Show:
> * 'Espacio: `nombreEspacioTrabajo`'
> * 'Registrado por: `nombreCompletoAuditoria`'
> * 'Fecha de registro: `fechaCreacion`'
> * 'ID: `id`'
> 
> 
> **Style:** Follow the Zinc dark theme, use 'Sentence case' for all labels, and ensure high visual density without feeling cluttered."

---

### Un detalle "Pro" de UX

Para este modal, te sugiero agregar un botón de **"Copiar ID"** pequeño al lado del ID de la transacción en el pie de página. Es una funcionalidad muy útil cuando un usuario necesita reportar un error o buscar ese registro específico en pgAdmin.
