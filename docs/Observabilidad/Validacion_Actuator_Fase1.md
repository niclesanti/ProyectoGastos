# ✅ Validación de Spring Boot Actuator - Fase 1

## 📋 Resumen de Cambios Implementados

### 1. Dependencias Agregadas (pom.xml)
- ✅ `spring-boot-starter-actuator`: Framework de observabilidad
- ✅ `micrometer-registry-prometheus`: Exportación de métricas en formato Prometheus

### 2. Configuración por Perfil

#### Desarrollo (application-dev.properties)
- **Endpoints expuestos**: TODOS (`*`) para facilitar testing
- **Puerto**: 8080 (mismo que la aplicación)
- **Seguridad**: Endpoints públicos (sin autenticación) para desarrollo rápido
- **Acceso**: `http://localhost:8080/actuator`

#### Producción (application-prod.properties)
- **Endpoints expuestos**: Solo esenciales (`health`, `metrics`, `prometheus`, `info`)
- **Puerto**: 9090 (separado del puerto de aplicación 8080)
- **Seguridad**: Puerto NO expuesto públicamente (solo interno al contenedor/red)
- **Acceso**: `http://localhost:9090/actuator` (solo desde dentro del servidor)

### 3. Seguridad (SecurityConfig.java)
- ✅ En **desarrollo**: `/actuator/**` es público (sin JWT)
- ✅ En **producción**: Actuator corre en puerto separado no expuesto al exterior
- ✅ Estrategia de defensa en profundidad

---

## 🧪 Guía de Validación Local (Desarrollo)

### Paso 1: Compilar y Levantar la Aplicación

```powershell
# Desde la raíz del proyecto backend
cd backend

# Compilar (esto genera las clases de MapStruct y procesa las properties)
mvn clean compile

# Ejecutar en modo desarrollo
mvn spring-boot:run
```

Deberías ver en los logs:
```
Exposing 21 endpoint(s) beneath base path '/actuator'
Started BackendApplication in X.XXX seconds
```

---

### Paso 2: Validar Endpoint de Salud (Health)

**Propósito**: Verificar que la aplicación está corriendo y todas sus dependencias están operativas.

```powershell
# PowerShell
Invoke-RestMethod -Uri http://localhost:8080/actuator/health | ConvertTo-Json -Depth 5
```

**Respuesta esperada**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

✅ **Validación exitosa si**: `status: "UP"` y el componente `db` está `UP`.

---

### Paso 3: Listar Todos los Endpoints Disponibles

```powershell
Invoke-RestMethod -Uri http://localhost:8080/actuator
```

**Respuesta esperada** (endpoints clave):
```json
{
  "_links": {
    "self": { "href": "http://localhost:8080/actuator" },
    "health": { "href": "http://localhost:8080/actuator/health" },
    "info": { "href": "http://localhost:8080/actuator/info" },
    "metrics": { "href": "http://localhost:8080/actuator/metrics" },
    "prometheus": { "href": "http://localhost:8080/actuator/prometheus" },
    "env": { "href": "http://localhost:8080/actuator/env" },
    "loggers": { "href": "http://localhost:8080/actuator/loggers" },
    "heapdump": { "href": "http://localhost:8080/actuator/heapdump" }
  }
}
```

✅ **Validación exitosa si**: Ves `health`, `metrics` y `prometheus` en la lista.

---

### Paso 4: Verificar Métricas de Memoria JVM

**Propósito**: Ver cuánta RAM está usando tu aplicación.

```powershell
# Ver todas las métricas disponibles
Invoke-RestMethod -Uri http://localhost:8080/actuator/metrics

# Ver métricas específicas de memoria Heap
Invoke-RestMethod -Uri http://localhost:8080/actuator/metrics/jvm.memory.used | ConvertTo-Json -Depth 3
```

**Respuesta esperada**:
```json
{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 268435456.0  // ~256 MB en bytes
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    }
  ]
}
```

**Para ver solo el Heap (memoria principal)**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap"
```

✅ **Validación exitosa si**: Ves un valor en bytes (ej: 268435456 = 256 MB).

---

### Paso 5: Verificar Métricas de Performance HTTP

```powershell
# Tiempo de respuesta de peticiones HTTP (percentil 95)
Invoke-RestMethod -Uri http://localhost:8080/actuator/metrics/http.server.requests | ConvertTo-Json -Depth 3
```

**Respuesta esperada**:
```json
{
  "name": "http.server.requests",
  "description": "Duration of HTTP server request handling",
  "baseUnit": "seconds",
  "measurements": [
    {
      "statistic": "COUNT",
      "value": 42.0  // Total de peticiones procesadas
    },
    {
      "statistic": "TOTAL_TIME",
      "value": 3.5  // Tiempo total acumulado
    },
    {
      "statistic": "MAX",
      "value": 0.523  // Petición más lenta (523ms)
    }
  ]
}
```

✅ **Validación exitosa si**: Ves estadísticas COUNT, TOTAL_TIME y MAX.

---

### Paso 6: Verificar Endpoint Prometheus (Formato para Grafana)

**Propósito**: Este es el endpoint que Grafana Agent "scrapeará" cada 60 segundos.

```powershell
# Ver las primeras 50 líneas del formato Prometheus
(Invoke-RestMethod -Uri http://localhost:8080/actuator/prometheus) -split "`n" | Select-Object -First 50
```

**Respuesta esperada** (extracto):
```
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",application="backend",} 1.34217728E8
jvm_memory_used_bytes{area="heap",id="G1 Old Gen",application="backend",} 5.24288E7

# HELP jvm_memory_max_bytes The maximum amount of memory in bytes that can be used for memory management
# TYPE jvm_memory_max_bytes gauge
jvm_memory_max_bytes{area="heap",id="G1 Eden Space",application="backend",} -1.0
jvm_memory_max_bytes{area="heap",id="G1 Old Gen",application="backend",} 5.36870912E8

# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/dashboard/balance-total",application="backend",} 5.0
http_server_requests_seconds_sum{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/api/dashboard/balance-total",application="backend",} 0.245
```

✅ **Validación exitosa si**: Ves métricas en formato texto plano con prefijos `jvm_`, `http_`, `system_`.

---

### Paso 7: Verificar Información de la Aplicación

```powershell
Invoke-RestMethod -Uri http://localhost:8080/actuator/info | ConvertTo-Json
```

**Respuesta esperada**:
```json
{
  "app": {
    "name": "backend",
    "description": "Backend del Sistema de Gestion de Producción Ganadera",
    "version": "0.0.1-SNAPSHOT"
  },
  "java": {
    "version": "21.0.x",
    "vendor": {
      "name": "Oracle Corporation"
    }
  },
  "os": {
    "name": "Windows 11",
    "version": "10.0"
  }
}
```

✅ **Validación exitosa si**: Ves información de Java 21 y el sistema operativo.

---

## 🔐 Validación de Seguridad

### Test 1: Verificar Que Actuator es Público en Dev

```powershell
# Este comando NO debe pedir autenticación
Invoke-RestMethod -Uri http://localhost:8080/actuator/health
```

✅ **Esperado**: Respuesta inmediata sin error 401 (Unauthorized).

---

### Test 2: Verificar Que Endpoints Protegidos Requieren JWT

```powershell
# Intentar acceder a un endpoint de API sin token
try {
    Invoke-RestMethod -Uri http://localhost:8080/api/transacciones
} catch {
    $_.Exception.Response.StatusCode.value__
}
```

✅ **Esperado**: Código de error 401 o 403 (requiere autenticación).

---

## 📊 Métricas Clave para Monitorear

Una vez validado que todo funciona, estas son las métricas más importantes:

| Métrica | Endpoint | Qué Indica |
|---------|----------|------------|
| **JVM Heap Used** | `/actuator/metrics/jvm.memory.used?tag=area:heap` | RAM usada (crítico con 1GB límite) |
| **JVM Heap Max** | `/actuator/metrics/jvm.memory.max?tag=area:heap` | Límite configurado (-Xmx512m) |
| **System CPU** | `/actuator/metrics/system.cpu.usage` | Carga del procesador |
| **HTTP Requests** | `/actuator/metrics/http.server.requests` | Peticiones por segundo |
| **HikariCP Connections** | `/actuator/metrics/hikaricp.connections.active` | Conexiones a PostgreSQL activas |
| **HikariCP Pending** | `/actuator/metrics/hikaricp.connections.pending` | Peticiones esperando conexión (bottleneck) |

---

## 🐛 Troubleshooting

### Problema 1: Error 404 en /actuator

**Síntoma**:
```
404 Not Found - /actuator/health
```

**Causas posibles**:
1. La dependencia `spring-boot-starter-actuator` no se agregó correctamente.
2. Maven no recompiló el proyecto.

**Solución**:
```powershell
mvn clean install -DskipTests
mvn spring-boot:run
```

---

### Problema 2: Health Status "DOWN"

**Síntoma**:
```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "org.postgresql.util.PSQLException: Connection refused"
      }
    }
  }
}
```

**Causa**: PostgreSQL no está corriendo o la conexión falló.

**Solución**:
```powershell
# Verificar que el contenedor de DB está corriendo
docker ps | Select-String postgres

# Si no está corriendo
docker-compose up -d db
```

---

### Problema 3: Actuator Requiere Autenticación en Dev

**Síntoma**:
```
401 Unauthorized - /actuator/health
```

**Causa**: El perfil activo no es `dev` o SecurityConfig no se actualizó.

**Solución**:
```powershell
# Verificar perfil activo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# O en application.properties validar:
# spring.profiles.active=dev
```

---

## 📝 Checklist de Validación

- [ ] ✅ Dependencias agregadas en pom.xml
- [ ] ✅ Configuración en application.properties
- [ ] ✅ Configuración específica en application-dev.properties
- [ ] ✅ Configuración específica en application-prod.properties
- [ ] ✅ Seguridad configurada en SecurityConfig.java
- [ ] ✅ Aplicación arranca sin errores
- [ ] ✅ `/actuator` devuelve lista de endpoints
- [ ] ✅ `/actuator/health` devuelve `status: UP`
- [ ] ✅ `/actuator/metrics` devuelve lista de métricas
- [ ] ✅ `/actuator/metrics/jvm.memory.used` devuelve valor en bytes
- [ ] ✅ `/actuator/prometheus` devuelve métricas en formato texto
- [ ] ✅ Actuator NO requiere autenticación en dev
- [ ] ✅ Endpoints de API SÍ requieren JWT

---

## 🎯 Próximos Pasos (Fase 2)

Una vez validado que Actuator funciona localmente:

1. ✅ **Deploy a Producción**: Subir la imagen Docker actualizada
2. ⏭️ **Configurar Grafana Cloud**: Crear cuenta y stack
3. ⏭️ **Instalar Grafana Agent**: En servidor Oracle Cloud
4. ⏭️ **Validar Recolección**: Ver métricas en Grafana Explore

---

**Autor**: Sistema de Observabilidad ProyectoGastos  
**Fecha**: Febrero 2026  
**Versión**: 1.0 - Fase 1 Completada
