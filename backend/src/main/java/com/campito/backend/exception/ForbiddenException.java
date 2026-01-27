package com.campito.backend.exception;

/**
 * Excepción lanzada cuando un usuario autenticado no tiene permisos 
 * suficientes para acceder a un recurso específico.
 * Se utiliza para validaciones de ownership y control de acceso.
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String mensaje) {
        super(mensaje);
    }
    
    public ForbiddenException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
