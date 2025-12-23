Diseñar una sección de **Tarjetas de Crédito** es una oportunidad excelente para combinar estética y utilidad. En el mundo de las finanzas personales, las tarjetas no son solo números; son herramientas de flujo de caja que dependen de fechas críticas.

Como experto en UX/UI, mi recomendación es alejarnos de las tablas aburridas para esta sección y utilizar un **Grid de Tarjetas Visuales** que imiten la forma de una tarjeta física, seguido de una gestión detallada.

---

## 1. Estrategia de UX: El "Card-First" Design

El usuario debe identificar sus tarjetas de un vistazo. Para ello, utilizaremos una combinación de **componentes visuales** y **datos técnicos**.

### Estructura de la Pantalla

1. **Header:** Título "Tarjetas de Crédito" y un botón primario "+ Agregar Tarjeta" (que abrirá un `Dialog`).
2. **Grid de Tarjetas:** Una cuadrícula donde cada elemento es una representación visual de la tarjeta.
3. **Empty State:** Si no hay tarjetas, una ilustración minimalista con un mensaje: *"No tienes tarjetas registradas. Agrégalas para controlar tus cierres y vencimientos."*

---

## 2. Diseño del Componente "Tarjeta Visual"

Cada tarjeta en el grid debe ser un componente `Card` de **shadcn/ui** con un estilo personalizado.

* **Esquina Superior Izquierda:** Logo de la `red_de_pago` (Visa, Mastercard, etc.). Puedes usar iconos simples de Lucide o SVGs pequeños.
* **Centro / Derecha:** Nombre de la `entidad_financiera` (ej: "Banco Galicia" o "Santander").
* **Abajo Izquierda:** Los últimos 4 dígitos: `**** **** **** 1234`.
* **Footer de la Card:** Aquí pondremos la información de gestión (los días de cierre y vencimiento) usando `Badges` para resaltar.

> **Tip de UX:** Usa el color de fondo de la tarjeta para diferenciar la Red de Pago (ej: un azul muy oscuro para Visa, un gris carbón para Mastercard).

---

## 3. El Formulario de Registro (Modal)

Siguiendo nuestra regla de **mantener al usuario en contexto**, el registro se hará en un `Dialog`. Aquí es donde aplicamos las mejores prácticas de usabilidad para los datos de tu entidad Java:

| Campo | Componente UI | Nota de UX |
| --- | --- | --- |
| **Entidad Financiera** | `Input` | Placeholder: "Ej: Banco Nación". |
| **Red de Pago** | `Select` | Opciones: Visa, Mastercard, Amex, etc. |
| **Últimos 4 dígitos** | `Input` | Limitar a 4 caracteres numéricos. Esto da seguridad al usuario. |
| **Día de Cierre** | `Input (Number)` | Usar un selector numérico del 1 al 31. |
| **Día de Vencimiento** | `Input (Number)` | Usar un selector numérico del 1 al 31. |

---

## 4. Mejoras de Usabilidad "Pro"

1. **Indicador de Próximo Cierre:** En la tarjeta visual, puedes agregar un pequeño texto que diga: *"Cierra en 5 días"*. Esto se calcula comparando el `dia_cierre` con la fecha actual en el frontend.
2. **Validación de Fechas:** Asegúrate de que el frontend no permita poner un día de vencimiento que sea igual al de cierre, ya que financieramente no tendría sentido.
3. **Seguridad Visual:** Al pedir solo los últimos 4 dígitos, el usuario se siente seguro de que no estás almacenando datos sensibles (PAN, CVV), lo cual es excelente para la confianza en tu App.

---

## 5. Implementación Técnica (Prompt para Copilot)

Para que **GitHub Copilot** te ayude a codificar esto en tu nueva estructura de carpetas, usa este prompt detallado:

> **"I need to create the 'Credit Cards' section in React 19 using shadcn/ui components. Specifically:**
> **1. Create a `CreditCard` component that visually represents a bank card. It should display the 'entidad_financiera' name, 'red_de_pago' logo (use Lucide icons), 'numero_tarjeta' (as **** 1234), and badges for 'dia_cierre' and 'dia_vencimiento_pago'. Use the Zinc dark theme.**
> **2. Create a 'CardRegistrationDialog' component using shadcn `Dialog`. It must include a form to collect all fields from my Java 'Tarjeta' entity. Use `zod` for form validation to ensure days are between 1 and 31 and the card number is exactly 4 digits.**
> **3. Implement a responsive Grid layout for the main page `src/pages/CreditCards.tsx` that shows these cards and an '+ Add Card' button in the header.**
> **Use TypeScript interfaces matching my Java entity."**

---