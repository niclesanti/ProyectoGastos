# 📊 Observabilidad y Métricas en ProyectoGastos

## 🎯 ¿Qué es Observabilidad y Por Qué Importa?

### Definición Simple

**Observabilidad** es la capacidad de entender qué está pasando dentro de tu aplicación sin necesidad de conectarte por SSH o revisar logs manualmente. Es como tener un "tablero de instrumentos" de un auto: ves la velocidad, temperatura del motor, nivel de gasolina, etc., todo en tiempo real.

En tu caso específico, con recursos limitados (1GB RAM) y una aplicación financiera crítica, la observabilidad no es un "lujo" sino una **necesidad operativa**.

### La Analogía del Piloto

Imagina que vuelas un avión (tu aplicación Spring Boot) con recursos limitados de combustible (1GB RAM). ¿Prefieres:

- **Opción A:** Volar "a ciegas" y darte cuenta de que te quedaste sin combustible cuando el motor se apaga (error OOM en producción).
- **Opción B:** Tener un panel de instrumentos que te muestre en tiempo real el consumo, te alerte cuando estés cerca del límite y te permita aterrizar de forma controlada.

La observabilidad es la **Opción B**.

---

## 🔥 ¿Por Qué es CRÍTICO en Tu Proyecto?

### 1. **Recursos Extremadamente Limitados**

Tu servidor de producción tiene:
- **1 GB de RAM total** (compartida entre sistema operativo, JVM, Docker, etc.)
- **2 GB de SWAP** (memoria virtual lenta que salva de crashes pero degrada performance)
- **1 vCPU** (procesador compartido)

**Problema Real:** Spring Boot 3.5.3 con Java 21 consume entre 350-600 MB de RAM solo para arrancar. Si tu aplicación tiene un pico de usuarios (aunque sean solo 3-4 clientes), **podrías saturar la memoria y que el sistema mate tu proceso** (*OOM Killer*).

**Cómo te salva la observabilidad:**
- Ves en tiempo real cuánta RAM está usando la JVM.
- Detectas cuando empieza a usar SWAP (señal de peligro).
- Identificas qué endpoint o funcionalidad consume más memoria.
- Puedes ajustar el límite de memoria de la JVM antes de que colapse (`-Xmx512m`).

### 2. **Aplicación Financiera = Zero Margen de Error**

Tu proyecto gestiona:
- Transacciones de dinero real
- Resúmenes de tarjetas de crédito
- Compras en cuotas
- Notificaciones en tiempo real (SSE)

**Problema Real:** Si el scheduler de `ResumenScheduler` falla por falta de memoria a las 2 AM, los resúmenes mensuales de tus clientes no se generarán. ¿Te enterarás al día siguiente cuando un cliente te reclame? ¿O tendrás una alerta automática?

**Cómo te salva la observabilidad:**
- Alertas cuando los schedulers fallan.
- Métricas de tiempo de respuesta de transacciones financieras.
- Monitoreo de la conexión a PostgreSQL (Aiven).
- Trazabilidad de errores sin revisar logs de 10.000 líneas.

### 3. **Preparación para Escalar a 3-4 Clientes**

**Problema Real:** Con 1 cliente (tú), la aplicación funciona bien. Pero cuando llegues a 3-4 clientes simultáneos:
- ¿Cuántas peticiones por segundo soporta tu servidor?
- ¿Qué endpoint es el más lento y necesita optimización?
- ¿La consulta del Dashboard está saturando PostgreSQL?

**Cómo te salva la observabilidad:**
- Identificas cuellos de botella ANTES de que los usuarios se quejen.
- Tomas decisiones basadas en datos: "El endpoint `/api/dashboard/gastos-ingresos-mensuales` tarda 3 segundos, necesito optimizar la query".
- Demuestras profesionalismo: tus clientes ven que monitorizas proactivamente.

### 4. **Valor Curricular (Aspecto Profesional)**

**Realidad del Mercado:** 
- Un desarrollador que muestra "mi proyecto funciona" → **Interesante**.
- Un desarrollador que muestra "mi proyecto funciona + dashboard de métricas en Grafana + alertas configuradas" → **Contratado**.

La observabilidad es una de las **competencias más valoradas** en entrevistas para roles Backend/DevOps/SRE porque demuestra:
- Mentalidad de producción (no solo desarrollo).
- Conocimiento de herramientas estándar del industry (Prometheus, Grafana).
- Capacidad de diagnosticar problemas sin ayuda.

---

## 📈 Métricas Específicas para Tu Proyecto

### Categoría 1: **Salud del Sistema (Crítico)**

Estas métricas te salvan de crashes y caídas del servicio.

| Métrica | Descripción | Valor de Alerta | Por Qué Importa |
|---------|-------------|-----------------|-----------------|
| **JVM Heap Memory Used** | RAM usada por la JVM | > 85% del límite | Riesgo de OOM inminente |
| **JVM Heap Memory Max** | Límite máximo configurado | N/A | Validar que `-Xmx512m` está activo |
| **System CPU Usage** | Uso del procesador | > 80% por 5 min | Tu único vCPU está saturado |
| **SWAP Usage** | Memoria virtual en uso | > 500 MB | Performance degradada severamente |
| **Application Uptime** | Tiempo desde el último reinicio | N/A | Detectar reinicios inesperados |
| **HTTP Server Connections** | Conexiones HTTP activas | > 50 simultáneas | Límite de tu servidor pequeño |

**Acción que tomas:** Si ves que la JVM usa 450 MB de 512 MB constantemente, sabes que debes optimizar código o aumentar SWAP.

---

### Categoría 2: **Performance de la API (Importante)**

Estas métricas te ayudan a optimizar la experiencia del usuario.

| Métrica | Descripción | Valor de Alerta | Por Qué Importa |
|---------|-------------|-----------------|-----------------|
| **HTTP Request Duration** (P95) | Tiempo de respuesta percentil 95 | > 2 segundos | Los usuarios notan lentitud |
| **HTTP Request Rate** | Peticiones por segundo | N/A | Saber tu capacidad máxima |
| **Endpoint Specific Duration** | Tiempo por endpoint específico | > 3 segundos | Identificar endpoints lentos |
| **HTTP Errors (5xx)** | Errores del servidor | > 0 | Algo falló en el backend |
| **HTTP Errors (4xx)** | Errores del cliente | > 10% peticiones | Problemas de validación o auth |

**Ejemplo Real:** 
- Descubres que `GET /api/dashboard/gastos-ingresos-mensuales` tarda 4 segundos.
- Revisas la query y notas que falta un índice en la tabla `GastosIngresosMensuales`.
- Añades el índice en una migración Flyway `V15__...sql`.
- El endpoint ahora responde en 300ms.

---

### Categoría 3: **Base de Datos (PostgreSQL en Aiven)**

| Métrica | Descripción | Valor de Alerta | Por Qué Importa |
|---------|-------------|-----------------|-----------------|
| **HikariCP Connections Active** | Conexiones activas al pool | > 8 de 10 | Pool de conexiones saturado |
| **HikariCP Connections Pending** | Peticiones esperando conexión | > 0 | Bottleneck en DB |
| **Query Execution Time** | Tiempo promedio de queries | > 500ms | Queries no optimizadas |
| **Connection Errors** | Fallos al conectar a Aiven | > 0 | Problema de red o SSL |

**Acción que tomas:** Si ves 9 conexiones activas constantemente, aumentas el pool de HikariCP en `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=15
```

---

### Categoría 4: **Lógica de Negocio (Específica de Tu App)**

Estas métricas te permiten entender el uso real de tu aplicación.

| Métrica | Descripción | Por Qué Importa |
|---------|-------------|-----------------|
| **Transacciones Creadas** (contador) | Total de transacciones registradas | Entiendes el volumen de uso |
| **Compras a Crédito Activas** | Cuotas pendientes de pago | Detectas si la funcionalidad se usa |
| **Resúmenes Generados** | Resúmenes cerrados por el scheduler | Validas que el scheduler funciona |
| **Notificaciones Enviadas (SSE)** | Notificaciones push enviadas | Monitoreas el sistema de eventos |
| **Usuarios Autenticados** (gauge) | Usuarios con sesión activa | Detectas picos de uso concurrente |
| **Espacios de Trabajo Activos** | Total de espacios con actividad | Métrica de adopción |

**Ejemplo Real:** 
- Notas que "Resúmenes Generados" se quedó en 0 el día de cierre.
- Revisas los logs y descubres que el scheduler falló por un error de validación.
- Corriges el bug antes de que el cliente lo note.

---

## 🛠 Plan de Implementación (Paso a Paso)

### **Fase 0: Pre-requisitos**

**Objetivo:** Entender el estado actual sin herramientas adicionales.

**Tareas:**
1. **Documentar recursos actuales:**
   - SSH al servidor Oracle Cloud.
   - Ejecutar `free -h` (ver RAM y SWAP usado).
   - Ejecutar `docker stats` (ver consumo del contenedor Spring Boot).
   - Anotar valores promedio: ¿cuánta RAM usa tu app en estado idle?

2. **Revisar logs actual:**
   - `docker logs springboot-campito-prod --tail 100`
   - Identificar mensajes de error recurrentes.

**Duración:** 30 minutos.  
**Costo:** $0 (solo revisión).

---

### **Fase 1: Spring Boot Actuator (Lo Mínimo Indispensable)**

**Objetivo:** Exponer métricas básicas de tu aplicación SIN instalar herramientas externas.

#### Paso 1.1: Agregar Dependencia

Edita `backend/pom.xml` y agrega:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

#### Paso 1.2: Configurar Endpoints

Edita `backend/src/main/resources/application.properties`:

```properties
# Actuator Endpoints
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

#### Paso 1.3: Desplegar y Validar

```bash
# Local (desarrollo)
mvn spring-boot:run

# Producción
docker build -t tu_usuario/proyecto-gastos-backend:latest ./backend
docker push tu_usuario/proyecto-gastos-backend:latest

# En servidor Oracle
docker compose pull backend && docker compose up -d
```

**Validación:**
```bash
# Verificar que funciona
curl http://localhost:8080/actuator/health
# Respuesta esperada: {"status":"UP"}

curl http://localhost:8080/actuator/metrics
# Verás lista de métricas disponibles

curl http://localhost:8080/actuator/metrics/jvm.memory.used
# Verás RAM usada por la JVM
```

#### Paso 1.4: Proteger Endpoints

**CRÍTICO:** Los endpoints de Actuator exponen información sensible. Debes protegerlos.

Edita `backend/src/main/java/com/campito/backend/config/SecurityConfig.java`:

```java
// Dentro del método SecurityFilterChain
.requestMatchers("/actuator/**").hasRole("ADMIN") // Solo admins ven métricas
```

O mejor aún, usa un puerto diferente para management (recomendado):

```properties
# application.properties
management.server.port=9090
management.endpoints.web.base-path=/actuator
```

Y en `compose.yaml` NO expongas el puerto 9090 públicamente (solo interno).

**Duración:** 1-2 horas (incluyendo deploy y validación).  
**Costo:** $0.  
**Beneficio:** Ya tienes métricas básicas accesibles vía HTTP.

---

### **Fase 2: Prometheus (Recolección de Métricas)**

**Objetivo:** Guardar las métricas en una base de datos de series temporales.

#### Opción A: Prometheus en la Nube (Recomendado para Ti)

**Problema:** Instalar Prometheus en tu servidor de 1GB consumiría 150-300 MB de RAM extra. No es viable.

**Solución:** Usar un servicio gratuito de Prometheus administrado.

**Opciones:**
1. **Grafana Cloud (Free Tier):** 10,000 series gratuitas, incluye Prometheus + Grafana.
2. **AWS Managed Prometheus (Gratis 2 meses):** Luego pagas por uso.
3. **Sysdig Monitor (Free Tier):** 50 containers gratis.

**Recomendación:** **Grafana Cloud** (es el más simple y tiene todo integrado).

#### Paso 2.1: Crear Cuenta en Grafana Cloud

1. Registrate en https://grafana.com/auth/sign-up/create-user (free tier).
2. Crea un "stack" (te dan una URL tipo `https://tu-proyecto.grafana.net`).
3. Ve a "Connections" > "Add new connection" > "Hosted Prometheus".
4. Copia la URL y el token de autenticación.

#### Paso 2.2: Instalar Grafana Agent en Tu Servidor

**Grafana Agent** es un recolector ligero (usa ~30 MB RAM) que envía métricas a la nube.

SSH a tu servidor:

```bash
# Descargar el binario
wget https://github.com/grafana/agent/releases/download/v0.40.0/grafana-agent-linux-amd64.zip
unzip grafana-agent-linux-amd64.zip
sudo mv grafana-agent-linux-amd64 /usr/local/bin/grafana-agent
sudo chmod +x /usr/local/bin/grafana-agent

# Crear archivo de configuración
sudo mkdir -p /etc/grafana-agent
sudo nano /etc/grafana-agent/config.yml
```

**Contenido de `config.yml`:**

```yaml
server:
  log_level: info

metrics:
  global:
    scrape_interval: 60s  # Cada 60 segundos (no saturar el servidor)
    remote_write:
      - url: https://prometheus-prod-XX-XX.grafana.net/api/prom/push  # Tu URL de Grafana Cloud
        basic_auth:
          username: <TU_INSTANCE_ID>
          password: <TU_API_KEY>

  configs:
    - name: proyecto-gastos
      scrape_configs:
        # Métricas de Spring Boot Actuator
        - job_name: 'spring-boot-backend'
          static_configs:
            - targets: ['backend:8080']  # Nombre del servicio en docker-compose
          metrics_path: '/actuator/prometheus'
```

**Crear servicio systemd:**

```bash
sudo nano /etc/systemd/system/grafana-agent.service
```

```ini
[Unit]
Description=Grafana Agent
After=network.target

[Service]
Type=simple
User=ubuntu
ExecStart=/usr/local/bin/grafana-agent -config.file=/etc/grafana-agent/config.yml
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

**Iniciar el agente:**

```bash
sudo systemctl daemon-reload
sudo systemctl enable grafana-agent
sudo systemctl start grafana-agent
sudo systemctl status grafana-agent  # Verificar que está corriendo
```

#### Paso 2.3: Validar Recolección

1. Ve a Grafana Cloud > Explore.
2. Escribe una query: `jvm_memory_used_bytes`
3. Deberías ver datos de tu aplicación.

**Duración:** 2-3 horas (primera vez).  
**Costo:** $0 (free tier de Grafana Cloud).  
**Beneficio:** Tienes historial de métricas para analizar tendencias.

---

### **Fase 3: Grafana (Visualización)**

**Objetivo:** Crear dashboards visuales profesionales.

#### Paso 3.1: Importar Dashboard Pre-construido

Grafana tiene dashboards comunitarios para Spring Boot.

1. Ve a Grafana Cloud > Dashboards > Import.
2. Usa el ID `4701` (Spring Boot 2.1 System Monitor - compatible con 3.x).
3. Selecciona tu datasource (Prometheus).
4. Guarda.

**Resultado:** Tienes un dashboard profesional con:
- Gráficos de memoria JVM.
- CPU usage.
- HTTP request rate.
- Latencia por endpoint.

#### Paso 3.2: Crear Dashboard Personalizado (Lógica de Negocio)

Para métricas custom (transacciones, resúmenes, etc.), necesitas **instrumentar tu código**.

**Ejemplo: Contador de Transacciones Creadas**

Edita `backend/src/main/java/com/campito/backend/service/TransaccionServiceImpl.java`:

```java
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {
    
    private final TransaccionRepository transaccionRepository;
    private final MeterRegistry meterRegistry;  // Inyectar Micrometer
    
    @Override
    @Transactional
    public TransaccionDTOResponse createTransaccion(TransaccionDTORequest request) {
        // Tu lógica existente...
        Transaccion transaccion = transaccionRepository.save(...);
        
        // Incrementar contador de métrica
        Counter.builder("transacciones.creadas")
               .description("Total de transacciones registradas")
               .tag("tipo", transaccion.getTipo().name())
               .tag("espacio_trabajo", transaccion.getEspacioTrabajo().getId().toString())
               .register(meterRegistry)
               .increment();
        
        return mapper.toResponse(transaccion);
    }
}
```

Ahora en Grafana:
1. Crea un nuevo panel.
2. Query: `rate(transacciones_creadas_total[5m])`
3. Título: "Transacciones por Minuto"
4. Tipo: Time series.

**Duración:** 1 hora para importar dashboard + 2-3 horas para personalizar.  
**Costo:** $0.  
**Beneficio:** Visualización profesional + métricas de negocio.

---

### **Fase 4: Alertas (Proactividad)**

**Objetivo:** Recibir notificaciones automáticas cuando algo va mal.

#### Paso 4.1: Configurar Alertas en Grafana

Ejemplos de alertas críticas:

**Alerta 1: Memoria JVM Alta**

```promql
# Query
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100 > 85

# Condición
Si el valor es > 85% durante 3 minutos consecutivos

# Acción
Enviar email + notificación a Slack/Telegram/Discord
```

**Alerta 2: Resúmenes No Generados**

```promql
# Query
increase(resumenes_generados_total[1d]) == 0

# Condición
Si en las últimas 24 horas no se generó ningún resumen (debería ser > 0 el día de cierre)

# Acción
Enviar alerta crítica
```

**Alerta 3: Errores HTTP 500**

```promql
# Query
rate(http_server_requests_seconds_count{status="500"}[5m]) > 0

# Condición
Si hay al menos 1 error 500 por minuto

# Acción
Investigar logs inmediatamente
```

#### Paso 4.2: Configurar Canal de Notificación

Opciones (todas free):
- **Email:** Configurar SMTP (Gmail gratis).
- **Telegram:** Crear bot con BotFather y configurar en Grafana.
- **Discord:** Crear webhook en tu servidor de Discord.
- **Slack:** (si tienes workspace).

**Duración:** 2 horas.  
**Costo:** $0.  
**Beneficio:** Te enteras de problemas antes que tus clientes.

---

### **Fase 5: Optimización Continua (Post-Implementación)**

Una vez que tengas las fases 1-4, usa las métricas para mejorar:

#### Acciones Concretas

1. **Reducir Tiempos de Respuesta:**
   - Identifica el endpoint más lento en Grafana.
   - Usa `@Transactional(readOnly = true)` en lecturas.
   - Agrega índices en columnas filtradas frecuentemente.
   - Implementa cache con `@Cacheable` para datos estáticos.

2. **Optimizar Memoria:**
   - Si la JVM usa constantemente > 80%, revisa si hay:
     - Colecciones grandes en memoria (Listas de transacciones).
     - Objetos no liberados (memory leaks).
   - Ajusta el garbage collector: `-XX:+UseG1GC` (mejor para heap pequeño).

3. **Probar Límites:**
   - Usa herramientas como `Apache JMeter` o `k6` para hacer load testing.
   - Simula 5 usuarios concurrentes creando transacciones.
   - Observa en Grafana cómo se comporta la memoria y CPU.

4. **Documentar Capacidad:**
   - "Mi aplicación soporta 10 peticiones/segundo con 300ms de latencia promedio".
   - Esto lo pones en tu README y en tu CV.

---

## 📊 Stack Tecnológico Recomendado (Resumen)

| Componente | Herramienta | Dónde Corre | Costo Mensual | Uso de RAM |
|------------|-------------|-------------|---------------|------------|
| **Instrumentación** | Spring Boot Actuator + Micrometer | Tu backend (Java) | $0 | 0 MB (ya incluido) |
| **Recolección** | Grafana Agent | Servidor Oracle (proceso ligero) | $0 | ~30 MB |
| **Almacenamiento** | Prometheus (Grafana Cloud) | Nube de Grafana | $0 (free tier) | 0 MB local |
| **Visualización** | Grafana Cloud | Nube de Grafana | $0 (free tier) | 0 MB local |
| **Alertas** | Grafana Alerting | Nube de Grafana | $0 | 0 MB local |

**Total de RAM Consumido en Tu Servidor:** ~30 MB (solo Grafana Agent).  
**Total de Costo Mensual:** $0.

---

## 🎯 Roadmap de Implementación (Timeline)

### **Sprint 1: Semana 1 (Fundamentos)**
- [ ] Fase 0: Documentar estado actual (30 min)
- [ ] Fase 1: Implementar Actuator (2 horas)
- [ ] Desplegar a producción y validar (1 hora)
- [ ] **Entregable:** Endpoints `/actuator/health` y `/actuator/metrics` funcionando.

### **Sprint 2: Semana 2 (Recolección)**
- [ ] Fase 2: Configurar Grafana Cloud (1 hora)
- [ ] Fase 2: Instalar Grafana Agent en servidor (2 horas)
- [ ] Validar que las métricas llegan a la nube (30 min)
- [ ] **Entregable:** Datos visibles en Grafana Cloud Explore.

### **Sprint 3: Semana 3 (Visualización)**
- [ ] Fase 3: Importar dashboard de Spring Boot (30 min)
- [ ] Fase 3: Instrumentar métricas custom (transacciones, resúmenes) (3 horas)
- [ ] Crear panel de métricas de negocio (1 hora)
- [ ] **Entregable:** Dashboard completo con sistema + negocio.

### **Sprint 4: Semana 4 (Alertas)**
- [ ] Fase 4: Configurar 3 alertas críticas (memoria, errores, scheduler) (2 horas)
- [ ] Fase 4: Configurar Telegram/Discord para notificaciones (30 min)
- [ ] Probar alertas (apagar el backend intencionalmente) (30 min)
- [ ] **Entregable:** Sistema de alertas operativo.

### **Sprint 5: Semana 5 (Documentación)**
- [ ] Capturar screenshots de dashboards.
- [ ] Actualizar README_BACKEND.md con sección de Observabilidad.
- [ ] Crear documento de "Troubleshooting" con playbooks (ej: "Si memoria > 90%, hacer X").
- [ ] **Entregable:** Documentación profesional para mostrar en portfolio.

**Tiempo Total Estimado:** 20-25 horas.  
**Costo Total:** $0.

---

## 🚀 Beneficios Concretos Para Tu Carrera

### 1. **Para Entrevistas de Trabajo**

**Pregunta del reclutador:** *"¿Cómo monitorizas tus aplicaciones en producción?"*

**Tu respuesta ANTES de observabilidad:**
> "Reviso los logs con `docker logs` y si algo falla, lo soluciono."

**Tu respuesta DESPUÉS de observabilidad:**
> "Uso Prometheus y Grafana. Tengo dashboards que monitorizan JVM heap memory, latencia de endpoints, y métricas de negocio como transacciones procesadas. Configuré alertas para memoria > 85% y errores HTTP 500. Esto me permitió detectar que el endpoint de dashboard tenía un problema de performance y lo optimicé de 4s a 300ms usando índices en PostgreSQL."

**Resultado:** El reclutador te ve como un **Ingeniero de Software Senior** a pesar de ser Junior.

### 2. **Para Tus Clientes (Confianza)**

Puedes mostrarles un dashboard público (read-only) con:
- "Tu aplicación tiene 99.8% de uptime este mes."
- "Tiempo de respuesta promedio: 250ms."
- "Tu espacio de trabajo procesó 145 transacciones este mes."

**Resultado:** Tus clientes pagan con confianza porque ven **transparencia y profesionalismo**.

### 3. **Para Tu Portfolio**

En tu README de GitHub puedes agregar:

```markdown
## 📊 Observabilidad

Este proyecto implementa observabilidad completa con:
- **Spring Boot Actuator + Micrometer** para instrumentación
- **Prometheus** para recolección de métricas
- **Grafana** para visualización y alertas

### Dashboards
![Dashboard Principal](docs/images/grafana-dashboard.png)

### Métricas Monitorizadas
- JVM Memory & GC
- HTTP Request Rate & Latency
- Database Connection Pool
- Métricas de negocio (transacciones, resúmenes)

### Alertas Configuradas
- Memoria JVM > 85%
- Errores HTTP 500
- Scheduler de resúmenes fallido
```

**Resultado:** Tu proyecto destaca entre cientos de portfolios genéricos.

---

## 🤔 Preguntas Frecuentes

### **P: ¿No es exagerado para una app de 1-4 usuarios?**

**R:** No. Precisamente PORQUE tienes pocos usuarios y recursos limitados, necesitas detectar problemas antes de que escale. Es más fácil implementar observabilidad ahora que cuando tengas 50 usuarios y el servidor crashee diariamente.

### **P: ¿Cuánto tiempo de mi servidor consumirá esto?**

**R:** El Grafana Agent usa ~30 MB de RAM (3% de tu 1GB) y scrapes cada 60 segundos (CPU despreciable). Spring Boot Actuator no añade overhead significativo.

### **P: ¿Qué pasa si supero el free tier de Grafana Cloud?**

**R:** El límite es 10,000 series de tiempo. Con tu aplicación, usarás ~200-500 series. Estarías VERY lejos del límite. Y si lo superas, puedes migrar a una solución self-hosted más tarde (cuando tengas más recursos).

### **P: ¿Es difícil de mantener?**

**R:** Una vez configurado (20-25 horas iniciales), el mantenimiento es CERO. El Grafana Agent se actualiza automáticamente y las métricas se recolectan sin intervención.

---

## 📚 Recursos de Aprendizaje

### Tutoriales Recomendados

1. **Spring Boot Actuator:**
   - [Documentación Oficial](https://docs.spring.io/spring-boot/reference/actuator/index.html)
   - Blog: "Spring Boot Actuator in 10 Minutes"

2. **Micrometer:**
   - [Micrometer.io](https://micrometer.io/docs)
   - Tutorial: "Custom Metrics with Micrometer"

3. **Prometheus:**
   - [Prometheus - First Steps](https://prometheus.io/docs/introduction/first_steps/)
   - Curso gratuito: "Prometheus for Beginners" (YouTube)

4. **Grafana:**
   - [Grafana Fundamentals](https://grafana.com/tutorials/grafana-fundamentals/)
   - Dashboard Gallery: [grafana.com/grafana/dashboards](https://grafana.com/grafana/dashboards/)

### Libros (Opcionales)

- **"Observability Engineering" - Charity Majors** (el estándar de la industry)
- **"Site Reliability Engineering" - Google** (gratis online)

---

## ✅ Conclusión y Próximos Pasos

### **Mi Recomendación Profesional**

**Implementa la observabilidad en las próximas 4 semanas.** Es la inversión de tiempo más rentable que puedes hacer en tu proyecto. Te dará:

1. **Seguridad operativa:** No más crashes inesperados.
2. **Confianza para crecer:** Cuando lleguen clientes, ya estás preparado.
3. **Ventaja competitiva:** Tu portfolio se destacará en entrevistas.
4. **Conocimiento valioso:** Aprenderás herramientas que se usan en empresas reales.

### **Checklist de Decisión**

Antes de empezar, confirma que tienes:

- [ ] Acceso SSH a tu servidor Oracle Cloud
- [ ] Cuenta en Grafana Cloud (free tier)
- [ ] Tiempo estimado: 20-25 horas en 4 semanas
- [ ] Mindset: "Voy a aprender algo nuevo y mejorar mi proyecto"

---

## 🎬 ¿Listo Para Empezar?

**Próximo paso sugerido:**

1. Revisa este plan completo.
2. Dime qué partes te generan dudas.
3. Comenzamos con la **Fase 1 (Actuator)** que es la más simple y ya te da valor inmediato.

**Pregunta para ti:**  
¿Quieres que proceda a implementar la Fase 1 (Spring Boot Actuator) directamente o prefieres que aclare alguna sección del plan primero?

---

**Autor:** GitHub Copilot (Claude Sonnet 4.5)  
**Fecha:** Febrero 2026  
**Versión:** 1.0  
**Proyecto:** ProyectoGastos - Sistema de Gestión de Gastos Personales
