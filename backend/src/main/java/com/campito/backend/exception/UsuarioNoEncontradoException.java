package com.campito.backend.exception;

/**
 * Excepción lanzada cuando un usuario no se encuentra en la base de datos.
 * Proporciona mensajes amigables para el usuario final.
 */
public class UsuarioNoEncontradoException extends RuntimeException {
    
    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
