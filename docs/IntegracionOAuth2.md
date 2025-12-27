# 🔐 Integración OAuth2 - Frontend React + Backend Spring Boot

## ✅ Implementación Completada

Se ha integrado exitosamente el sistema de autenticación OAuth2 con Google entre el nuevo frontend React (desacoplado) y el backend Spring Boot.

---

## 📋 Componentes Implementados

### **Backend (Spring Boot)**

#### 1. **CorsConfig.java**
- Configura CORS para permitir requests desde `localhost:3000` (frontend)
- Habilita credenciales (cookies) cross-origin
- Permite métodos: GET, POST, PUT, DELETE, OPTIONS

#### 2. **SecurityConfig.java** (Actualizado)
- URLs de éxito/fallo redirigen al frontend (`http://localhost:3000`)
- Logout redirige al frontend
- Permite endpoint `/api/auth/status` sin autenticación

#### 3. **AuthController.java** (Nuevo)
- **GET `/api/auth/status`**: Verifica si el usuario está autenticado
- Devuelve: `{ authenticated: boolean, user: UsuarioDTO | null }`

#### 4. **Variables de entorno**
- `FRONTEND_URL`: URL del frontend (default: `http://localhost:3000`)

---

### **Frontend (React + TypeScript)**

#### 1. **authService.ts**
Servicio centralizado para manejar autenticación:
- `loginWithGoogle()`: Redirige a `/oauth2/authorization/google`
- `checkAuthStatus()`: Verifica si el usuario está autenticado
- `getCurrentUser()`: Obtiene datos del usuario actual
- `logout()`: Cierra sesión

#### 2. **AuthContext.tsx**
Context API para estado global de autenticación:
- `user`: Datos del usuario autenticado
- `isAuthenticated`: Boolean de estado de autenticación
- `isLoading`: Loading state durante verificación
- `login()`: Inicia flujo OAuth2
- `logout()`: Cierra sesión
- `refreshAuth()`: Refresca estado de autenticación

#### 3. **ProtectedRoute.tsx**
HOC (Higher Order Component) para proteger rutas:
- Muestra loading mientras verifica autenticación
- Redirige a `/login` si el usuario no está autenticado
- Renderiza children si está autenticado

#### 4. **LoginPage.tsx** (Actualizado)
- Integrado con `AuthContext`
- Botón de Google redirige al flujo OAuth2 del backend
- Auto-redirige a dashboard si ya está autenticado

#### 5. **Header.tsx** (Actualizado)
- Muestra avatar del usuario autenticado
- Dropdown con nombre, email y opción de cerrar sesión
- Integrado con `AuthContext`

#### 6. **App.tsx** (Actualizado)
- Envuelto con `AuthProvider`
- Rutas del dashboard protegidas con `ProtectedRoute`

---

## 🔄 Flujo de Autenticación

```
1. Usuario visita: http://localhost:3000/
   ↓
2. ProtectedRoute verifica autenticación
   ↓
3. Si NO está autenticado → Redirige a /login
   ↓
4. Usuario hace clic en "Continuar con Google"
   ↓
5. Frontend redirige a: http://localhost:8080/oauth2/authorization/google
   ↓
6. Backend inicia flujo OAuth2 con Google
   ↓
7. Usuario se autentica en Google
   ↓
8. Google redirige a: http://localhost:8080/login/oauth2/code/google
   ↓
9. Backend procesa autenticación:
   - Guarda/actualiza usuario en BD
   - Crea sesión (cookie JSESSIONID)
   ↓
10. Backend redirige a: http://localhost:3000/
    ↓
11. Frontend verifica autenticación con /api/auth/status
    ↓
12. AuthContext actualiza estado con datos del usuario
    ↓
13. ProtectedRoute permite acceso al Dashboard
```

---

## 🚀 Cómo Probar

### **1. Levantar los servicios con Docker**

```powershell
# Desde la raíz del proyecto
docker-compose down
docker-compose up -d --build
```

### **2. Verificar que los servicios estén corriendo**

```powershell
docker-compose ps
```

Deberías ver:
- `postgres-campito` (Puerto 5432)
- `springboot-campito` (Puerto 8080)
- `react-campito` (Puerto 3000)
- `pgadmin-campito` (Puerto 5050)

### **3. Abrir el frontend**

Navega a: **http://localhost:3000/**

- Si no estás autenticado, te redirigirá a `/login`
- Haz clic en "Continuar con Google"
- Autentica con tu cuenta de Google
- Serás redirigido automáticamente al dashboard

### **4. Verificar autenticación**

En las DevTools del navegador (F12), ve a la pestaña **Application** → **Cookies** → **http://localhost:8080**

Deberías ver una cookie llamada `JSESSIONID`

### **5. Probar cierre de sesión**

- En el dashboard, haz clic en tu avatar (esquina superior derecha)
- Selecciona "Cerrar sesión"
- Serás redirigido a `/login`

---

## 🔧 Endpoints del Backend

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/auth/status` | Verifica estado de autenticación | No |
| GET | `/usuario/me` | Obtiene datos del usuario actual | Sí |
| GET | `/oauth2/authorization/google` | Inicia flujo OAuth2 | No |
| POST | `/logout` | Cierra sesión | Sí |

---

## 🐛 Troubleshooting

### **Problema: Cookie no se está enviando**
**Solución:** Asegúrate de que `credentials: 'include'` está en todos los `fetch()` del frontend.

### **Problema: CORS errors**
**Solución:** 
1. Verifica que `FRONTEND_URL` esté configurado en el backend
2. Revisa que `CorsConfig.java` esté cargando correctamente
3. Comprueba los logs del backend: `docker-compose logs backend`

### **Problema: Redirección infinita**
**Solución:**
1. Limpia las cookies del navegador
2. Verifica que `/api/auth/status` esté en la lista de URLs permitidas en `SecurityConfig`

### **Problema: Usuario no se guarda en BD**
**Solución:**
1. Verifica logs de backend: `docker-compose logs backend`
2. Comprueba que las credenciales de Google OAuth2 estén configuradas en `.env`
3. Verifica la conexión a PostgreSQL

---

## 📦 Variables de Entorno Necesarias

### **Backend (.env)**
```env
# Base de datos
DB_NAME=campito_db
DB_USER=campito_user
DB_PASSWORD=campito_pass

# Google OAuth2
GOOGLE_CLIENT_ID=tu_client_id_de_google
GOOGLE_CLIENT_SECRET=tu_client_secret_de_google

# Frontend URL (opcional, default: http://localhost:3000)
FRONTEND_URL=http://localhost:3000
```

### **Frontend (docker-compose.override.yml)**
```yaml
environment:
  - VITE_API_URL=http://localhost:8080
```

---

## 🎯 Próximos Pasos (Opcional)

1. **Refresh Token**: Implementar renovación automática de sesión
2. **Remember Me**: Opción de mantener sesión por más tiempo
3. **Roles y Permisos**: Sistema de autorización basado en roles
4. **Multi-proveedor**: Agregar Facebook, GitHub, etc.
5. **2FA**: Autenticación de dos factores

---

## 📝 Notas Técnicas

- **Sesiones**: Se usan cookies HTTP-only (más seguras que localStorage)
- **CSRF**: Deshabilitado (considera habilitarlo en producción)
- **SameSite**: En desarrollo es `Lax`, en producción debe ser `None` con HTTPS
- **HTTPS**: En producción, ambos servicios deben usar HTTPS

---

## ✅ Checklist de Producción

- [ ] Configurar HTTPS en frontend y backend
- [ ] Habilitar CSRF protection
- [ ] Configurar SameSite=None; Secure en cookies
- [ ] Usar variables de entorno para URLs (no hardcodear localhost)
- [ ] Implementar rate limiting en endpoints de autenticación
- [ ] Configurar logs apropiados (no exponer info sensible)
- [ ] Implementar refresh tokens
- [ ] Configurar timeout de sesión
- [ ] Implementar logout en todos los dispositivos
- [ ] Agregar monitoreo de intentos de login fallidos

---

**Documentación creada el:** 27 de Diciembre de 2025  
**Versión:** 1.0.0
