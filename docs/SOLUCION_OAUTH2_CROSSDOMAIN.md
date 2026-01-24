# ✅ SOLUCIÓN IMPLEMENTADA: OAuth2 Cross-Domain

## 🎯 Problema Diagnosticado
Las **cookies de sesión (JSESSIONID) no podían viajar entre dominios diferentes** (Render ↔ Vercel). Aunque OAuth2 autenticaba correctamente y creaba el usuario en la BD, la sesión se perdía al redirigir al frontend porque el navegador bloqueaba las cookies cross-domain.

## 🔧 Cambios Implementados

### 1. ✅ Nuevo: OAuth2LoginSuccessHandler
**Archivo:** `backend/src/main/java/com/campito/backend/config/OAuth2LoginSuccessHandler.java`

- Configura cookies con `SameSite=None; Secure` para permitir cross-domain
- Agrega logs detallados para debugging
- Maneja la redirección al frontend después de autenticación exitosa

### 2. ✅ Actualizado: SecurityConfig
**Archivo:** `backend/src/main/java/com/campito/backend/config/SecurityConfig.java`

**Cambios realizados:**
- Integrado `OAuth2LoginSuccessHandler` con `@Autowired`
- Configurada gestión de sesión: `SessionCreationPolicy.IF_REQUIRED`
- Agregado `OncePerRequestFilter` que modifica automáticamente todas las cookies `JSESSIONID` para incluir `SameSite=None; Secure`
- Actualizado OAuth2 login para usar el success handler personalizado

### 3. ✅ Actualizado: application-prod.properties
**Archivo:** `backend/src/main/resources/application-prod.properties`

**Nuevas propiedades agregadas:**
```properties
# Configuración de Cookies de Sesión para Cross-Domain
server.servlet.session.cookie.same-site=none
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.max-age=86400
server.servlet.session.timeout=24h
```

### 4. ✅ Mejorado: CorsConfig
**Archivo:** `backend/src/main/java/com/campito/backend/config/CorsConfig.java`

**Cambios:**
- Headers específicos agregados: `Cookie`, `Authorization`, `Content-Type`, etc.
- Exposed headers: `Set-Cookie`, `Access-Control-Allow-Credentials`
- Métodos adicionales: `PATCH`
- Documentación JavaDoc agregada

### 5. ✅ Frontend ya estaba correcto
- `withCredentials: true` en `api-client.ts` ✓
- `credentials: 'include'` en `authService.ts` ✓

---

## 🚀 PASOS PARA DESPLEGAR

### Paso 1: Commit y Push
```bash
cd c:\dev\ProyectoGastos
git add .
git commit -m "fix: Configurar cookies cross-domain para OAuth2 en producción"
git push origin main
```

### Paso 2: Verificar Variables de Entorno en Render

**Dashboard de Render > Tu Backend Service > Environment**

Asegúrate de tener:
```
FRONTEND_URL=https://proyecto-gastos-frontend.vercel.app
GOOGLE_CLIENT_ID=tu_client_id_real
GOOGLE_CLIENT_SECRET=tu_client_secret_real
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

**⚠️ IMPORTANTE:** `FRONTEND_URL` NO debe tener trailing slash (`/` al final)

### Paso 3: Google Cloud Console - Verificar OAuth2

**Console > APIs & Services > Credentials > OAuth 2.0 Client IDs**

**Orígenes autorizados de JavaScript:**
```
https://proyectogastos-backend.onrender.com
https://proyecto-gastos-frontend.vercel.app
```

**URIs de redireccionamiento autorizadas:**
```
https://proyectogastos-backend.onrender.com/login/oauth2/code/google
```

### Paso 4: Forzar Re-deploy en Render (si es necesario)
```
Render Dashboard > Tu Service > Manual Deploy > Deploy latest commit
```

---

## 🔍 VERIFICACIÓN POST-DESPLIEGUE

### A) Logs de Render

Después de hacer login, busca en los logs estos mensajes:

```
✅ Usuario autenticado exitosamente: tuemail@gmail.com
✅ Session ID creado: 1A2B3C4D5E6F...
✅ Cookie de sesión configurada con SameSite=None y Secure
✅ Redirigiendo a: https://proyecto-gastos-frontend.vercel.app/
```

### B) DevTools del Navegador (F12)

**1. Network Tab > Login OAuth2:**
- Busca la petición a `/login/oauth2/code/google`
- En **Response Headers** debe aparecer:
  ```
  Set-Cookie: JSESSIONID=...; Path=/; HttpOnly; Secure; SameSite=None
  ```

**2. Application Tab > Cookies:**
- Expande el dominio `https://proyectogastos-backend.onrender.com`
- Debe haber una cookie `JSESSIONID` con:
  - ✓ HttpOnly: true
  - ✓ Secure: true
  - ✓ SameSite: None

**3. Network Tab > `/api/auth/status`:**
- En **Request Headers** debe incluir:
  ```
  Cookie: JSESSIONID=...
  ```
- **Response Status:** `200 OK`
- **Response Body:**
  ```json
  {
    "authenticated": true,
    "user": {
      "id": "...",
      "nombre": "Tu Nombre",
      "email": "tuemail@gmail.com",
      "fotoPerfil": "..."
    }
  }
  ```

---

## 📊 Flujo Esperado (Correcto)

```
1. Usuario → "Continuar con Google"
   ↓
2. Frontend redirige → backend.onrender.com/oauth2/authorization/google
   ↓
3. Google autentica → backend.onrender.com/login/oauth2/code/google
   ↓
4. Backend (OAuth2LoginSuccessHandler):
   ✓ CustomOidcUserService crea/actualiza usuario en BD
   ✓ Crea sesión HTTP
   ✓ Envía cookie: JSESSIONID; SameSite=None; Secure; HttpOnly
   ✓ Redirige a: frontend.vercel.app/
   ↓
5. Frontend carga → AuthContext.useEffect()
   ↓
6. Ejecuta → authService.checkAuthStatus()
   ↓
7. Petición → backend.onrender.com/api/auth/status
   - Request incluye: Cookie: JSESSIONID=...
   ↓
8. Backend valida sesión:
   Response: { authenticated: true, user: {...} }
   ↓
9. ✅ Usuario ve el DASHBOARD
```

---

## 🐛 Troubleshooting

### Problema: Cookie no se crea
**Síntomas:** En DevTools > Application > Cookies no aparece `JSESSIONID`

**Soluciones:**
1. Verifica que `FRONTEND_URL` en Render no tenga trailing slash (`/`)
2. Asegúrate que el backend esté usando HTTPS (Render siempre usa HTTPS ✓)
3. Revisa logs de Render por errores durante el login

### Problema: Cookie no se envía en requests
**Síntomas:** En Network > Request Headers no aparece `Cookie: JSESSIONID=...`

**Soluciones:**
1. Verifica CORS: debe permitir `allowCredentials: true`
2. Frontend debe tener `withCredentials: true` (ya está ✓)
3. Intenta en modo incógnito (para descartar extensiones del navegador)

### Problema: 401 en `/api/auth/status`
**Síntomas:** La petición retorna Unauthorized aunque hay cookie

**Soluciones:**
1. La sesión expiró (24 horas). Haz login nuevamente.
2. Verifica que el backend esté leyendo la cookie correctamente
3. Revisa logs de Spring Security en Render

### Problema: Render no actualiza el código
**Solución:**
```bash
# Forzar redeploy desde terminal
git commit --allow-empty -m "Trigger Render rebuild"
git push origin main
```

O desde Render Dashboard:
```
Settings > Manual Deploy > Deploy latest commit
```

---

## 📝 Notas Técnicas

### Por qué `SameSite=None; Secure`?
- **SameSite=None:** Permite que la cookie viaje entre dominios diferentes (Render ↔ Vercel)
- **Secure:** Obligatorio cuando `SameSite=None`. Solo funciona con HTTPS.
- **HttpOnly:** Previene acceso desde JavaScript (seguridad XSS)

### Por qué antes no funcionaba?
Por defecto, Spring Security usa `SameSite=Lax`, que bloquea cookies cross-site en navegadores modernos (Chrome, Firefox, Edge).

### ¿Es seguro?
Sí, siempre y cuando:
- ✓ HTTPS esté habilitado (Render y Vercel lo garantizan)
- ✓ CORS esté correctamente configurado (solo tu frontend puede hacer requests)
- ✓ `HttpOnly` esté activado (JavaScript no puede robar la cookie)

---

## ✅ Checklist Final

Antes de declarar éxito, verifica:

- [ ] Commit y push realizados
- [ ] Variables de entorno en Render correctas (sin trailing slash en `FRONTEND_URL`)
- [ ] Google OAuth2 actualizado con URLs de producción
- [ ] Render desplegó la última versión del código
- [ ] Logs de Render muestran mensajes de éxito
- [ ] DevTools muestra cookie `JSESSIONID` con `SameSite=None`
- [ ] `/api/auth/status` retorna `200 OK` con `authenticated: true`
- [ ] Dashboard carga correctamente después del login

---

**Última actualización:** 24 de enero de 2026
**Versión:** 1.0
