package com.campito.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuración centralizada de métricas de negocio para Micrometer/Prometheus.
 * 
 * Define las métricas custom que se utilizan en toda la aplicación para
 * monitorear la lógica de negocio específica de ProyectoGastos.
 * 
 * Categorías de métricas:
 * - Transacciones (gastos, ingresos, transferencias)
 * - Compras a crédito y cuotas
 * - Resúmenes de tarjetas (scheduler)
 * - Notificaciones en tiempo real (SSE)
 * - Espacios de trabajo (multi-tenancy)
 * 
 * @author ProyectoGastos Team
 */
@Configuration
public class MetricsConfig {

    /**
     * Define los nombres de las métricas como constantes para evitar errores de tipeo
     * y facilitar el mantenimiento.
     */
    public static class MetricNames {
        // Transacciones
        public static final String TRANSACCIONES_CREADAS = "negocio.transacciones.creadas";
        public static final String TRANSACCIONES_ELIMINADAS = "negocio.transacciones.eliminadas";
        
        // Compras a Crédito
        public static final String COMPRAS_CREDITO_CREADAS = "negocio.compras.credito.creadas";
        public static final String CUOTAS_PENDIENTES = "negocio.cuotas.pendientes";
        public static final String CUOTAS_PAGADAS = "negocio.cuotas.pagadas";
        
        // Resúmenes (Scheduler)
        public static final String RESUMENES_GENERADOS = "negocio.resumenes.generados";
        public static final String RESUMENES_ERRORES = "negocio.resumenes.errores";
        public static final String RESUMENES_TIMER = "negocio.resumenes.tiempo";
        public static final String RESUMENES_PAGADOS = "negocio.resumenes.pagados";
        
        // Notificaciones
        public static final String NOTIFICACIONES_ENVIADAS = "negocio.notificaciones.enviadas";
        public static final String NOTIFICACIONES_LEIDAS = "negocio.notificaciones.leidas";
        public static final String SSE_CONEXIONES_ACTIVAS = "negocio.sse.conexiones.activas";
        
        // Espacios de Trabajo (Multi-tenancy)
        public static final String ESPACIOS_ACTIVOS = "negocio.espacios.activos";
        public static final String ESPACIOS_CREADOS = "negocio.espacios.creados";
    }

    /**
     * Define tags estándar para filtrar métricas en Grafana.
     */
    public static class TagNames {
        public static final String TIPO_TRANSACCION = "tipo";
        public static final String TIPO_NOTIFICACION = "tipo_notificacion";
        public static final String ESPACIO_TRABAJO = "espacio_trabajo_id";
        public static final String METODO_PAGO = "metodo_pago";
        public static final String ESTADO = "estado";
        public static final String RESULTADO = "resultado";
    }

    /**
     * Gauge para contar cuotas pendientes.
     * Usa AtomicInteger para thread-safety ya que puede ser actualizado desde múltiples hilos.
     */
    private final AtomicInteger cuotasPendientesCount = new AtomicInteger(0);

    /**
     * Gauge para contar conexiones SSE activas.
     */
    private final AtomicInteger sseConexionesActivasCount = new AtomicInteger(0);

    /**
     * Bean para registrar el gauge de cuotas pendientes.
     * Este valor debe ser actualizado manualmente cuando se crean o pagan cuotas.
     */
    @Bean
    public AtomicInteger cuotasPendientesGauge(MeterRegistry registry) {
        Gauge.builder(MetricNames.CUOTAS_PENDIENTES, cuotasPendientesCount, AtomicInteger::get)
                .description("Cantidad total de cuotas de crédito pendientes de pago en todos los espacios")
                .baseUnit("cuotas")
                .register(registry);
        return cuotasPendientesCount;
    }

    /**
     * Bean para registrar el gauge de conexiones SSE activas.
     */
    @Bean
    public AtomicInteger sseConexionesActivasGauge(MeterRegistry registry) {
        Gauge.builder(MetricNames.SSE_CONEXIONES_ACTIVAS, sseConexionesActivasCount, AtomicInteger::get)
                .description("Cantidad de conexiones Server-Sent Events (SSE) activas en tiempo real")
                .baseUnit("conexiones")
                .register(registry);
        return sseConexionesActivasCount;
    }

    /**
     * Nota: Los Counters y Timers NO se registran como Beans porque se crean
     * dinámicamente en cada servicio usando el MeterRegistry inyectado.
     * 
     * Esto permite agregar tags específicos según el contexto (por ejemplo, tipo de transacción).
     */
}
