package com.campito.backend.service;

import lombok.extern.slf4j.Slf4j;
import com.campito.backend.notificaciones.domain.dto.NotificacionDTOResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
@Slf4j
public class SseEmitterServiceImpl implements SseEmitterService {
    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1 hora
    
    /**
     * Mapa concurrente para almacenar las conexiones SSE activas por usuario.
     */
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    private final AtomicInteger sseConexionesActivasGauge;  // Gauge inyectado desde MetricsConfig
    
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
            log.info("SSE completado para usuario: {}", idUsuario);
            emitters.remove(idUsuario);
            sseConexionesActivasGauge.set(emitters.size());  // 📊 MÉTRICA: Actualizar gauge
        });
        
        // Handler de timeout
        emitter.onTimeout(() -> {
            log.info("SSE timeout para usuario: {}", idUsuario);
            emitters.remove(idUsuario);
            sseConexionesActivasGauge.set(emitters.size());  // 📊 MÉTRICA: Actualizar gauge
        });
        
        // Handler de error
        emitter.onError((e) -> {
            log.error("Error en SSE para usuario {}: {}", idUsuario, e.getMessage());
            emitters.remove(idUsuario);
            sseConexionesActivasGauge.set(emitters.size());  // 📊 MÉTRICA: Actualizar gauge
        });
        
        emitters.put(idUsuario, emitter);
        sseConexionesActivasGauge.set(emitters.size());  // 📊 MÉTRICA: Actualizar gauge
        log.info("SSE emitter creado para usuario: {}", idUsuario);
        
        // Enviar evento inicial de confirmación de conexión
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Conexión SSE establecida exitosamente"));
            log.info("Evento 'connected' enviado a usuario: {}", idUsuario);
        } catch (IOException e) {
            log.error("Error al enviar evento 'connected' a usuario {}: {}", idUsuario, e.getMessage());
            emitters.remove(idUsuario);
            throw new RuntimeException("Error al establecer conexión SSE", e);
        }
        
        return emitter;
    }
    
    /**
     * Envía una notificación a un usuario via SSE si está conectado.
     * 
     * @param idUsuario ID del usuario destinatario
     * @param notificacion DTO de la notificación a enviar
     */
    @Override
    public void enviarNotificacion(UUID idUsuario, NotificacionDTOResponse notificacion) {
        SseEmitter emitter = emitters.get(idUsuario);
        
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notificacion));
                log.info("Notificación enviada via SSE a usuario: {}", idUsuario);
            } catch (IOException e) {
                log.error("Error al enviar notificación via SSE: {}", e.getMessage());
                emitters.remove(idUsuario);
            }
        } else {
            log.debug("Usuario {} no tiene conexión SSE activa", idUsuario);
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
