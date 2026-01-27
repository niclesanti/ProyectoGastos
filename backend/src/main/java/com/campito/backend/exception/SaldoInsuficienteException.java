package com.campito.backend.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación financiera
 * que requiere fondos superiores al saldo disponible.
 * Se usa en transacciones de gasto, transferencias entre cuentas,
 * y operaciones que afectan el balance de cuentas bancarias o registros mensuales.
 */
public class SaldoInsuficienteException extends RuntimeException {
    
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
    
    public SaldoInsuficienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
