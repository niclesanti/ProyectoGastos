package com.campito.backend.service;

import com.campito.backend.dao.NotificacionRepository;
import com.campito.backend.dto.NotificacionDTOResponse;
import com.campito.backend.event.NotificacionEvent;
import com.campito.backend.mapper.NotificacionMapper;
import com.campito.backend.model.Notificacion;
import com.campito.backend.model.TipoNotificacion;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import com.campito.backend.config.MetricsConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de notificaciones.
 * 
 * Gestiona el acceso a notificaciones con validaciones de seguridad
 * para asegurar que los usuarios solo puedan acceder a sus propias notificaciones.
 */
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacionServiceImpl.class);
    
    private final NotificacionRepository notificacionRepository;
    private final NotificacionMapper notificacionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;  // Para métricas de Prometheus/Grafana
    
    /**
     * Obtiene las notificaciones de un usuario (máximo 50 más recientes).
     * 
     * @param idUsuario ID del usuario
     * @return Lista de notificaciones ordenadas por fecha descendente
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTOResponse> obtenerNotificacionesUsuario(UUID idUsuario) {
        logger.info("Obteniendo notificaciones para usuario: {}", idUsuario);
        List<Notificacion> notificaciones = notificacionRepository
                .findTop50ByUsuarioIdOrderByFechaCreacionDesc(idUsuario);
        return notificacionMapper.toResponseList(notificaciones);
    }
    
    /**
     * Cuenta las notificaciones no leídas de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Cantidad de notificaciones no leídas
     */
    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidas(UUID idUsuario) {
        logger.info("Contando notificaciones no leídas para usuario: {}", idUsuario);
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(idUsuario);
    }
    
    /**
     * Marca una notificación como leída.
     * 
     * @param idNotificacion ID de la notificación
     * @throws EntityNotFoundException si la notificación no existe
     */
    @Override
    @Transactional
    public void marcarComoLeida(Long idNotificacion) {
        logger.info("Marcando notificación {} como leída.", idNotificacion);
        
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> {
                    String msg = "Notificación con ID " + idNotificacion + " no encontrada.";
                    logger.warn(msg);
                    return new EntityNotFoundException(msg);
                });
        
        if (!notificacion.getLeida()) {
            notificacion.setLeida(true);
            notificacion.setFechaLeida(LocalDateTime.now());
            notificacionRepository.save(notificacion);
            logger.info("Notificación {} marcada como leída", idNotificacion);
            
            // 📊 MÉTRICA: Incrementar contador de notificaciones leídas
            Counter.builder(MetricsConfig.MetricNames.NOTIFICACIONES_LEIDAS)
                    .description("Total de notificaciones marcadas como leídas")
                    .tag(MetricsConfig.TagNames.TIPO_NOTIFICACION, notificacion.getTipo().name())
                    .register(meterRegistry)
                    .increment();
        }
    }
    
    /**
     * Marca todas las notificaciones de un usuario como leídas.
     * 
     * @param idUsuario ID del usuario
     */
    @Override
    @Transactional
    public void marcarTodasComoLeidas(UUID idUsuario) {
        logger.info("Marcando todas las notificaciones como leídas para usuario: {}", idUsuario);
        int actualizadas = notificacionRepository
                .marcarTodasComoLeidas(idUsuario, LocalDateTime.now());
        logger.info("Marcadas {} notificaciones como leídas para usuario {}", 
                   actualizadas, idUsuario);
    }
    
    /**
     * Elimina una notificación.
     * 
     * @param idNotificacion ID de la notificación
     * @throws EntityNotFoundException si la notificación no existe
     */
    @Override
    @Transactional
    public void eliminarNotificacion(Long idNotificacion) {
        logger.info("Eliminando notificación {}", idNotificacion);
        
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .orElseThrow(() -> {
                    String msg = "Notificación con ID " + idNotificacion + " no encontrada.";
                    logger.warn(msg);
                    return new EntityNotFoundException(msg);
                });
        
        notificacionRepository.delete(notificacion);
        logger.info("Notificación {} eliminada exitosamente", idNotificacion);
    }
    
    /**
     * Limpia notificaciones leídas con más de 3 días de antigüedad.
     * Este método es invocado por el scheduler diariamente.
     */
    @Override
    @Transactional
    public void limpiarNotificacionesLeidasAntiguas() {
        logger.info("Iniciando limpieza de notificaciones leídas antiguas");
        // Eliminar notificaciones leídas con más de 3 días
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(3);
        int eliminadas = notificacionRepository.eliminarNotificacionesLeidasAntiguas(fechaLimite);
        logger.info("Eliminadas {} notificaciones leídas antiguas (>3 días)", eliminadas);
    }
    
    /**
     * Limpia notificaciones no leídas con más de 15 días de antigüedad.
     * Este método es invocado por el scheduler mensualmente.
     */
    @Override
    @Transactional
    public void limpiarNotificacionesNoLeidasAntiguas() {
        logger.info("Iniciando limpieza de notificaciones no leídas antiguas");
        // Eliminar notificaciones no leídas con más de 15 días (probablemente ya no son relevantes)
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(15);
        int eliminadas = notificacionRepository.eliminarNotificacionesNoLeidasAntiguas(fechaLimite);
        logger.info("Eliminadas {} notificaciones no leídas antiguas (>15 días)", eliminadas);
    }
    
    /**
     * Envía una notificación de prueba al usuario especificado.
     * 
     * @param idUsuario ID del usuario destinatario
     * @param tipo Tipo de notificación
     * @param mensaje Mensaje personalizado (opcional)
     */
    @Override
    public void enviarNotificacionPrueba(UUID idUsuario, TipoNotificacion tipo, String mensaje) {
        logger.info("Enviando notificación de prueba a usuario: {} - Tipo: {}", idUsuario, tipo);
        
        String mensajeFinal = mensaje != null && !mensaje.isBlank()
            ? mensaje 
            : "🧪 Notificación de prueba enviada a las " + LocalDateTime.now().toLocalTime();
        
        try {
            // Publicar evento de notificación
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                idUsuario,
                tipo,
                mensajeFinal
            ));
            
            logger.info("Evento de notificación de prueba publicado exitosamente");
        } catch (Exception e) {
            logger.error("Error al publicar evento de notificación de prueba: {}", e.getMessage(), e);
            throw new RuntimeException("Error al enviar notificación de prueba", e);
        }
    }
}
