package com.campito.backend.common.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado cuando cambia el saldo de un espacio de trabajo.
 *
 * Lleva el saldo COMPLETO (no un delta) para que el consumidor pueda
 * actualizar el read-model de forma idempotente.
 *
 * Consumido de forma síncrona por el listener del dashboard para mantener
 * el saldo desnormalizado en {@code dashboard.resumen_financiero}.
 */
public record SaldoActualizadoEvent(
    UUID idEspacioTrabajo,
    BigDecimal nuevoSaldo
) {
}
