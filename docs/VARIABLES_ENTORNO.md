# 🚀 Variables de Entorno - Configuración Rápida

## 📋 Desarrollo Local con Docker

### 1. Backend
Crea `.env` en la raíz del proyecto:

```bash
# Database
DB_NAME=campito_db
DB_USER=campito_user
DB_PASSWORD=campito_pass

# PgAdmin
PGADMIN_EMAIL=admin@campito.com
PGADMIN_PASSWORD=admin

# Google OAuth2 (obtener en: https://console.cloud.google.com/apis/credentials)
GOOGLE_CLIENT_ID=tu-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-client-secret

# Frontend URL (debe coincidir con el puerto de docker-compose)
FRONTEND_URL=http://localhost:3100

# Spring Profile
SPRING_PROFILES_ACTIVE=dev
```

### 2. Frontend
Crea `frontend/.env.local`:

```bash
# URL del backend
VITE_API_URL=http://localhost:8080
```

### 3. Levantar servicios
```bash
docker-compose up --build
```

**URLs:**
- Frontend: http://localhost:3100
- Backend API: http://localhost:8080/api
- PgAdmin: http://localhost:5050

---

## 🌐 Producción (Servicios Distribuidos)

### Backend (Google Cloud Run / Railway)
```bash
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://host.neon.tech:5432/db?sslmode=require
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

## 📚 Ver Más

- **Guía completa de despliegue:** [docs/GUIA_DESPLIEGUE_PRODUCCION.md](GUIA_DESPLIEGUE_PRODUCCION.md)
- **Resumen de cambios:** [docs/CAMBIOS_PRODUCCION.md](CAMBIOS_PRODUCCION.md)
- **Ejemplos de variables:** [backend/.env.example](../backend/.env.example) y [frontend/.env.example](../frontend/.env.example)
