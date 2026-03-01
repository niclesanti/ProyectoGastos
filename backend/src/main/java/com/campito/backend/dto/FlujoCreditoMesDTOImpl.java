package com.campito.backend.dto;

import java.math.BigDecimal;

/**
 * Implementación del DTO para el flujo mensual de tarjeta de crédito.
 */
public class FlujoCreditoMesDTOImpl implements FlujoCreditoMesDTO {

    private final String mes;
    private final BigDecimal comprasCredito;
    private final BigDecimal pagoResumen;

    public FlujoCreditoMesDTOImpl(String mes, BigDecimal comprasCredito, BigDecimal pagoResumen) {
        this.mes = mes;
        this.comprasCredito = comprasCredito;
        this.pagoResumen = pagoResumen;
    }

    @Override
    public String getMes() {
        return mes;
    }

    @Override
    public BigDecimal getComprasCredito() {
        return comprasCredito;
    }

    @Override
    public BigDecimal getPagoResumen() {
        return pagoResumen;
    }
}
