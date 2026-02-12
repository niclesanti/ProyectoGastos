package com.campito.backend.event;

import com.campito.backend.dao.NotificacionRepository;
import com.campito.backend.dao.UsuarioRepository;
import com.campito.backend.model.Notificacion;
import com.campito.backend.model.Usuario;
import com.campito.backend.service.SseEmitterService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class NotificacionEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacionEventListener.class);
    
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
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
            logger.info("Procesando notificación: tipo={}, usuario={}", 
                       event.getTipo(), event.getIdUsuario());
            
            // 1. Buscar usuario
            Usuario usuario = usuarioRepository.findById(event.getIdUsuario()).orElseThrow(() -> {
                String mensaje = "Usuario con ID " + event.getIdUsuario() + " no encontrado";
                logger.warn(mensaje);
                return new EntityNotFoundException(mensaje);
            });
            
            // 2. Crear notificación
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(usuario);
            notificacion.setTipo(event.getTipo());
            notificacion.setMensaje(event.getMensaje());
            
            // 3. Guardar en BD
            notificacion = notificacionRepository.save(notificacion);
            
            // 4. Enviar via SSE (si el usuario está conectado)
            sseEmitterService.enviarNotificacion(event.getIdUsuario(), notificacion);
            
            // 📊 MÉTRICA: Incrementar contador de notificaciones enviadas
            Counter.builder(MetricsConfig.MetricNames.NOTIFICACIONES_ENVIADAS)
                    .description("Total de notificaciones enviadas exitosamente")
                    .tag(MetricsConfig.TagNames.TIPO_NOTIFICACION, event.getTipo().name())
                    .register(meterRegistry)
                    .increment();
            
            logger.info("Notificación procesada exitosamente: id={}", notificacion.getId());
            
        } catch (Exception e) {
            logger.error("Error al procesar notificación: {}", e.getMessage(), e);
            // No se propaga la excepción para evitar que un error en notificaciones
            // afecte la operación principal que publicó el evento
        }
    }
}
