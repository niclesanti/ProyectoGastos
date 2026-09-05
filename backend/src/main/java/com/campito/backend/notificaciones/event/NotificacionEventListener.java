package com.campito.backend.notificaciones.event;

import lombok.extern.slf4j.Slf4j;
import com.campito.backend.common.event.NotificacionEvent;
import com.campito.backend.notificaciones.repository.NotificacionRepository;
import com.campito.backend.notificaciones.mapper.NotificacionMapper;
import com.campito.backend.notificaciones.domain.entity.Notificacion;
import com.campito.backend.notificaciones.service.SseEmitterService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.campito.backend.config.MetricsConfig;

/**
 * Listener que procesa eventos de notificación de forma asíncrona.
 * 
 * Responsabilidades:
 * 1. Capturar eventos {@link NotificacionEvent}
 * 2. Crear el registro de notificación en la base de datos
 * 3. Enviar la notificación en tiempo real via SSE (si está habilitado)
 * 
 * El procesamiento asíncrono (@Async) evita bloquear el flujo principal
 * de ejecución del servicio que publica el evento.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionEventListener {
    
    private final NotificacionRepository notificacionRepository;
    private final NotificacionMapper notificacionMapper;
    private final SseEmitterService sseEmitterService;
    private final MeterRegistry meterRegistry;  // Para métricas de Prometheus/Grafana
    
    /**
     * Maneja el evento de notificación de forma asíncrona.
     * 
     * @param event Evento con los datos de la notificación
     */
    @Async
    @EventListener
    @Transactional
    public void handleNotificacionEvent(NotificacionEvent event) {
        try {
            log.info("Procesando notificación: tipo={}, usuario={}", 
                       event.getTipo(), event.getIdUsuario());
            
            // 2. Crear notificación
            Notificacion notificacion = new Notificacion();
            notificacion.setIdUsuario(event.getIdUsuario());
            notificacion.setTipo(event.getTipo());
            notificacion.setMensaje(event.getMensaje());
            
            // 3. Guardar en BD
            notificacion = notificacionRepository.save(notificacion);
            
            // 4. Enviar via SSE (si el usuario está conectado)
            sseEmitterService.enviarNotificacion(event.getIdUsuario(), notificacionMapper.toResponse(notificacion));
            
            // 📊 MÉTRICA: Incrementar contador de notificaciones enviadas
            Counter.builder(MetricsConfig.MetricNames.NOTIFICACIONES_ENVIADAS)
                    .description("Total de notificaciones enviadas exitosamente")
                    .tag(MetricsConfig.TagNames.TIPO_NOTIFICACION, event.getTipo().name())
                    .register(meterRegistry)
                    .increment();
            
            log.info("Notificación procesada exitosamente: id={}", notificacion.getId());
            
        } catch (Exception e) {
            log.error("Error al procesar notificación: {}", e.getMessage(), e);
            // No se propaga la excepción para evitar que un error en notificaciones
            // afecte la operación principal que publicó el evento
        }
    }
}
