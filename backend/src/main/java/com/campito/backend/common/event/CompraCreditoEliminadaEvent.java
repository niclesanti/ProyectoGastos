package com.campito.backend.common.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento publicado cuando se elimina una compra a crédito.
 * 
 * Consumido de forma síncrona por el listener del dashboard para revertir
 * las compras a crédito del mes correspondiente.
 */
public record CompraCreditoEliminadaEvent(
    UUID idEspacioTrabajo,
    BigDecimal montoTotal,
    LocalDate fechaCompra
) {
}
