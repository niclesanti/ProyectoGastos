package com.campito.backend.service;

import com.campito.backend.dto.NotificacionDTOResponse;
import com.campito.backend.mapper.NotificacionMapper;
import com.campito.backend.model.Notificacion;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar conexiones Server-Sent Events (SSE).
 * 
 * Permite enviar notificaciones en tiempo real a los usuarios conectados
 * mediante una conexión HTTP persistente unidireccional (servidor → cliente).
 * 
 * Ventajas de SSE sobre WebSocket:
 * - Más simple de implementar (HTTP estándar)
 * - Reconexión automática del navegador
 * - Menor consumo de recursos
 * - Suficiente para notificaciones (no necesitamos bidireccionalidad)
 */
@Service
@RequiredArgsConstructor
public class SseEmitterServiceImpl implements SseEmitterService {
    
    private static final Logger logger = LoggerFactory.getLogger(SseEmitterService.class);
    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1 hora
    
    /**
     * Mapa concurrente para almacenar las conexiones SSE activas por usuario.
     */
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    private final NotificacionMapper notificacionMapper;
    
    /**
     * Crea un nuevo emitter SSE para un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Emitter configurado con timeout y handlers
     */
    @Override
    public SseEmitter crearEmitter(UUID idUsuario) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // Handler de finalización normal
        emitter.onCompletion(() -> {
            logger.info("SSE completado para usuario: {}", idUsuario);
            emitters.remove(idUsuario);
        });
        
        // Handler de timeout
        emitter.onTimeout(() -> {
            logger.info("SSE timeout para usuario: {}", idUsuario);
            emitters.remove(idUsuario);
        });
        
        // Handler de error
        emitter.onError((e) -> {
            logger.error("Error en SSE para usuario {}: {}", idUsuario, e.getMessage());
            emitters.remove(idUsuario);
        });
        
        emitters.put(idUsuario, emitter);
        logger.info("SSE emitter creado para usuario: {}", idUsuario);
        
        return emitter;
    }
    
    /**
     * Envía una notificación a un usuario via SSE si está conectado.
     * 
     * @param idUsuario ID del usuario destinatario
     * @param notificacion Notificación a enviar
     */
    @Override
    public void enviarNotificacion(UUID idUsuario, Notificacion notificacion) {
        SseEmitter emitter = emitters.get(idUsuario);
        
        if (emitter != null) {
            try {
                NotificacionDTOResponse dto = notificacionMapper.toResponse(notificacion);
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(dto));
                logger.info("Notificación enviada via SSE a usuario: {}", idUsuario);
            } catch (IOException e) {
                logger.error("Error al enviar notificación via SSE: {}", e.getMessage());
                emitters.remove(idUsuario);
            }
        } else {
            logger.debug("Usuario {} no tiene conexión SSE activa", idUsuario);
        }
    }
    
    /**
     * Obtiene la cantidad de conexiones SSE activas.
     * 
     * @return Cantidad de usuarios conectados via SSE
     */
    @Override
    public int getActiveConnections() {
        return emitters.size();
    }
}
