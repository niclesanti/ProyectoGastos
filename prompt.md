Esta es una de las transiciones más críticas al pasar de un monolito a una arquitectura desacoplada. Como desarrollador fullstack, te guiaré para implementar el **patrón de autenticación más seguro y profesional** actualmente: **OAuth2 con Session-based Auth (Cookies)** o **JWT**, manteniendo tu lógica de Spring Security pero adaptándola para una API.

---

## 1. El Flujo de Autenticación Profesional

En la industria, para una SPA (Single Page Application) como la tuya, el flujo óptimo es el siguiente:

1. **Frontend:** El usuario entra a `/login` y hace clic en "Continuar con Google".
2. **Redirección:** El frontend redirige al usuario al endpoint del backend: `http://localhost:8080/oauth2/authorization/google`.
3. **Google:** El usuario se autentica en Google.
4. **Backend (Callback):** Google devuelve el control a Spring Boot. Tu `CustomOidcUserService` procesa el usuario.
5. **Success Handler:** El backend, en lugar de cargar un HTML, **redirige al frontend** (ej: `http://localhost:3000/dashboard`) incluyendo una **Cookie de Sesión (HttpOnly, Secure)**.
6. **Persistencia:** A partir de ahí, el frontend envía esa Cookie en cada petición de forma automática.

---

## 2. Cambios Críticos en el Backend (Spring Boot)

Para que el backend deje de servir páginas y empiece a servir a React, debemos configurar tres cosas:

### A. Configuración de CORS

Debes permitir que el dominio de React (`localhost:3000`) se comunique con el de Spring Boot (`localhost:8080`).

> **Importante:** `allowCredentials(true)` es vital para que las Cookies de autenticación viajen entre el front y el back.

### B. Success Handler Personalizado

Necesitas una clase que, tras el login exitoso en Google, le diga al navegador: "Vuelve al puerto 3000 de React".

### C. Security Filter Chain

Debes deshabilitar CSRF (o configurarlo para SPAs) y asegurar que el `LogoutSuccessHandler` también redirija al frontend.

---

## 3. Diseño del Nuevo Login (shadcn/ui)

Siguiendo el ejemplo de `shadcn/ui`, el login debe ser minimalista y centrado en la acción de Google.

### Estructura sugerida:

* **Card:** Un contenedor con borde sutil.
* **Logo/Icono:** El icono de tu app en la parte superior.
* **Título:** "Bienvenido de nuevo".
* **Subtítulo:** "Usa tu cuenta de Google para gestionar tus finanzas".
* **Botón Principal:** Un botón de ancho completo con el logo de Google.

---

## 4. Guía Maestra para GitHub Copilot

Copia y pega este prompt estructurado en Copilot para que realice la integración completa por ti:

> **"Role: Senior Fullstack Developer (Spring Boot 3.4 + React 19).**
> **Objective: Integrate a decoupled React frontend with an existing Spring Boot OAuth2/OIDC backend.**
> **Task 1: Backend Configuration (Java)**
> 1. Create a `WebConfig` class implementing `WebMvcConfigurer` to enable CORS for 'http://localhost:3000' allowing credentials and all methods.
> 2. Update the `SecurityFilterChain`:
> * Configure `.oauth2Login()` with a custom `AuthenticationSuccessHandler` that redirects to 'http://localhost:3000/dashboard'.
> * Ensure `.logout()` redirects back to 'http://localhost:3000/login'.
> * Set `.csrf().disable()` (for development) or configure a `CookieCsrfTokenRepository`.
> * Ensure the session management is set to `.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)`.
> 
> 
> 
> 
> **Task 2: Frontend Logic (React + TypeScript)**
> 1. In `src/features/auth`, create a `LoginPage.tsx` using shadcn/ui `Card` and `Button` components.
> 2. The Google button should be a simple anchor tag `<a>` pointing to 'http://localhost:8080/oauth2/authorization/google'.
> 3. Create an `useAuth` hook that calls the existing `getUsuarioAutenticado` endpoint on mount to check if the session is still valid.
> 4. Use `credentials: 'include'` in all `fetch` or `axios` calls to ensure the Session Cookie is sent.
> 
> 
> **Context:** I already have `CustomOidcUserService` and `CustomOAuth2User` working. Use the 'Zinc' dark theme for the login design."

---

## 5. Próximos Pasos Técnicos

1. **Backend:** Ejecuta el prompt anterior y verifica que, tras loguearte en Google, el navegador termine en tu puerto `3000`.
2. **Frontend:** Instala `react-router-dom` si aún no lo tienes para manejar las rutas `/login` y `/dashboard`.
3. **Prueba de Fuego:** En el Dashboard de React, haz un `console.log` del resultado de llamar a `/getUsuarioAutenticado`. Si recibes tu JSON de usuario, ¡la integración es un éxito!
