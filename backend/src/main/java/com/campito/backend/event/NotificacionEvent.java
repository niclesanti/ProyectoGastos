package com.campito.backend.event;

import com.campito.backend.model.TipoNotificacion;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Evento publicado cuando se genera una notificación en el sistema.
 * 
 * Este evento es capturado por {@link NotificacionEventListener} de forma
 * asíncrona para crear la notificación en la base de datos y enviarla
 * en tiempo real via SSE si el usuario está conectado.
 * 
 * Arquitectura Dirigida por Eventos:
 * - Desacopla la lógica de negocio del sistema de notificaciones
 * - Permite procesamiento asíncrono sin bloquear el flujo principal
 * - Facilita agregar nuevos listeners sin modificar código existente
 */
@Getter
public class NotificacionEvent extends ApplicationEvent {
    
    /**
     * ID del usuario destinatario de la notificación.
     */
    private final UUID idUsuario;
    
    /**
     * Tipo de notificación a generar.
     */
    private final TipoNotificacion tipo;
    
    /**
     * Mensaje descriptivo de la notificación.
     */
    private final String mensaje;
    
    /**
     * Constructor del evento de notificación.
     * 
     * @param source Objeto que origina el evento (típicamente el servicio que lo publica)
     * @param idUsuario ID del usuario destinatario
     * @param tipo Tipo de notificación
     * @param mensaje Mensaje descriptivo
     */
    public NotificacionEvent(Object source, UUID idUsuario, TipoNotificacion tipo, String mensaje) {
        super(source);
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.mensaje = mensaje;
    }
}
