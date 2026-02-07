package com.campito.backend.dto;

import com.campito.backend.model.TipoNotificacion;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para notificaciones.
 * 
 * Contiene toda la información necesaria para mostrar una notificación
 * en el frontend sin exponer detalles internos de la entidad.
 * 
 * Nota: El ID Long se serializa como number en JSON.
 */
public record NotificacionDTOResponse(
    Long id,
    TipoNotificacion tipo,
    String mensaje,
    Boolean leida,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaLeida
) {

}
