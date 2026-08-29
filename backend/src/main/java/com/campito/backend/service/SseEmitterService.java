package com.campito.backend.service;

import com.campito.backend.notificaciones.domain.entity.Notificacion;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
    public SseEmitter crearEmitter(UUID idUsuario);
    public void enviarNotificacion(UUID idUsuario, Notificacion notificacion);
    public int getActiveConnections();
}
