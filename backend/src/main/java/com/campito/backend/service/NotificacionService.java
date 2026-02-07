package com.campito.backend.service;

import com.campito.backend.dto.NotificacionDTOResponse;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de notificaciones del sistema.
 * 
 * Proporciona operaciones para consultar, marcar como leídas y eliminar
 * notificaciones, así como métodos de mantenimiento automático.
 */
public interface NotificacionService {
    
    /**
     * Obtiene las notificaciones de un usuario (máximo 50 más recientes).
     * 
     * @param idUsuario ID del usuario
     * @return Lista de notificaciones ordenadas por fecha descendente
     */
    List<NotificacionDTOResponse> obtenerNotificacionesUsuario(UUID idUsuario);
    
    /**
     * Cuenta las notificaciones no leídas de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Cantidad de notificaciones no leídas
     */
    Long contarNoLeidas(UUID idUsuario);
    
    /**
     * Marca una notificación como leída.
     * 
     * @param idNotificacion ID de la notificación
     * @throws EntityNotFoundException si la notificación no existe
     */
    void marcarComoLeida(Long idNotificacion);
    
    /**
     * Marca todas las notificaciones de un usuario como leídas.
     * 
     * @param idUsuario ID del usuario
     */
    void marcarTodasComoLeidas(UUID idUsuario);
    
    /**
     * Elimina una notificación.
     * 
     * @param idNotificacion ID de la notificación
     * @throws EntityNotFoundException si la notificación no existe
     */
    void eliminarNotificacion(Long idNotificacion);
    
    /**
     * Limpia notificaciones leídas con más de 10 días de antigüedad.
     * Este método es invocado por el scheduler diariamente.
     */
    void limpiarNotificacionesLeidasAntiguas();
    
    /**
     * Limpia notificaciones no leídas con más de 30 días de antigüedad.
     * Este método es invocado por el scheduler mensualmente.
     */
    void limpiarNotificacionesNoLeidasAntiguas();
}
