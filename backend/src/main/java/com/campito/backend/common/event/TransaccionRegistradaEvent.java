package com.campito.backend.common.event;

import com.campito.backend.common.domain.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento publicado cuando se registra una transacción.
 * 
 * Consumido de forma síncrona por el listener del dashboard para
 * actualizar los gastos/ingresos del mes correspondiente en la misma
 * transacción del servicio productor.
 */
public record TransaccionRegistradaEvent(
    UUID idEspacioTrabajo,
    TipoTransaccion tipo,
    BigDecimal monto,
    LocalDate fecha
) {
}
