package com.campito.backend.transacciones.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Facade de lectura del módulo de transacciones para el cálculo de deuda
 * pendiente y resumen mensual de tarjetas de crédito de un espacio de trabajo.
 */
public interface CuotasCreditoApi {

    BigDecimal calcularDeudaTotalPendiente(UUID idEspacio);

    /**
     * Calcula el resumen mensual total (suma de cuotas que entrarán en los
     * próximos resúmenes) según el período específico de cada tarjeta del espacio.
     *
     * @param idEspacio         ID del espacio de trabajo
     * @param fechaReferencia   Fecha de referencia para el cálculo de los ciclos
     * @return Total del resumen mensual pendiente
     */
    BigDecimal resumenMensual(UUID idEspacio, LocalDate fechaReferencia);
}
