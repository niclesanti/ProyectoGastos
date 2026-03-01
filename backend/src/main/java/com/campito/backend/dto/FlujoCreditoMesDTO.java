package com.campito.backend.dto;

import java.math.BigDecimal;

/**
 * DTO para representar el flujo mensual de tarjeta de crédito.
 * Contiene la suma de compras con crédito y los pagos de resúmenes por mes.
 */
public interface FlujoCreditoMesDTO {
    String getMes();
    BigDecimal getComprasCredito();
    BigDecimal getPagoResumen();
}
