package com.campito.backend.exception;

/**
 * Excepción lanzada cuando un usuario no está autenticado correctamente.
 * Se utiliza cuando se intenta acceder a un recurso sin credenciales válidas.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }
    
    public UnauthorizedException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
