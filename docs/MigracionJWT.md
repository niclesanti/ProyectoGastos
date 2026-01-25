# 🔐 Migración a Autenticación JWT - Solución al Problema de Login

## 📊 Diagnóstico del Problema

### El Problema Original
Al intentar autenticarse en producción (Vercel + Render), el usuario se registraba correctamente en la base de datos, pero **la sesión no persistía** y el usuario era redirigido nuevamente al login en lugar de al dashboard.

### Causa Raíz
Spring Security, por defecto, utiliza **cookies de sesión basadas en `JSESSIONID`** para mantener la autenticación. Sin embargo, estas cookies:

1. **No funcionan entre dominios diferentes** (`.vercel.app` ↔ `.onrender.com`)
2. Son bloqueadas por políticas de **SameSite** en navegadores modernos
3. Requieren configuración compleja de CORS para cross-domain
4. La cookie se generaba con el dominio del backend, haciendo imposible que el frontend la acceda

### Por qué el Usuario se Guardaba pero no se Autenticaba
- OAuth2 procesaba correctamente el login con Google
- `CustomOidcUserService` guardaba el usuario en la base de datos ✅
- Spring Security generaba una cookie de sesión en el dominio del backend ✅
- Al redirigir al frontend, **la cookie no se enviaba** en las peticiones subsecuentes ❌
- El frontend no podía acceder a la sesión del backend ❌

---

## ✅ Solución Implementada: JSON Web Tokens (JWT)

Hemos migrado la autenticación de **sesiones basadas en cookies** a **tokens JWT**, el estándar de la industria para aplicaciones distribuidas modernas.

### Ventajas de JWT
- ✅ **Sin estado (stateless)**: El servidor no necesita mantener sesiones
- ✅ **Cross-domain**: Funciona perfectamente entre dominios diferentes
- ✅ **Seguro**: Tokens firmados digitalmente que no pueden ser modificados
- ✅ **Escalable**: Ideal para microservicios y arquitecturas distribuidas
- ✅ **Estándar**: Compatible con cualquier cliente (web, mobile, etc.)

---

## 🛠️ Cambios Implementados

### Backend (Spring Boot)

#### 1. Nuevas Dependencias
- `io.jsonwebtoken:jjwt-api` - API de JWT
- `io.jsonwebtoken:jjwt-impl` - Implementación
- `io.jsonwebtoken:jjwt-jackson` - Serialización JSON

#### 2. Nuevas Clases de Seguridad

| Clase | Propósito |
|-------|-----------|
| [JwtTokenProvider](../backend/src/main/java/com/campito/backend/security/JwtTokenProvider.java) | Genera y valida tokens JWT |
| [JwtAuthenticationFilter](../backend/src/main/java/com/campito/backend/security/JwtAuthenticationFilter.java) | Intercepta peticiones y autentica via JWT |
| [OAuth2AuthenticationSuccessHandler](../backend/src/main/java/com/campito/backend/security/OAuth2AuthenticationSuccessHandler.java) | Maneja el éxito de OAuth2 y genera JWT |

#### 3. Configuración de Seguridad Actualizada
- **SessionCreationPolicy**: Cambiado a `STATELESS`
- **JwtAuthenticationFilter**: Agregado a la cadena de filtros
- **OAuth2 Success Handler**: Redirige al frontend con el token JWT

#### 4. Variables de Entorno Nuevas
```properties
JWT_SECRET=<secreto_seguro_minimo_256_bits>
jwt.expiration=604800000  # 7 días en milisegundos
```

### Frontend (React + TypeScript)

#### 1. authService.ts
- **Captura el token JWT** del parámetro de URL después del login OAuth2
- **Almacena el token** en `localStorage`
- **Envía el token** en el header `Authorization: Bearer <token>`

#### 2. api-client.ts (Axios)
- **Interceptor de Request**: Agrega automáticamente el token JWT a todas las peticiones
- **Interceptor de Response**: Maneja errores 401 y limpia el token inválido

#### 3. OAuthCallback.tsx
- **Captura el token** de la URL (`?token=xyz`)
- **Guarda en localStorage**
- **Redirige al dashboard**

---

## 🔄 Flujo de Autenticación Completo

```
1. Usuario → Click "Continuar con Google" en /login
   └─ Frontend redirige a: backend/oauth2/authorization/google

2. Backend → Redirige a Google OAuth2
   └─ Usuario autoriza la aplicación

3. Google → Redirige a: backend/login/oauth2/code/google
   └─ Backend recibe el código de autorización

4. Backend → CustomOidcUserService procesa el usuario
   ├─ Busca o crea usuario en PostgreSQL
   ├─ Actualiza fecha de último acceso
   └─ Devuelve CustomOAuth2User

5. Backend → OAuth2AuthenticationSuccessHandler
   ├─ Genera token JWT con id y email del usuario
   └─ Redirige a: frontend/oauth2/callback?token=<JWT>

6. Frontend → OAuthCallback.tsx
   ├─ Captura el token de la URL
   ├─ Guarda en localStorage: auth_token
   └─ Redirige al dashboard

7. Frontend → Todas las peticiones subsecuentes
   ├─ api-client.ts intercepta la petición
   ├─ Agrega header: Authorization: Bearer <token>
   └─ Backend valida el token con JwtAuthenticationFilter
```

---

## 📦 Pasos para Desplegar

### 1. Backend en Render

#### Agregar Variable de Entorno
```bash
JWT_SECRET=<genera_uno_seguro_aqui>
```

Para generar un secret seguro:
```bash
# Linux/Mac
openssl rand -base64 32

# PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

#### Variables Completas en Render
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
FRONTEND_URL=https://proyecto-gastos-frontend.vercel.app
JWT_SECRET=<tu_secreto_generado>
```

### 2. Frontend en Vercel

#### Variable de Entorno
```
VITE_API_URL=https://proyectogastos-backend.onrender.com
```

### 3. Google Cloud Console

#### Actualizar URIs de Redirección
Ir a: [Google Cloud Console](https://console.cloud.google.com/apis/credentials)

**Authorized redirect URIs**:
```
https://proyectogastos-backend.onrender.com/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/google
```

**Authorized JavaScript origins**:
```
https://proyecto-gastos-frontend.vercel.app
https://proyectogastos-backend.onrender.com
http://localhost:3100
http://localhost:8080
```

---

## 🧪 Cómo Verificar que Funciona

### 1. Flujo de Login
1. Ir a la página de login
2. Click en "Continuar con Google"
3. Autorizar la aplicación
4. **Verificar que redirige al dashboard** ✅

### 2. Verificar Token en DevTools
1. F12 → Application → Local Storage
2. Buscar clave: `auth_token`
3. Valor debe ser un JWT (tres partes separadas por puntos)

### 3. Verificar Headers en Network
1. F12 → Network
2. Hacer cualquier petición (ej. GET /api/cuentas-bancarias)
3. Headers → Request Headers
4. Debe incluir: `Authorization: Bearer eyJhbGc...`

### 4. Verificar en Backend
Render Logs debe mostrar:
```
Token JWT generado exitosamente para el usuario: usuario@email.com
Usuario autenticado via JWT: usuario@email.com
```

---

## 🐛 Troubleshooting

### Error: "Token JWT vacío"
- **Causa**: El token no se guardó en localStorage
- **Solución**: Verificar que el backend está redirigiendo a `/oauth2/callback?token=...`

### Error: "Token JWT inválido"
- **Causa**: JWT_SECRET no coincide o no está configurado
- **Solución**: Verificar que JWT_SECRET está en Render y es correcto

### Usuario redirigido al login después de autenticarse
- **Causa**: Token no se está enviando en las peticiones
- **Solución**: Verificar interceptor de Axios en api-client.ts

### Error 401 en todas las peticiones
- **Causa**: Token expiró o es inválido
- **Solución**: Hacer logout y login nuevamente

---

## 📝 Notas Importantes

### Seguridad
- ⚠️ **JWT_SECRET debe ser único y secreto** en producción
- ⚠️ No compartir el JWT_SECRET en repositorios públicos
- ⚠️ Los tokens expiran después de 7 días (configurable)

### Compatibilidad
- ✅ Funciona en todos los navegadores modernos
- ✅ Compatible con mobile apps (solo agregar el token en headers)
- ✅ Escalable a múltiples instancias de backend

### Performance
- ✅ Sin estado: No hay overhead de sesiones en el servidor
- ✅ Reduce latencia: No hay lookups de sesión en cada petición
- ✅ Cacheable: El token puede ser validado sin acceso a base de datos

---

## 🔍 Documentación Adicional

- [Guía Completa de Despliegue JWT](./GuiaDespliegueJWT.md)
- [Backend - API Endpoints](../backend/README_BACKEND.md)
- [Frontend - Arquitectura](../frontend/README_FRONTEND.md)

---

## ✅ Checklist de Despliegue

- [ ] Código actualizado en GitHub
- [ ] JWT_SECRET configurado en Render
- [ ] VITE_API_URL configurado en Vercel
- [ ] URIs de redirección actualizadas en Google Console
- [ ] Backend redeployado
- [ ] Frontend redeployado
- [ ] Login probado y funcional
- [ ] Token visible en localStorage
- [ ] Dashboard carga correctamente
- [ ] Peticiones incluyen Authorization header

---

**Fecha de Implementación**: 24 de Enero de 2026  
**Versión**: 2.0.0 (Migración a JWT)
