# 🐳 Guía Docker - Frontend

## 📋 Comandos Principales

### Desarrollo (con hot-reload)

```bash
# Levantar todos los servicios (incluido frontend)
docker-compose up -d

# Ver logs del frontend
docker-compose logs -f frontend

# Detener todos los servicios
docker-compose down

# Reconstruir el frontend
docker-compose up -d --build frontend
```

### Producción

```bash
# Levantar en modo producción
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Detener
docker-compose -f docker-compose.yml -f docker-compose.prod.yml down
```

## 🎯 Acceso a los Servicios

### Desarrollo
- **Frontend**: http://localhost:3000
- **Backend**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **pgAdmin**: http://localhost:5050

### Producción
- **Frontend**: http://localhost (puerto 80)
- **Backend**: http://localhost:8080

## 📁 Estructura de Archivos Docker

```
frontend/
├── Dockerfile          # Build de producción (multi-stage con Nginx)
├── Dockerfile.dev      # Build de desarrollo (con hot-reload)
├── nginx.conf          # Configuración Nginx para producción
└── .dockerignore       # Archivos a ignorar en el build
```

## 🔄 Modo Desarrollo vs Producción

### Desarrollo (Dockerfile.dev)
- ✅ Hot-reload activado (cambios en tiempo real)
- ✅ Volúmenes montados: `./frontend/src` → `/app/src`
- ✅ Servidor Vite en puerto 3000
- ✅ Variables de entorno de desarrollo

### Producción (Dockerfile)
- ✅ Build optimizado con Vite
- ✅ Servido con Nginx (alta performance)
- ✅ Assets comprimidos con Gzip
- ✅ Proxy configurado para API backend
- ✅ Cache de archivos estáticos

## 🛠️ Comandos Útiles

```bash
# Ver todos los contenedores
docker-compose ps

# Reiniciar solo el frontend
docker-compose restart frontend

# Ver logs de todos los servicios
docker-compose logs -f

# Eliminar volúmenes (limpiar datos)
docker-compose down -v

# Reconstruir todo desde cero
docker-compose up -d --build --force-recreate

# Ejecutar comandos dentro del contenedor
docker-compose exec frontend sh
docker-compose exec frontend npm install nueva-libreria
```

## 🐛 Troubleshooting

### El frontend no se actualiza en desarrollo
```bash
# Reconstruir el contenedor
docker-compose up -d --build frontend

# O limpiar todo
docker-compose down
docker-compose up -d --build
```

### Error de conexión con el backend
El frontend usa el proxy configurado en nginx.conf (producción) o vite.config.ts (desarrollo).

Verifica que el backend esté corriendo:
```bash
docker-compose logs backend
```

### Cambios en package.json no se reflejan
```bash
# Reconstruir sin cache
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

### Puerto 3000 ya está en uso
```bash
# Detener el proceso que usa el puerto
# Windows PowerShell:
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# O cambiar el puerto en docker-compose.override.yml:
ports:
  - "3001:3000"  # Usar puerto 3001 externamente
```

## 🔐 Variables de Entorno

### Desarrollo
Las variables se configuran en `docker-compose.override.yml`:
```yaml
environment:
  - VITE_API_URL=http://localhost:8080/api
```

### Producción
Las variables se deben configurar en tiempo de build. Edita `.env` antes de construir:
```bash
# .env
VITE_API_URL=https://api.tudominio.com/api
```

Luego construye:
```bash
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build frontend
```

## 📊 Comparación: Docker vs Local

| Característica | Docker | Local (npm) |
|---------------|--------|-------------|
| Setup inicial | Automático | Manual (npm install) |
| Aislamiento | ✅ Total | ❌ Usa Node local |
| Performance | 🐌 Ligeramente más lento | ⚡ Más rápido |
| Hot-reload | ✅ Funciona | ✅ Funciona |
| Consistencia | ✅ Mismo en todos los entornos | ❌ Depende de versiones locales |
| Producción | ✅ Exacto al deploy | ⚠️ Puede diferir |

## 🚀 Workflow Recomendado

### Para Desarrollo Diario
```bash
# 1. Levantar todos los servicios
docker-compose up -d

# 2. Ver logs si necesitas
docker-compose logs -f frontend

# 3. Trabajar normalmente (hot-reload activo)
# Los cambios en src/ se reflejan automáticamente

# 4. Al terminar
docker-compose down
```

### Para Testing Completo
```bash
# Probar como en producción
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
# Abrir http://localhost
```

## 📦 Comandos Rápidos

```bash
# Levantar
docker-compose up -d

# Bajar
docker-compose down

# Ver logs
docker-compose logs -f frontend

# Reconstruir
docker-compose up -d --build frontend

# Limpiar todo
docker-compose down -v
docker system prune -a
```

## ✅ Checklist

- [ ] El backend está corriendo
- [ ] El archivo `.env` existe en la raíz del proyecto
- [ ] Puerto 3000 está disponible (desarrollo)
- [ ] Puerto 80 está disponible (producción)
- [ ] Docker Desktop está corriendo

---

**¿Necesitas ayuda?** Revisa los logs: `docker-compose logs -f frontend`
