# Guía de Docker para Desarrollo

Esta guía describe el flujo de trabajo para trabajar con Docker en el proyecto usando el perfil `dev`.

## Requisitos Previos

- Docker y Docker Compose instalados
- Archivo `.env` configurado en la raíz del proyecto con las siguientes variables:
  ```env
  DB_NAME=campito_db
  DB_USER=postgres
  DB_PASSWORD=tu_password
  PGADMIN_EMAIL=admin@campito.com
  PGADMIN_PASSWORD=admin
  GOOGLE_CLIENT_ID=tu_client_id
  GOOGLE_CLIENT_SECRET=tu_client_secret
  SPRING_PROFILES_ACTIVE=dev
  ```

## Primera Vez: Levantar los Servicios

```bash
# 1. Construir las imágenes y levantar los servicios
docker-compose up -d --build

# 2. Verificar que los servicios estén corriendo
docker-compose ps
```

### Servicios Disponibles

- **Backend (Spring Boot)**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **pgAdmin**: http://localhost:5050
- **Swagger**: http://localhost:8080/swagger-ui/index.html

## Flujo de Trabajo: Modificar el Backend

Cuando realizas cambios en el código del backend:

```bash
# 1. Detener el contenedor del backend
docker-compose stop backend

# 2. Reconstruir la imagen del backend
docker-compose build backend

# 3. Levantar el contenedor actualizado
docker-compose up -d backend

# 4. Ver los logs del backend (opcional)
docker-compose logs -f backend
```

### Alternativa Rápida (Un Solo Comando)

```bash
docker-compose up -d --build backend
```

## Comandos Útiles

### Ver Logs de los Servicios

```bash
# Todos los servicios
docker-compose logs -f

# Solo backend
docker-compose logs -f backend

# Solo base de datos
docker-compose logs -f db
```

### Detener los Servicios

```bash
# Detener todos los servicios
docker-compose stop

# Detener un servicio específico
docker-compose stop backend
```

### Reiniciar un Servicio

```bash
docker-compose restart backend
```

## Eliminar Volúmenes (Limpiar Base de Datos)

⚠️ **ADVERTENCIA**: Esto eliminará TODOS los datos de la base de datos y configuraciones de pgAdmin.

```bash
# 1. Detener y eliminar todos los contenedores
docker-compose down

# 2. Eliminar los volúmenes
docker-compose down -v

# 3. Levantar los servicios desde cero
docker-compose up -d --build
```

### Eliminar Solo Volúmenes Específicos

```bash
# Ver los volúmenes existentes
docker volume ls

# Eliminar volumen de PostgreSQL
docker volume rm proyectocampo_postgres_data

# Eliminar volumen de pgAdmin
docker volume rm proyectocampo_pgadmin_data
```

## Borrar Todo y Empezar de Cero

Si necesitas limpiar completamente el entorno:

```bash
# 1. Detener y eliminar contenedores, redes y volúmenes
docker-compose down -v

# 2. Eliminar imágenes construidas (opcional)
docker rmi campito/backend

# 3. Reconstruir y levantar
docker-compose up -d --build
```

## Solución de Problemas

### El Backend no se conecta a la Base de Datos

```bash
# Verificar que la BD esté lista
docker-compose logs db

# Reiniciar el backend
docker-compose restart backend
```

### Puerto ya en uso

```bash
# Ver qué proceso usa el puerto 8080
netstat -ano | findstr :8080

# Cambiar el puerto en docker-compose.override.yml o detener el proceso
```

### Limpiar Contenedores Huérfanos

```bash
docker-compose down --remove-orphans
```

## Notas Importantes

- Los cambios en el código requieren reconstruir la imagen del backend
- La base de datos persiste entre reinicios gracias a los volúmenes
- El perfil `dev` se activa automáticamente según `docker-compose.override.yml`
- Las migraciones de Flyway se ejecutan automáticamente al iniciar el backend
