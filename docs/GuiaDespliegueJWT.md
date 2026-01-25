# 🔧 Guía de Configuración para Despliegue en Producción

## 📋 Resumen del Problema Resuelto

El problema principal era que **Spring Security usa cookies de sesión por defecto**, las cuales **NO funcionan entre dominios diferentes** (Vercel ↔ Render) debido a las políticas de SameSite y CORS.

### ✅ Solución Implementada: Autenticación JWT

Hemos migrado de sesiones basadas en cookies a **tokens JWT (JSON Web Tokens)**, que son el estándar para arquitecturas distribuidas modernas.

---

## 🛠️ Cambios Realizados en el Backend

### 1. **Dependencias agregadas** ([pom.xml](backend/pom.xml))
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### 2. **Nuevas clases creadas**
- `JwtTokenProvider`: Genera y valida tokens JWT
- `JwtAuthenticationFilter`: Intercepta requests y autentica vía JWT
- `OAuth2AuthenticationSuccessHandler`: Maneja el éxito de OAuth2 y genera JWT

### 3. **Configuración actualizada** ([SecurityConfig.java](backend/src/main/java/com/campito/backend/config/SecurityConfig.java))
- Cambio de `SessionCreationPolicy.IF_REQUIRED` a `SessionCreationPolicy.STATELESS`
- Filtro JWT agregado antes del filtro de autenticación de Spring
- OAuth2 ahora redirige a `/oauth2/callback` con el token en la URL

### 4. **Variables de entorno necesarias** (Render)
Debes agregar en Render → Environment Variables:

```
JWT_SECRET=tu_secreto_super_seguro_minimo_256_bits_aqui_123456789012345678901234567890
```

**⚠️ IMPORTANTE**: Genera un secret seguro con al menos 256 bits. Puedes usar:
```bash
openssl rand -base64 32
```

O en PowerShell:
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

---

## 🌐 Cambios Realizados en el Frontend

### 1. **authService.ts actualizado**
- Ahora guarda el token JWT en `localStorage`
- Envía el token en el header `Authorization: Bearer <token>`
- Limpia el token en caso de error 401

### 2. **api-client.ts actualizado**
- Interceptor de request: Agrega el token JWT automáticamente a todas las peticiones
- Interceptor de response: Maneja errores 401 y redirige al login

### 3. **OAuthCallback.tsx actualizado**
- Captura el token de la URL (`?token=xyz`)
- Lo guarda en `localStorage`
- Redirige al dashboard

---

## ⚙️ Configuración en Google Cloud Console

### **URIs de redirección autorizados (Authorized redirect URIs)**

Debes actualizar las URIs en Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs:

#### Para desarrollo local:
```
http://localhost:8080/login/oauth2/code/google
```

#### Para producción en Render:
```
https://proyectogastos-backend.onrender.com/login/oauth2/code/google
```

**Nota**: Reemplaza `proyectogastos-backend.onrender.com` con tu URL real de Render.

### **Orígenes de JavaScript autorizados (Authorized JavaScript origins)**

```
https://proyecto-gastos-frontend.vercel.app
https://proyectogastos-backend.onrender.com
http://localhost:3000
http://localhost:8080
```

**Nota**: Reemplaza con tus URLs reales.

---

## 🚀 Pasos para Desplegar

### 1. **Backend en Render**

1. Asegúrate de que tu repositorio esté actualizado con los cambios
2. En Render Dashboard → ProyectoGastos-Backend → Environment:
   ```
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATASOURCE_URL=<tu_url_postgresql>
   SPRING_DATASOURCE_USERNAME=<usuario>
   SPRING_DATASOURCE_PASSWORD=<password>
   GOOGLE_CLIENT_ID=<client_id>
   GOOGLE_CLIENT_SECRET=<client_secret>
   FRONTEND_URL=https://proyecto-gastos-frontend.vercel.app
   JWT_SECRET=<genera_uno_seguro_aqui>
   ```

3. **Redeploy manual** desde Render Dashboard

### 2. **Frontend en Vercel**

1. Asegúrate de que tu repositorio esté actualizado con los cambios
2. En Vercel Dashboard → Settings → Environment Variables:
   ```
   VITE_API_URL=https://proyectogastos-backend.onrender.com
   ```

3. **Redeploy** desde Vercel Dashboard (o hacer push a la rama principal)

### 3. **Google Cloud Console**

1. Ve a [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. Selecciona tu proyecto
3. Edita tus credenciales OAuth 2.0
4. **Actualiza las URIs de redirección** como se indicó arriba
5. **Guarda los cambios**

---

## 🧪 Cómo Probar

### Flujo completo:

1. **Usuario visita** `https://proyecto-gastos-frontend.vercel.app/login`
2. **Click en "Continuar con Google"**
3. **Google redirige a** `https://proyectogastos-backend.onrender.com/oauth2/authorization/google`
4. **Usuario autoriza en Google**
5. **Google redirige a** `https://proyectogastos-backend.onrender.com/login/oauth2/code/google`
6. **Backend procesa OAuth2**, guarda usuario en BD, genera JWT
7. **Backend redirige a** `https://proyecto-gastos-frontend.vercel.app/oauth2/callback?token=<JWT>`
8. **Frontend captura el token**, lo guarda en localStorage
9. **Frontend redirige al dashboard**
10. **Todas las peticiones subsecuentes usan el JWT** en el header `Authorization: Bearer <token>`

### Verificación en DevTools:

1. **Application → Local Storage**: Debe aparecer `auth_token`
2. **Network → Headers**: Las peticiones deben incluir `Authorization: Bearer <token>`
3. **Console**: Logs de autenticación exitosa

---

## 🔍 Debugging

### Si el login no funciona:

1. **Revisar logs de Render**: 
   - Render Dashboard → Logs
   - Buscar errores relacionados con JWT o OAuth2

2. **Verificar variables de entorno en Render**:
   - Todas las variables deben estar configuradas
   - JWT_SECRET debe existir

3. **Verificar URIs en Google Console**:
   - Las URIs de redirección deben coincidir exactamente
   - No debe haber espacios ni barras finales

4. **Frontend DevTools**:
   - Console: Ver logs de autenticación
   - Network: Verificar que el token se envía correctamente
   - Application: Verificar que el token se guarda en localStorage

### Si el usuario se guarda pero no se autentica:

- Verificar que `JWT_SECRET` está configurado en Render
- Verificar que el frontend está capturando el token de la URL
- Verificar que el token se está enviando en las peticiones subsecuentes

---

## 📝 Notas Importantes

- **JWT_SECRET**: Debe ser una cadena larga y segura (mínimo 256 bits)
- **Token expiration**: Por defecto es 7 días (configurado en `jwt.expiration`)
- **CORS**: Ya está configurado para permitir el frontend de Vercel
- **Cookies**: Ya no se usan para autenticación, pero se mantienen para compatibilidad

---

## ✅ Checklist Final

- [ ] Código actualizado en GitHub
- [ ] Variables de entorno configuradas en Render (incluyendo JWT_SECRET)
- [ ] Variables de entorno configuradas en Vercel (VITE_API_URL)
- [ ] URIs de redirección actualizadas en Google Console
- [ ] Backend redeployado en Render
- [ ] Frontend redeployado en Vercel
- [ ] Probado el flujo completo de login
- [ ] Verificado que el dashboard se muestra correctamente
- [ ] Verificado que las peticiones subsecuentes funcionan

---

## 🆘 Soporte

Si encuentras algún problema, revisa:
1. Los logs de Render
2. La consola del navegador (DevTools)
3. Las variables de entorno en ambos servicios
4. La configuración de Google OAuth2
