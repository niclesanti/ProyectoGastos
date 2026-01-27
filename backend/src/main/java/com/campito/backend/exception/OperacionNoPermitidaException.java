package com.campito.backend.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación que viola
 * las reglas de negocio de la aplicación.
 * Se usa para operaciones como:
 * - Eliminar una compra a crédito con cuotas pagadas
 * - Eliminar una tarjeta con compras asociadas
 * - Cualquier otra operación que comprometa la integridad de datos ya procesados
 */
public class OperacionNoPermitidaException extends RuntimeException {
    
    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
    
    public OperacionNoPermitidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
