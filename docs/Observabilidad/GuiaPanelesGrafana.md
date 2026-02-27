# 📊 Guía de Paneles de Grafana para Métricas de Negocio

Esta guía te muestra cómo crear paneles personalizados en Grafana Cloud para visualizar las métricas de lógica de negocio implementadas en ProyectoGastos.

---

## 📋 Tabla de Contenidos
1. [Métricas Implementadas](#métricas-implementadas)
2. [Queries de Prometheus por Categoría](#queries-de-prometheus)
3. [Cómo Crear Paneles en Grafana](#cómo-crear-paneles)
4. [Ejemplos de Dashboards Completos](#ejemplos-de-dashboards)

---

## 🎯 Métricas Implementadas

### Categoría 1: Transacciones

| Nombre de Métrica | Tipo | Descripción | Tags |
|-------------------|------|-------------|------|
| `negocio_transacciones_creadas_total` | Counter | Total de transacciones registradas | `tipo`, `espacio_trabajo_id` |
| `negocio_transacciones_eliminadas_total` | Counter | Total de transacciones eliminadas | `tipo`, `espacio_trabajo_id` |

### Categoría 2: Compras a Crédito

| Nombre de Métrica | Tipo | Descripción | Tags |
|-------------------|------|-------------|------|
| `negocio_compras_credito_creadas_total` | Counter | Total de compras a crédito | `espacio_trabajo_id`, `tarjeta_id`, `cuotas` |

### Categoría 3: Resúmenes de Tarjetas (Scheduler)

| Nombre de Métrica | Tipo | Descripción | Tags |
|-------------------|------|-------------|------|
| `negocio_resumenes_generados_total` | Counter | Resúmenes generados por el scheduler | - |
| `negocio_resumenes_pagados_total` | Counter | Resúmenes pagados exitosamente | `espacio_trabajo_id`, `tarjeta_id` |
| `negocio_resumenes_errores_total` | Counter | Errores al generar resúmenes | `tarjeta_id` |
| `negocio_resumenes_tiempo_seconds` | Timer | Tiempo de ejecución del scheduler | `resultado` |

### Categoría 4: Notificaciones

| Nombre de Métrica | Tipo | Descripción | Tags |
|-------------------|------|-------------|------|
| `negocio_notificaciones_enviadas_total` | Counter | Notificaciones enviadas exitosamente | `tipo_notificacion` |
| `negocio_notificaciones_leidas_total` | Counter | Notificaciones marcadas como leídas | `tipo_notificacion` |
| `negocio_sse_conexiones_activas` | Gauge | Conexiones SSE activas en tiempo real | - |

### Categoría 5: Agente IA — Limpieza de Historial (Scheduler)

Ejecutado diariamente a las 01:00 AM. Elimina todos los registros de `agente_audit_log` anteriores al inicio del día actual.

| Nombre de Métrica | Tipo | Descripción | Tags |
|-------------------|------|-------------|------|
| `negocio_agente_historial_eliminados_total` | Counter | Total de registros de historial eliminados por el scheduler | - |
| `negocio_agente_historial_errores_total` | Counter | Errores durante la limpieza del historial | - |
| `negocio_agente_historial_tiempo_seconds` | Timer | Tiempo de ejecución del scheduler de limpieza | `resultado=[exitoso\|error]` |

### Categoría 6: Agente IA — Operaciones en Tiempo Real

Métricas registradas en `AgenteIAServiceImpl` por cada interacción con el LLM (Groq).

| Nombre de Métrica | Tipo | Descripción | Tags |
|-------------------|------|-------------|------|
| `negocio_agente_requests_total` | Counter | Total de requests al agente IA | `tipo=[chat\|stream]`, `resultado=[exitoso\|error\|iniciado]` |
| `negocio_agente_tokens_consumidos_total` | Counter | Tokens consumidos en respuestas del LLM (correlaciona con costo de API Groq) | `tipo=[chat]` |
| `negocio_agente_latencia_seconds` | Timer | Latencia de respuesta del LLM en modo chat bloqueante | `tipo=[chat]` |

---

## 📊 Queries de Prometheus por Categoría

### Panel 1: **Transacciones por Tipo (Tasa por Minuto)**

**Descripción:** Muestra cuántas transacciones (GASTO, INGRESO, TRANSFERENCIA) se crean por minuto en tiempo real.

**Query PromQL:**
```promql
rate(negocio_transacciones_creadas_total[5m])
```

**Configuración del Panel:**
- **Tipo:** Time series (línea)
- **Legend:** `{{tipo}}`
- **Unit:** ops/sec
- **Color:** 
  - GASTO: Rojo
  - INGRESO: Verde
  - TRANSFERENCIA: Azul

---

### Panel 2: **Transacciones por Espacio de Trabajo (Top 5)**

**Descripción:** Identifica qué espacios de trabajo tienen más actividad.

**Query PromQL:**
```promql
topk(5, sum by (espacio_trabajo_id) (negocio_transacciones_creadas_total))
```

**Configuración del Panel:**
- **Tipo:** Bar chart (horizontal)
- **Legend:** `Espacio {{espacio_trabajo_id}}`
- **Unit:** None
- **Display:** Last value

---

### Panel 3: **Ratio de Transacciones Eliminadas vs Creadas**

**Descripción:** Muestra qué porcentaje de transacciones se están eliminando (puede indicar errores del usuario o problemas de UX).

**Query PromQL:**
```promql
(sum(negocio_transacciones_eliminadas_total) / sum(negocio_transacciones_creadas_total)) * 100
```

**Configuración del Panel:**
- **Tipo:** Gauge (medidor)
- **Legend:** Ratio de Eliminación
- **Unit:** Percent (0-100)
- **Thresholds:**
  - Verde: 0-10%
  - Amarillo: 10-25%
  - Rojo: >25% (podría haber un problema de usabilidad)

---

### Panel 4: **Compras a Crédito por Cantidad de Cuotas**

**Descripción:** Muestra qué cantidad de cuotas prefieren tus usuarios (1, 3, 6, 12, etc.).

**Query PromQL:**
```promql
sum by (cuotas) (negocio_compras_credito_creadas_total)
```

**Configuración del Panel:**
- **Tipo:** Pie chart (torta)
- **Legend:** `{{cuotas}} cuotas`
- **Unit:** None
- **Display:** Last value

---

### Panel 5: **Cuotas Pagadas (Tasa Diaria)**

**Descripción:** Muestra cuántas cuotas se están pagando por día.

**Query PromQL:**
```promql
increase(negocio_cuotas_pagadas_total[1d])
```

**Configuración del Panel:**
- **Tipo:** Time series (línea)
- **Legend:** Cuotas Pagadas Hoy
- **Unit:** None
- **Color:** Verde

---

### Panel 6: **Resúmenes Generados por el Scheduler (Éxito vs Errores)**

**Descripción:** Monitorea la salud del proceso automático de cierre de resúmenes.

**Queries PromQL:**
```promql
# Query A (Exitosos)
increase(negocio_resumenes_generados_total[1d])

# Query B (Errores)
increase(negocio_resumenes_errores_total[1d])
```

**Configuración del Panel:**
- **Tipo:** Time series (barras apiladas)
- **Legend:**
  - Query A: Exitosos (verde)
  - Query B: Errores (rojo)
- **Unit:** None
- **Alert:** 
  - Condición: `negocio_resumenes_errores_total > 0`
  - Severidad: Crítica

---

### Panel 7: **Tiempo de Ejecución del Scheduler de Resúmenes**

**Descripción:** Monitorea si el scheduler está tardando más de lo normal (puede indicar problemas de performance).

**Query PromQL:**
```promql
negocio_resumenes_tiempo_seconds_sum / negocio_resumenes_tiempo_seconds_count
```

**Configuración del Panel:**
- **Tipo:** Stat (valor grande)
- **Legend:** Tiempo Promedio
- **Unit:** Seconds (s)
- **Thresholds:**
  - Verde: 0-30s (normal)
  - Amarillo: 30-60s (lento)
  - Rojo: >60s (muy lento, investigar)

---

### Panel 8: **Notificaciones Enviadas por Tipo (Últimas 24 horas)**

**Descripción:** Muestra qué tipos de notificaciones se están generando más.

**Query PromQL:**
```promql
sum by (tipo_notificacion) (increase(negocio_notificaciones_enviadas_total[24h]))
```

**Configuración del Panel:**
- **Tipo:** Bar chart (horizontal)
- **Legend:** `{{tipo_notificacion}}`
- **Unit:** None
- **Ejemplos de valores esperados:**
  - `CIERRE_TARJETA`: Al final de cada mes (días 20-31)
  - `CUOTAS_PENDIENTES`: Diariamente a las 3 AM
  - `RESUMEN_VENCIDO`: Cuando hay tarjetas sin pagar

---

### Panel 9: **Ratio de Notificaciones Leídas**

**Descripción:** Mide el engagement de los usuarios con las notificaciones (¿las leen o las ignoran?).

**Query PromQL:**
```promql
(sum(negocio_notificaciones_leidas_total) / sum(negocio_notificaciones_enviadas_total)) * 100
```

**Configuración del Panel:**
- **Tipo:** Gauge (medidor)
- **Legend:** % de Notificaciones Leídas
- **Unit:** Percent (0-100)
- **Thresholds:**
  - Rojo: 0-30% (baja lectura, revisar relevancia)
  - Amarillo: 30-60% (moderado)
  - Verde: 60-100% (buena adopción)

---

### Panel 10: **Conexiones SSE Activas en Tiempo Real**

**Descripción:** Muestra cuántos usuarios están conectados en este momento vía Server-Sent Events.

**Query PromQL:**
```promql
negocio_sse_conexiones_activas
```

**Configuración del Panel:**
- **Tipo:** Stat (valor grande)
- **Legend:** Usuarios Conectados
- **Unit:** None
- **Color:** Azul
- **Note:** Este valor debería ser igual al número de usuarios con sesión activa.

---

### Panel 11: **Resúmenes Pagados (Mensual)**

**Descripción:** Muestra cuántos resúmenes de tarjeta se pagaron este mes (útil para entender el ciclo de pagos).

**Query PromQL:**
```promql
increase(negocio_resumenes_pagados_total[30d])
```

**Configuración del Panel:**
- **Tipo:** Time series (área)
- **Legend:** Resúmenes Pagados Este Mes
- **Unit:** None
- **Color:** Verde

---

## 🛠 Cómo Crear Paneles en Grafana

### Opción 1: Dashboard Nuevo (Recomendado)

Sí, puedes crear un **dashboard completamente nuevo solo para métricas de negocio** y así mantener el dashboard de JVM/sistema separado.

**Pasos:**

1. **Ingresar a Grafana Cloud:**
   - Ve a tu URL: `https://tu-proyecto.grafana.net`
   - Login con tu cuenta.

2. **Crear Nuevo Dashboard:**
   - Click en el botón `+` (Create) en la barra lateral izquierda
   - Selecciona `Dashboard`
   - Se abrirá un dashboard vacío

3. **Darle un Nombre:**
   - Click en el ícono de configuración ⚙️ (arriba a la derecha)
   - En "Dashboard settings" > "General"
   - **Title:** `ProyectoGastos - Métricas de Negocio`
   - **Description:** `Dashboard con métricas de lógica de negocio: transacciones, compras a crédito, resúmenes, notificaciones.`
   - **Tags:** `negocio`, `financiero`, `produccion`
   - Click en `Save dashboard` (icono disquete arriba a la derecha)

4. **Agregar Primer Panel:**
   - Click en `Add panel` > `Add a new panel`
   - En el editor de query (parte inferior):
     - **Data source:** Selecciona tu Prometheus
     - **Query:** Pega la query PromQL (ejemplo: `rate(negocio_transacciones_creadas_total[5m])`)
   - En el panel derecho (configuración):
     - **Panel title:** `Transacciones por Minuto`
     - **Description:** `Tasa de transacciones creadas por tipo`
     - **Legend:** `{{tipo}}`
     - **Unit:** `ops/sec` (en Format > Unit)
   - Click en `Apply` (arriba a la derecha)

5. **Agregar Más Paneles:**
   - Vuelve al dashboard
   - Click en `Add panel` > `Add a new panel`
   - Repite el proceso para cada panel listado arriba

6. **Organizar Layout:**
   - Arrastra los paneles para organizarlos
   - Puedes cambiar el tamaño estirando desde las esquinas
   - **Sugerencia de organización:**
     ```
     +------------------------------+------------------------------+
     |     Transacciones/min        |   Volumen Dinero Procesado   |
     |     (Time series)            |         (Stat)               |
     +------------------------------+------------------------------+
     |     Top 5 Espacios           |   Ratio Eliminadas/Creadas   |
     |     (Bar chart)              |        (Gauge)               |
     +------------------------------+------------------------------+
     |  Compras Crédito por Cuotas  |   Cuotas Pagadas (Diario)    |
     |     (Pie chart)              |      (Time series)           |
     +------------------------------+------------------------------+
     ```

7. **Guardar el Dashboard:**
   - Click en el icono disquete (arriba a la derecha)
   - **Note:** Puedes agregar un mensaje de commit (opcional)
   - Click en `Save`

---

### Opción 2: Agregar al Dashboard Existente

Si prefieres tener todo en un solo dashboard, puedes agregar estos paneles al dashboard que ya configuraste con métricas de JVM.

**Pasos:**

1. **Abrir tu Dashboard existente:**
   - Desde la barra lateral: `Dashboards` > `Dashboard - ProyectoGastos Backend`

2. **Agregar Separador Visual (Opcional pero recomendado):**
   - Click en `Add panel` > `Add a new panel`
   - En el panel derecho, cambia el tipo a `Text`
   - **Content:** 
     ```markdown
     # 📊 Métricas de Lógica de Negocio
     
     Monitoreo de transacciones, compras a crédito, resúmenes y notificaciones.
     ```
   - **Mode:** Markdown
   - Estira el panel para que ocupe todo el ancho
   - Click en `Apply`

3. **Agregar Paneles de Negocio:**
   - Sigue el mismo proceso del punto 4 de la Opción 1
   - Los paneles se agregarán debajo de los existentes

4. **Reorganizar:**
   - Arrastra el separador de "Métricas de Negocio" para que esté después de los paneles de JVM
   - Arrastra tus nuevos paneles debajo del separador

5. **Guardar:**
   - Click en el icono disquete

---

## 📌 Recomendación Personal

**Te recomiendo crear UN NUEVO DASHBOARD separado** llamado `ProyectoGastos - Métricas de Negocio` por estas razones:

✅ **Ventajas:**
- **Audiencia diferente:** El dashboard de JVM es para ti (developer/DevOps). El de negocio puede mostrárselo a tus clientes/stakeholders.
- **Más limpio:** No mezclas métricas técnicas (heap, CPU) con métricas de producto (transacciones, pagos).
- **Mejor para entrevistas:** Puedes mostrar ambos dashboards en una presentación técnica y demostrar que entiendes la diferencia entre monitoreo técnico y análisis de producto.
- **Facilita permisos:** En el futuro, si quieres que un cliente vea solo las métricas de su espacio de trabajo, puedes crear un dashboard filtrado.

🚀 **Dashboard de JVM (actual):**
- Enfoque: Salud técnica del sistema
- Audiencia: Tú, tu equipo de desarrollo
- Alertas: OOM, CPU alto, conexiones saturadas

📊 **Dashboard de Negocio (nuevo):**
- Enfoque: Uso de la aplicación y KPIs de producto
- Audiencia: Clientes, stakeholders, PM, tu portfolio
- Alertas: Scheduler falló, notificaciones no leídas, ratio de eliminación alto

---

## 🎨 Ejemplo de Dashboard Completo

### "ProyectoGastos - Métricas de Negocio"

**Estructura recomendada:**

```
+------------------------------------------------------------------+
|                   🎯 ProyectoGastos - Métricas de Negocio        |
|                           Dashboard de Monitoreo                  |
+------------------------------------------------------------------+

+---------------------------+-------------------+-------------------+
|  Transacciones/min        | Top 5 Espacios    | Ratio Eliminadas  |
|  (Time series - línea)    | (Bar chart)       | (Gauge - %)       |
+---------------------------+-------------------+-------------------+

+---------------------------+-------------------+-------------------+
|  Top 5 Espacios Activos   | Compras Crédito   | Cuotas Pagadas    |
|  (Bar chart - horizontal) | (Pie chart)       | (Time series)     |
+---------------------------+-------------------+-------------------+

+------------------------------------------------------------------+
|  Resúmenes: Exitosos vs Errores (Time series - barras apiladas) |
+------------------------------------------------------------------+

+---------------------------+-------------------+-------------------+
|  Tiempo Ejecución         | Notificaciones    | SSE Conexiones    |
|  Scheduler (Stat - s)     | Leídas (Gauge %)  | Activas (Stat)    |
+---------------------------+-------------------+-------------------+

+------------------------------------------------------------------+
|  Resúmenes Pagados (Mensual) - (Time series - área)             |
+------------------------------------------------------------------+
```

---

## ⚡ Atajos de Teclado en Grafana

- **E:** Edit panel (con el panel seleccionado)
- **V:** View mode
- **D:** Duplicate panel
- **P:** Share panel
- **Ctrl + S:** Save dashboard
- **Esc:** Exit panel edit

---

## 🔔 Configurar Alertas (Bonus)

Una vez que tengas los paneles, puedes configurar alertas:

### Ejemplo: Alerta si el Scheduler Falla

1. Edita el panel "Resúmenes: Exitosos vs Errores"
2. Ve a la pestaña `Alert`
3. Click en `Create alert rule from this panel`
4. Configura:
   ```
   Condition: WHEN last() OF query(B) IS ABOVE 0
   Meaning: Si hay al menos 1 error en generación de resúmenes
   
   For: 5 minutes
   
   Notification: Email / Telegram
   
   Message: 
   ⚠️ El scheduler de resúmenes falló al procesar al menos una tarjeta.
   Revisa los logs en Docker: `docker logs springboot-campito-prod`
   ```
5. Save

---

## 📖 Recursos Adicionales

- **Documentación de PromQL:** https://prometheus.io/docs/prometheus/latest/querying/basics/
- **Grafana Panel Types:** https://grafana.com/docs/grafana/latest/panels-visualizations/
- **Community Dashboards:** https://grafana.com/grafana/dashboards/ (puedes buscar "Spring Boot" para inspirarte)

---

¡Con esto tienes toda la información para crear tus dashboards! 🎉

**Próximos pasos:**
1. Desplegar tu backend con las métricas instrumentadas
2. Crear el dashboard en Grafana Cloud
3. Agregar los paneles uno por uno
4. Configurar alertas críticas
5. Screenshots para tu portfolio 📸
