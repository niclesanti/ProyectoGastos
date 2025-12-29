# Sección Tarjetas de crédito (CreditosPage)

Al seleccionar un espacio de trabajo en la Sidebar, se debe cargar visualmente las tarjetas que pertenecen a este espacio. Para ello consumir la API de forma moderna y profesional (sin fetch) y mantener los datos en cache:
- GET "/api/comprascredito/tarjetas/{idEspacioTrabajo}"
  - public ResponseEntity<List<TarjetaDTOResponse>> listarTarjetas(@PathVariable Long idEspacioTrabajo)

MUY IMPORTANTE -> No modificar componentes ya definidos para mostrar las tarjetas.

## Modal Agregar Tarjeta de Crédito

Los siguientes cambios que se proponen en este modal es para que el mismo sea mas profesional y se valide las entradas de los usuarios.

### Campos de entrada:

#### Últimos 4 dígitos

- Campo obligatorio:
  - Restricción en el frontend que no se puede enviar el formulario con estos datos vacíos.
  - Restricción en el backend en la validación de datos del DTO que el campo no puede ser null ni estar vacío ni ser una cadena "".
- Solo pueden ser dígitos numéricos del 1 al 9
  - Validar esto en el frontend.
  - Validar esto en el backend en el DTO.

#### Entidad financiera
- Campo obligatorio:
  - Restricción en el frontend que no se puede enviar el formulario con estos datos vacíos.
  - Restricción en el backend en la validación de datos del DTO que el campo no puede ser null ni estar vacío ni ser una cadena "".
- Rellenar selector con las siguientes entidades financieras:
  - Banco Credicoop
  - Banco de Santa Fe
  - Banco Macro
  - Banco Patagonia
  - Banco Santander
  - BBVA
  - BNA
  - Brubank
  - Galicia
  - HSBC
  - ICBC
  - Lemon Cash
  - Mercado Pago
  - Naranja X
  - Personal Pay
  - Ualá

#### Red de pago
- Campo obligatorio:
  - Restricción en el frontend que no se puede enviar el formulario con estos datos vacíos.
  - Restricción en el backend en la validación de datos del DTO que el campo no puede ser null ni estar vacío ni ser una cadena "".
- Cambiar "Visa" por "VISA".

#### Dia de cierre
- Campo obligatorio:
  - Restricción en el frontend que no se puede enviar el formulario con estos datos vacíos.
  - Restricción en el backend en la validación de datos del DTO que el campo no puede ser null ni estar vacío ni ser una cadena "".
- Solo pueden ser dígitos numéricos del 1 al 29
  - Validar esto en el frontend.
  - Validar esto en el backend en el DTO.

#### Dia de vencimiento
- Campo obligatorio:
  - Restricción en el frontend que no se puede enviar el formulario con estos datos vacíos.
  - Restricción en el backend en la validación de datos del DTO que el campo no puede ser null ni estar vacío ni ser una cadena "".
- Solo pueden ser dígitos numéricos del 1 al 29
  - Validar esto en el frontend.
  - Validar esto en el backend en el DTO.

### Botón "Guardar Tarjeta"
Consumir la API de la forma mas profesional y moderna como lo has hecho las anteriores veces.
- POST "/api/comprascredito/registrarTarjeta"
  - public ResponseEntity<TarjetaDTOResponse> registrarTarjeta(@Valid @RequestBody TarjetaDTORequest tarjetaDTO)


## Para cada campo obligatorio:
Implementar de manera genérica para los campos obligatorios la siguiente solución moderna para que los usuarios entiendan que les faltó completar uno o mas campos:

La validación de formularios en aplicaciones modernas ha evolucionado de simples alertas a una **retroalimentación contextual y elegante**. Como diseñador y desarrollador, la mejor práctica actual no es solo "avisar del error", sino guiar al usuario para que lo corrija sin frustración.

En el ecosistema de **shadcn/ui**, la solución estándar y más profesional es utilizar la integración de **React Hook Form** con **Zod** para la validación de esquemas.

---

### 1. El Concepto UX: Validación "Justo a Tiempo"

Para que tu aplicación se sienta profesional, la validación debe seguir estas reglas:

* **No ser punitiva:** No uses colores rojos chillones o alertas intrusivas.
* **Contextual:** El mensaje de error debe aparecer justo debajo del campo afectado.
* **Visualmente sutil:** El borde del componente cambia a un tono "Destructive" (rojo suave) para llamar la atención sin gritar.

---

### 2. Anatomía de un Campo con Error (Componentes shadcn/ui)

Para implementar esto, utilizaremos el componente `<Form />` de shadcn, que ya incluye toda la lógica de accesibilidad.

* **`FormControl`**: Cambia automáticamente el borde del input a `border-destructive` cuando el esquema de Zod detecta un error.
* **`FormMessage`**: Un componente animado que aparece debajo del input con el texto del error. Utiliza un tono rojo mate (`text-destructive`) que combina con tu tema Zinc.

---

### 3. Definición del Esquema de Validación (Zod)

Para que el usuario entienda qué le faltó, los mensajes deben ser específicos. En lugar de un genérico "Campo obligatorio", usa **mensajes de acción**:

```typescript
const formSchema = z.object({
  tipo: z.string().min(1, { message: "Por favor, selecciona un tipo de transacción." }),
  fecha: z.date({ required_error: "La fecha es necesaria para el registro." }),
  monto: z.coerce.number().gt(0, { message: "El monto debe ser mayor a 0." }),
  motivo: z.string().min(1, { message: "Debes asignar un motivo al gasto." }),
});

```

---

### 4. Retroalimentación Global: El uso de "Toasts"

Si el usuario intenta presionar "Guardar" y hay múltiples errores, la mejor práctica moderna es disparar un **Sonner (Toast)**.

* **Mensaje:** *"Error al guardar: Por favor, revisa los campos obligatorios."*
* Esto le da al usuario una señal auditiva y visual de que algo falló, incluso si los campos con error están fuera de su vista inmediata.

---

### 5. Prompt para GitHub Copilot (Implementación de Validación Pro)

Usa este prompt para que Copilot transforme tus modales en formularios con validación profesional:

> **"Implement professional form validation for the 'New Transaction' modal using `react-hook-form`, `zod`, and shadcn/ui `<Form />` components.**
> **1. Validation Schema:** Create a Zod schema where 'Tipo', 'Fecha', 'Monto', and 'Motivo' are required.
> * 'Monto' must be a positive number.
> * Use custom user-friendly messages like: 'Por favor, indica el monto de la operación'.
> 
> 
> **2. Visual Feedback:** >    - Use the `<FormMessage />` component to display errors in `text-destructive` (muted red).
> * Ensure the `Input` and `Select` components automatically get a `border-destructive` style when invalid.
> 
> 
> **3. Submission Logic:** >    - If the form is invalid on submit, trigger a shadcn `toast` (Sonner) notifying the user to check required fields.
> * Disable the 'Guardar' button while the form is submitting (`isSubmitting`).
> 
> 
> **4. Accessibility:** Ensure all error messages have the correct ARIA attributes provided by shadcn's Form wrapper. Keep the Zinc dark theme aesthetic."

---

### Por qué esta es la mejor solución:

1. **Consistencia:** Utiliza los mismos tokens de color que el resto de tu app.
2. **Accesibilidad:** Los lectores de pantalla identificarán automáticamente qué campo tiene el error.
3. **Mantenibilidad:** Toda la lógica de validación vive en el esquema de Zod, no mezclada con tu código HTML/JSX.
