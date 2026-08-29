package com.campito.backend.common.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Evento publicado cuando se paga un resumen de tarjeta.
 * 
 * Consumido de forma síncrona por el listener del dashboard para anotar
 * el pago del resumen en el mes del ciclo al que corresponde dicho resumen.
 */
public record ResumenPagadoEvent(
    UUID idEspacioTrabajo,
    BigDecimal montoTotal,
    Integer anio,
    Integer mes
) {
}
