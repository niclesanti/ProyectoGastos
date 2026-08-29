package com.campito.backend.common.event;

import com.campito.backend.common.domain.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento publicado cuando se elimina una transacción.
 * 
 * Consumido de forma síncrona por el listener del dashboard para revertir
 * los gastos/ingresos del mes. Si el monto a revertir supera el saldo
 * mensual registrado, el listener lanza {@code SaldoInsuficienteException}
 * provocando el rollback de toda la operación.
 */
public record TransaccionEliminadaEvent(
    UUID idEspacioTrabajo,
    TipoTransaccion tipo,
    BigDecimal monto,
    LocalDate fecha
) {
}
