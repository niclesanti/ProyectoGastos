package com.campito.backend.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.config.MetricsConfig;
import com.campito.backend.dao.AgenteAuditLogRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * Scheduler que ejecuta la limpieza automática del historial del agente IA a la 01:00 AM.
 *
 * <p>Elimina todos los registros de {@code AgenteAuditLog} cuyo timestamp sea anterior
 * al inicio del día actual (es decir, el día anterior y cualquier día previo que haya
 * quedado pendiente de borrar). De esta forma se garantiza que el historial almacenado
 * corresponda únicamente al día en curso.</p>
 *
 * <p>Esta tarea sigue el mismo patrón que {@link ResumenScheduler}: métricas Prometheus,
 * logging estructurado y manejo de errores sin interrumpir el proceso.</p>
 */
@Component
@RequiredArgsConstructor
public class AgenteHistorialScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AgenteHistorialScheduler.class);

    private final AgenteAuditLogRepository agenteAuditLogRepository;
    private final MeterRegistry meterRegistry;

    /**
     * Elimina el historial del agente IA todos los días a la 01:00 AM (hora Argentina).
     *
     * <p>Borra todos los registros con {@code timestamp} anterior al inicio del día actual,
     * cubriendo el día anterior y cualquier registro más antiguo que no haya sido eliminado
     * en ejecuciones previas.</p>
     *
     * <p>Cron: segundo minuto hora día mes día_semana</p>
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void limpiarHistorialDiario() {

        // Inicio del día actual → se eliminan todos los registros anteriores a este instante
        LocalDateTime inicioDiaActual = LocalDate.now().atStartOfDay();

        logger.info("Iniciando limpieza automática del historial del agente IA. Eliminando registros anteriores a {}",
                inicioDiaActual);

        // 📊 MÉTRICA: Medir tiempo total de ejecución de la limpieza
        var timerSample = Timer.start(meterRegistry);

        int registrosEliminados = 0;
        boolean conError = false;

        try {
            registrosEliminados = agenteAuditLogRepository.deleteByTimestampBefore(inicioDiaActual);

            logger.info("Limpieza del historial del agente IA finalizada. Registros eliminados: {}",
                    registrosEliminados);

            // 📊 MÉTRICA: Contador de registros eliminados
            if (registrosEliminados > 0) {
                Counter.builder(MetricsConfig.MetricNames.AGENTE_HISTORIAL_ELIMINADOS)
                        .description("Total de registros de historial del agente IA eliminados por el scheduler")
                        .register(meterRegistry)
                        .increment(registrosEliminados);
            }

        } catch (Exception e) {
            conError = true;
            logger.error("Error al limpiar el historial del agente IA", e);

            // 📊 MÉTRICA: Contador de errores
            Counter.builder(MetricsConfig.MetricNames.AGENTE_HISTORIAL_ERRORES)
                    .description("Total de errores en la limpieza del historial del agente IA")
                    .register(meterRegistry)
                    .increment();

        } finally {
            // 📊 MÉTRICA: Registrar tiempo de ejecución
            timerSample.stop(Timer.builder(MetricsConfig.MetricNames.AGENTE_HISTORIAL_TIMER)
                    .description("Tiempo de ejecución del scheduler de limpieza del historial del agente IA")
                    .tag("resultado", conError ? "error" : "exitoso")
                    .register(meterRegistry));
        }
    }
}
