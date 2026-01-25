# 🔧 Configuración de Google OAuth2 - Paso a Paso

## 📍 URL: https://console.cloud.google.com/apis/credentials

---

## 🎯 Paso 1: Acceder a Credenciales

1. Ir a [Google Cloud Console](https://console.cloud.google.com)
2. Seleccionar tu proyecto (debe ser el mismo que usas actualmente)
3. Menú lateral → "APIs & Services" → "Credentials"
4. Buscar tu "OAuth 2.0 Client ID" existente
5. Click en el ícono de lápiz (✏️) para editar

---

## 🌐 Paso 2: Configurar URIs de JavaScript

### Sección: "Authorized JavaScript origins"

**Agregar estas URIs** (si no existen):

```
https://proyecto-gastos-frontend.vercel.app
https://proyectogastos-backend.onrender.com
http://localhost:3100
http://localhost:8080
```

**⚠️ IMPORTANTE**:
- NO agregar barra final (`/`)
- NO incluir `www.`
- Reemplazar con tus URLs reales de Vercel y Render

**Ejemplo Visual**:
```
┌─────────────────────────────────────────────────────┐
│ Authorized JavaScript origins                       │
├─────────────────────────────────────────────────────┤
│ 1  https://proyecto-gastos-frontend.vercel.app      │
│ 2  https://proyectogastos-backend.onrender.com      │
│ 3  http://localhost:3100                            │
│ 4  http://localhost:8080                            │
└─────────────────────────────────────────────────────┘
```

---

## 🔄 Paso 3: Configurar URIs de Redirección

### Sección: "Authorized redirect URIs"

**⚠️ CRÍTICO**: Esta es la parte más importante. Debe ser EXACTAMENTE como se muestra.

#### Para Producción (Render):
```
https://proyectogastos-backend.onrender.com/login/oauth2/code/google
```

#### Para Desarrollo Local:
```
http://localhost:8080/login/oauth2/code/google
```

**Ejemplo Visual**:
```
┌──────────────────────────────────────────────────────────────────────┐
│ Authorized redirect URIs                                             │
├──────────────────────────────────────────────────────────────────────┤
│ 1  https://proyectogastos-backend.onrender.com/login/oauth2/code/google │
│ 2  http://localhost:8080/login/oauth2/code/google                   │
└──────────────────────────────────────────────────────────────────────┘
```

**🚨 ERRORES COMUNES A EVITAR**:

❌ **INCORRECTO**:
```
https://proyecto-gastos-frontend.vercel.app/login/oauth2/code/google
https://proyectogastos-backend.onrender.com/oauth2/callback
https://proyectogastos-backend.onrender.com/
https://proyectogastos-backend.onrender.com/login/oauth2/code/google/
```

✅ **CORRECTO**:
```
https://proyectogastos-backend.onrender.com/login/oauth2/code/google
```

---

## 📋 Checklist de Validación

### Antes de Guardar:

- [ ] **Authorized JavaScript origins**:
  - [ ] Incluye la URL del frontend de Vercel
  - [ ] Incluye la URL del backend de Render
  - [ ] No tiene barras finales (`/`)

- [ ] **Authorized redirect URIs**:
  - [ ] La URI apunta al **BACKEND** (no al frontend)
  - [ ] Termina en `/login/oauth2/code/google`
  - [ ] No tiene espacios ni caracteres extra
  - [ ] Usa la URL exacta de Render

### Después de Guardar:

- [ ] Click en "SAVE" (Guardar) en la parte inferior
- [ ] Esperar confirmación verde: "OAuth client updated"
- [ ] No cerrar la ventana hasta ver la confirmación

---

## 🔍 Cómo Obtener tu URL de Render

1. Ir a [Render Dashboard](https://dashboard.render.com/)
2. Click en tu servicio "ProyectoGastos-Backend"
3. En la parte superior verás la URL: `https://tu-servicio.onrender.com`
4. Copiar esa URL exacta
5. Agregar `/login/oauth2/code/google` al final

**Ejemplo**:
```
URL de Render: https://proyectogastos-backend.onrender.com
URI completa:  https://proyectogastos-backend.onrender.com/login/oauth2/code/google
```

---

## 🧪 Cómo Probar que Está Correcto

### Prueba 1: Verificar Redirección
1. Ir a tu frontend: `https://proyecto-gastos-frontend.vercel.app/login`
2. Click en "Continuar con Google"
3. **Debe redirigir a Google** (no mostrar error)
4. Después de autorizar, **debe volver a tu app** (no mostrar error 400)

### Prueba 2: Verificar en DevTools
1. F12 → Network
2. Click en "Continuar con Google"
3. Buscar en las redirecciones:
   ```
   oauth2/authorization/google
   → accounts.google.com
   → login/oauth2/code/google
   → oauth2/callback?token=...
   ```

### Prueba 3: Verificar Token
1. Después de login exitoso
2. F12 → Application → Local Storage
3. Debe aparecer: `auth_token`

---

## ⚠️ Errores Comunes y Soluciones

### Error 400: "redirect_uri_mismatch"

**Mensaje**:
```
Error: redirect_uri_mismatch
The redirect URI in the request, https://..., does not match
the ones authorized for the OAuth client.
```

**Causa**: La URI de redirección en Google Console no coincide exactamente.

**Solución**:
1. Copiar la URI exacta del mensaje de error
2. Agregarla a "Authorized redirect URIs" en Google Console
3. Asegurarse de que termina en `/login/oauth2/code/google`
4. Guardar y esperar 1-2 minutos

### Error: "Access Blocked: This app's request is invalid"

**Causa**: Falta agregar el dominio en "Authorized JavaScript origins"

**Solución**:
1. Agregar la URL del frontend en "Authorized JavaScript origins"
2. Agregar la URL del backend en "Authorized JavaScript origins"
3. Guardar

### Login funciona en local pero no en producción

**Causa**: Olvidaste agregar las URIs de producción

**Solución**:
1. Verificar que las URIs de Vercel/Render estén en Google Console
2. Verificar que las variables de entorno estén correctas en Render
3. Hacer redeploy del backend

---

## 📸 Ejemplo de Configuración Completa

```
┌──────────────────────────────────────────────────────────────────────┐
│ OAuth 2.0 Client ID                                                  │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│ Name: ProyectoGastos Web Client                                      │
│                                                                      │
│ Authorized JavaScript origins                                        │
│ ┌──────────────────────────────────────────────────────────────────┐ │
│ │ 1  https://proyecto-gastos-frontend.vercel.app                   │ │
│ │ 2  https://proyectogastos-backend.onrender.com                   │ │
│ │ 3  http://localhost:3100                                         │ │
│ │ 4  http://localhost:8080                                         │ │
│ └──────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│ Authorized redirect URIs                                             │
│ ┌──────────────────────────────────────────────────────────────────┐ │
│ │ 1  https://proyectogastos-backend.onrender.com/login/oauth2/code/google │
│ │ 2  http://localhost:8080/login/oauth2/code/google               │ │
│ └──────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│                                    [CANCEL]  [SAVE] ←── Click aquí  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 🎓 Conceptos Importantes

### ¿Por qué apunta al Backend y no al Frontend?

El flujo OAuth2 funciona así:
1. Frontend redirige a: **Backend** `/oauth2/authorization/google`
2. Backend redirige a: **Google** para autorizar
3. Google redirige a: **Backend** `/login/oauth2/code/google` ← URI configurada
4. Backend procesa y genera JWT
5. Backend redirige a: **Frontend** `/oauth2/callback?token=...`

### ¿Por qué debe terminar en `/login/oauth2/code/google`?

Es la ruta estándar de Spring Security OAuth2. **No se puede cambiar** sin modificar código.

---

## ✅ Confirmación Final

Después de guardar los cambios:

```
✅ URIs de JavaScript: 4 entradas (producción + desarrollo)
✅ URIs de Redirección: 2 entradas (producción + desarrollo)
✅ Todas las URIs sin errores de tipeo
✅ Cambios guardados y confirmados
```

**¡Listo para desplegar!** 🚀

---

**Última actualización**: 24 de Enero de 2026
