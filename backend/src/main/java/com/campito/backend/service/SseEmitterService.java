package com.campito.backend.service;

import com.campito.backend.notificaciones.domain.dto.NotificacionDTOResponse;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
    public SseEmitter crearEmitter(UUID idUsuario);
    public void enviarNotificacion(UUID idUsuario, NotificacionDTOResponse notificacion);
    public int getActiveConnections();
}
