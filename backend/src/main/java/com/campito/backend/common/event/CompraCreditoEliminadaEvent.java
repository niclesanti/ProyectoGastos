package com.campito.backend.common.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento publicado cuando se elimina una compra a crédito.
 * 
 * Consumido de forma síncrona por el listener del dashboard para:
 * - revertir las compras a crédito del mes correspondiente (montoTotal)
 * - decrementar la deuda total (sumaCuotas) del read-model.
 *
 * {@code sumaCuotas} es la suma de los montos de cuota reales (redondeados
 * con MoneyUtils) = montoCuota * cantidadCuotas, calculada ANTES de borrar
 * las cuotas para mantener la invariante de la deuda.
 */
public record CompraCreditoEliminadaEvent(
    UUID idEspacioTrabajo,
    BigDecimal montoTotal,
    BigDecimal sumaCuotas,
    LocalDate fechaCompra
) {
}
