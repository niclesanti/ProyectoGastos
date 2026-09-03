package com.campito.backend.common.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento publicado cuando se registra una compra a crédito.
 * 
 * Consumido de forma síncrona por el listener del dashboard para:
 * - actualizar las compras a crédito del mes correspondiente (montoTotal)
 * - incrementar la deuda total (sumaCuotas) del read-model.
 *
 * {@code sumaCuotas} es la suma de los montos de cuota reales (redondeados
 * con MoneyUtils) = montoCuota * cantidadCuotas. NO es montoTotal, porque
 * el redondeo HALF_UP de cada cuota hace que la suma difiera del monto total.
 */
public record CompraCreditoRegistradaEvent(
    UUID idEspacioTrabajo,
    BigDecimal montoTotal,
    BigDecimal sumaCuotas,
    LocalDate fechaCompra
) {
}
