package com.campito.backend.model;

/**
 * Enum que define los diferentes tipos de notificaciones del sistema.
 * 
 * Cada tipo tiene una descripción asociada para facilitar la comprensión
 * del contexto de la notificación.
 */
public enum TipoNotificacion {
    
    /**
     * Notificación enviada cuando se cierra automáticamente un resumen de tarjeta de crédito.
     */
    CIERRE_TARJETA("Cierre de tarjeta de crédito"),
    
    /**
     * Notificación enviada cuando un resumen de tarjeta está próximo a vencer.
     */
    VENCIMIENTO_RESUMEN("Resumen próximo a vencer"),
    
    /**
     * Notificación enviada cuando un usuario es invitado a unirse a un espacio de trabajo.
     */
    INVITACION_ESPACIO("Invitación a espacio de trabajo"),
    
    /**
     * Notificación enviada cuando se agrega un nuevo miembro a un espacio de trabajo.
     */
    MIEMBRO_AGREGADO("Nuevo miembro agregado"),
    
    /**
     * Notificación general del sistema.
     */
    SISTEMA("Mensaje del sistema");
    
    private final String descripcion;
    
    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
