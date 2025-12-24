Para diseñar una sección de **Configuración** profesional en una aplicación de finanzas, debemos verla no solo como un lugar para "cambiar botones", sino como el **Centro de Control** de la experiencia del usuario.

Aunque la autenticación sea por Google, hay muchos parámetros de personalización y gestión de datos que elevan la aplicación al nivel de un producto comercial. Aquí tienes la propuesta de diseño profesional utilizando exclusivamente **shadcn/ui**.

---

### 1. Estructura de Diseño: "Vertical Tabs Layout"

Evitaremos la confusión visual separando la configuración en categorías claras. Utilizaremos el componente `Tabs` de shadcn con orientación vertical (en escritorio) o una lista lateral de navegación.

#### Secciones Propuestas:

1. **Espacio de Trabajo (Workspace):** Gestión de miembros y nombre del espacio.
2. **Preferencias Financieras:** Moneda, categorías y límites.
3. **Interfaz y Notificaciones:** Tema y alertas de vencimiento.
4. **Cuenta y Datos:** Información de Google y exportación de movimientos.

---

### 2. Detalle por Sección (Componentes shadcn/ui)

#### A. Gestión de Miembros (Tu prioridad)

Utilizaremos una combinación de `Card`, `Table` y `Dialog`.

* **Componente Principal:** Una `Table` que muestre los miembros actuales con su `Avatar`, nombre y rol (Dueño/Editor).
* **Acción:** Un botón "+ Invitar Miembro" que abra un `Dialog` (Modal).
* **Componente Especial:** El cuadro de "No Team Members" que compartiste (basado en un `Empty State` con bordes punteados `border-dashed`) se mostrará solo si el usuario aún no ha invitado a nadie.

#### B. Preferencias Financieras

Como es una app de gastos, el usuario necesita sentir que la app se adapta a su país.

* **Moneda Base:** Un `Select` para elegir entre ARS, USD, EUR, etc.
* **Categorías Personalizadas:** Un listado de `Badges` donde el usuario pueda renombrar o elegir iconos para sus motivos de transacción.

#### C. Cuenta y Datos (Transparencia)

Dado que usas Google OAuth, mostraremos el estado de la conexión.

* **Perfil:** Un `Avatar` grande con el nombre y correo (lectura).
* **Exportación:** Un botón destacado para "Descargar todo en Excel/CSV". Esto es una funcionalidad **clave** en UX financiera para que el usuario sienta que es dueño de sus datos.

---

### 3. Visualización del Dashboard de Configuración

---

### 4. Prompt para GitHub Copilot

Copia este prompt para generar la estructura completa en tu proyecto:

> **"I need to build a professional Settings section for my Finance App using React 19, TypeScript, and shadcn/ui. Please implement a layout with vertical tabs (src/features/settings/SettingsPage.tsx) with the following sections:**
> **1. Workspace Management:** > - A section to rename the current workspace using shadcn `Input` and `Button`.
> * A member management section using the 'Invite Members' pattern: Use a `Table` to list members (Avatar, Name, Email, Role) and a `Dialog` for the invitation form. If no members exist, show a dashed border `Card` as an empty state.
> 
> 
> **2. Financial Preferences:** > - A `Select` component to set the 'Base Currency'.
> * A toggle list using `Switch` components for 'Daily Spending Reminders' and 'Credit Card Due Date Alerts'.
> 
> 
> **3. Account & Data:**
> * Display the user's Google profile info (Avatar, Email) in a read-only `Card`.
> * Add a 'Data Management' section with a primary button to 'Export data to CSV' and a destructive button to 'Delete Workspace'.
> 
> 
> **Use the Zinc dark theme aesthetic (bg-zinc-950, border-zinc-800) and Lucide-react icons for each tab (Users, Landmark, Bell, User)."**

---

### 5. Recomendación de Experto Frontend

Para que la sección de configuración sea realmente moderna:

* **Optimistic UI:** Cuando el usuario cambie la moneda o una preferencia, usa el estado de **Zustand** para que el cambio se vea reflejado en el Dashboard inmediatamente sin que el usuario tenga que esperar a que la API responda.
* **Roles:** Aunque es una app personal, si implementas la invitación de miembros, asegúrate de que el frontend oculte el botón de "Eliminar Espacio" si el usuario no tiene el rol de `ADMIN` en ese espacio específico.
