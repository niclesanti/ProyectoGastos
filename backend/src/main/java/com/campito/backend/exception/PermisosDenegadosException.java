package com.campito.backend.exception;

/**
 * Excepción lanzada cuando un usuario intenta realizar una acción
 * para la cual no tiene los permisos necesarios.
 * Se usa principalmente en operaciones que requieren privilegios
 * de administrador dentro de un espacio de trabajo.
 */
public class PermisosDenegadosException extends RuntimeException {
    
    public PermisosDenegadosException(String mensaje) {
        super(mensaje);
    }
    
    public PermisosDenegadosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
