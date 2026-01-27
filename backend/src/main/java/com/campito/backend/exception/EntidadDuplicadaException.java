package com.campito.backend.exception;

/**
 * Excepción lanzada cuando se intenta crear una entidad que ya existe.
 * Se usa para validar la unicidad de nombres en entidades como 
 * MotivoTransaccion y ContactoTransferencia dentro de un espacio de trabajo.
 */
public class EntidadDuplicadaException extends RuntimeException {
    
    public EntidadDuplicadaException(String mensaje) {
        super(mensaje);
    }
    
    public EntidadDuplicadaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
