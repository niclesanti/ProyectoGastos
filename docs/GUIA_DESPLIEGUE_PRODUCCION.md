# 🚀 Guía de Despliegue en Producción - Servicios Distribuidos

Esta guía explica cómo desplegar tu aplicación **ProyectoGastos** en producción con servicios distribuidos (base de datos, backend y frontend en diferentes proveedores).

---

## 📊 Arquitectura de Producción

```
┌─────────────────┐
│   FRONTEND      │  ← Vercel / Netlify / Cloudflare Pages
│   (React/Vite)  │     https://tu-app.vercel.app
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│    BACKEND      │  ← Google Cloud Run / Railway / Render
│  (Spring Boot)  │     https://api-tuapp.run.app
└────────┬────────┘
         │ HTTPS + SSL
         ▼
┌─────────────────┐
│   DATABASE      │  ← Neon / Supabase / Railway
│  (PostgreSQL)   │     postgres://xxx.neon.tech:5432/db
└─────────────────┘
```

---

## 🗄️ PASO 1: Desplegar la Base de Datos

### Opción A: Neon (Recomendado - Free Tier Generoso)

1. **Crear cuenta en** [Neon](https://neon.tech)
2. **Crear nuevo proyecto PostgreSQL**
3. **Copiar la cadena de conexión:**
   ```
   postgres://user:password@ep-xxx.neon.tech/neondb?sslmode=require
   ```
4. **Guardar estas variables:**
   ```bash
   SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.neon.tech:5432/neondb?sslmode=require
   SPRING_DATASOURCE_USERNAME=user
   SPRING_DATASOURCE_PASSWORD=password
   ```

### Opción B: Supabase

1. **Crear proyecto en** [Supabase](https://supabase.com)
2. **Ir a Settings → Database**
3. **Copiar "Connection string" (JDBC format)**

### ✅ Verificación
- La base de datos debe permitir conexiones SSL
- Flyway ejecutará las migraciones automáticamente en el primer despliegue del backend

---

## 🖥️ PASO 2: Desplegar el Backend

### Opción A: Google Cloud Run (Recomendado)

#### 2.1 Preparar el proyecto
```bash
# Instalar gcloud CLI
# https://cloud.google.com/sdk/docs/install

# Autenticarse
gcloud auth login

# Crear proyecto (si no existe)
gcloud projects create proyecto-gastos --name="Proyecto Gastos"
gcloud config set project proyecto-gastos

# Habilitar APIs necesarias
gcloud services enable run.googleapis.com
gcloud services enable cloudbuild.googleapis.com
```

#### 2.2 Configurar variables de entorno
Crea un archivo `backend/.env.prod` (NO subir a Git):
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx.neon.tech:5432/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=tu_usuario
SPRING_DATASOURCE_PASSWORD=tu_password
GOOGLE_CLIENT_ID=tu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-client-secret
FRONTEND_URL=https://tu-app.vercel.app
PORT=8080
```

#### 2.3 Desplegar a Cloud Run
```bash
cd backend

# Build y deploy (primera vez)
gcloud run deploy proyecto-gastos-backend \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8080 \
  --env-vars-file .env.prod

# Obtener la URL del backend
gcloud run services describe proyecto-gastos-backend \
  --region us-central1 \
  --format 'value(status.url)'
```

**Resultado:** Recibirás una URL como `https://proyecto-gastos-backend-xxx.run.app`

---

### Opción B: Railway

1. **Conectar repositorio en** [Railway](https://railway.app)
2. **Seleccionar el directorio `/backend`**
3. **Configurar variables de entorno** en el dashboard:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `SPRING_DATASOURCE_URL=...`
   - `SPRING_DATASOURCE_USERNAME=...`
   - `SPRING_DATASOURCE_PASSWORD=...`
   - `GOOGLE_CLIENT_ID=...`
   - `GOOGLE_CLIENT_SECRET=...`
   - `FRONTEND_URL=https://tu-app.vercel.app` (actualizar después)
4. **Deploy automático**

---

## 🎨 PASO 3: Configurar Google OAuth2

1. **Ir a** [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
2. **Editar tu OAuth 2.0 Client**
3. **Agregar Authorized redirect URIs:**
   ```
   # Backend en producción
   https://tu-backend.run.app/login/oauth2/code/google
   
   # Localhost para desarrollo
   http://localhost:8080/login/oauth2/code/google
   ```
4. **Agregar Authorized JavaScript origins:**
   ```
   # Frontend en producción
   https://tu-app.vercel.app
   
   # Localhost para desarrollo
   http://localhost:3000
   http://localhost:5173
   ```

---

## 🌐 PASO 4: Desplegar el Frontend

### Opción A: Vercel (Recomendado para Vite/React)

#### 4.1 Preparar el repositorio
```bash
# Asegurarse de tener un archivo .env.production
cd frontend
cat > .env.production << EOF
VITE_API_URL=https://tu-backend.run.app
EOF
```

#### 4.2 Desplegar con Vercel CLI
```bash
# Instalar Vercel CLI
npm install -g vercel

# Login
vercel login

# Deploy
cd frontend
vercel

# Seguir el wizard:
# - Set up and deploy? Yes
# - Which scope? Tu cuenta
# - Link to existing project? No
# - Project name? proyecto-gastos
# - Directory? ./
# - Override settings? No
```

#### 4.3 Configurar variables de entorno en Vercel

**IMPORTANTE:** Las variables de entorno en Vercel se configuran desde el dashboard:

1. Ir a tu proyecto en [Vercel Dashboard](https://vercel.com/dashboard)
2. **Settings → Environment Variables**
3. Agregar:
   ```
   Name: VITE_API_URL
   Value: https://tu-backend.run.app
   Environments: Production ✅
   ```
4. **Redeploy** para que tome la nueva variable:
   ```bash
   vercel --prod
   ```

---

### Opción B: Netlify

1. **Conectar repositorio en** [Netlify](https://netlify.com)
2. **Configurar build settings:**
   - Base directory: `frontend`
   - Build command: `npm run build`
   - Publish directory: `frontend/dist`
3. **Environment variables:**
   ```
   VITE_API_URL=https://tu-backend.run.app
   ```
4. **Deploy**

---

## 🔄 PASO 5: Actualizar URLs Cruzadas

Una vez desplegados todos los servicios:

### 5.1 Actualizar Backend con URL del Frontend
```bash
# En Google Cloud Run
gcloud run services update proyecto-gastos-backend \
  --region us-central1 \
  --update-env-vars FRONTEND_URL=https://tu-app.vercel.app
```

### 5.2 Actualizar Frontend con URL del Backend
En Vercel Dashboard:
- Settings → Environment Variables
- Actualizar `VITE_API_URL` con la URL correcta del backend
- **Trigger redeploy**

---

## ✅ PASO 6: Verificación del Despliegue

### 6.1 Verificar Backend
```bash
# Health check
curl https://tu-backend.run.app/actuator/health

# Auth status (debería retornar 401 o datos de usuario)
curl https://tu-backend.run.app/api/auth/status
```

### 6.2 Verificar Frontend
1. Abrir `https://tu-app.vercel.app`
2. Abrir DevTools → Network
3. Hacer login con Google
4. Verificar que las llamadas API apunten a `https://tu-backend.run.app`

### 6.3 Verificar CORS
En la consola del navegador **NO** debe aparecer:
```
Access to XMLHttpRequest at 'https://tu-backend.run.app/api/...' 
from origin 'https://tu-app.vercel.app' has been blocked by CORS policy
```

Si aparece este error:
1. Verificar que `FRONTEND_URL` esté configurado en el backend
2. Verificar que no haya espacios o caracteres extra
3. Redeploy del backend

---

## 🔐 Variables de Entorno - Resumen

### Backend (Google Cloud Run / Railway)
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://xxx.neon.tech:5432/db?sslmode=require
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=contraseña
GOOGLE_CLIENT_ID=xxx.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=xxx
FRONTEND_URL=https://tu-app.vercel.app
PORT=8080
```

### Frontend (Vercel / Netlify)
```bash
VITE_API_URL=https://tu-backend.run.app
```

---

## 🐛 Troubleshooting

### Error: "CORS policy blocking requests"
**Causa:** `FRONTEND_URL` no está configurado o es incorrecto en el backend

**Solución:**
```bash
# Verificar variable en Cloud Run
gcloud run services describe proyecto-gastos-backend \
  --region us-central1 \
  --format 'value(spec.template.spec.containers[0].env)'

# Actualizar si es necesario
gcloud run services update proyecto-gastos-backend \
  --region us-central1 \
  --update-env-vars FRONTEND_URL=https://tu-app-correcta.vercel.app
```

### Error: "OAuth2 redirect not working"
**Causa:** Redirect URI no configurado en Google Cloud Console

**Solución:**
1. Ir a Google Cloud Console → Credentials
2. Editar OAuth 2.0 Client
3. Agregar: `https://tu-backend.run.app/login/oauth2/code/google`

### Error: "Database connection timeout"
**Causa:** URL de base de datos incorrecta o falta `sslmode=require`

**Solución:**
```bash
# Formato correcto para Neon/Supabase
jdbc:postgresql://host:5432/database?sslmode=require
```

### Frontend muestra "localhost:8080" en producción
**Causa:** Variable `VITE_API_URL` no configurada o no se rebuildeó

**Solución:**
1. Verificar variable en Vercel Dashboard
2. Trigger **redeploy** (las variables de Vite se inyectan en build time)

---

## 🔄 Actualizaciones Futuras

### Backend
```bash
# Commit cambios
git add .
git commit -m "fix: actualización backend"

# Redeploy
gcloud run deploy proyecto-gastos-backend \
  --source ./backend \
  --region us-central1
```

### Frontend
```bash
# Vercel hace deploy automático al hacer push a main
git push origin main

# O manualmente
cd frontend
vercel --prod
```

---

## 💡 Mejores Prácticas

1. **Nunca commitear archivos `.env` reales** - solo `.env.example`
2. **Usar secretos del proveedor** (Secret Manager en GCP, Railway Secrets)
3. **Monitorear logs:**
   ```bash
   # Cloud Run
   gcloud run services logs read proyecto-gastos-backend --region us-central1
   
   # Vercel
   vercel logs
   ```
4. **Backup de base de datos** (Neon hace backups automáticos)
5. **Custom domains** (opcional):
   - Backend: `api.tudominio.com`
   - Frontend: `tudominio.com`

---

## 📞 Soporte

Si tienes problemas:
1. Revisar logs del backend: `gcloud run services logs read ...`
2. Revisar console del navegador (DevTools → Network)
3. Verificar que todas las variables de entorno estén configuradas
4. Comprobar que OAuth2 redirect URIs estén bien configurados

---

## ✅ Checklist de Despliegue

- [ ] Base de datos creada en Neon/Supabase
- [ ] Backend desplegado en Google Cloud Run/Railway
- [ ] Variables de entorno configuradas en backend
- [ ] OAuth2 redirect URIs actualizados en Google Console
- [ ] Frontend desplegado en Vercel/Netlify
- [ ] Variable `VITE_API_URL` configurada en frontend
- [ ] Variable `FRONTEND_URL` actualizada en backend con URL final
- [ ] Redeploy del frontend después de configurar variables
- [ ] Login con Google funciona correctamente
- [ ] API calls funcionan sin errores CORS
- [ ] Datos se persisten en la base de datos

---

¡Listo! Tu aplicación está desplegada profesionalmente en producción. 🎉
