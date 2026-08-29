package com.campito.backend.dashboard.domain.dto;

import java.math.BigDecimal;

public class IngresosGastosMesDTOImpl implements IngresosGastosMesDTO {
    private String mes;
    private BigDecimal ingresos;
    private BigDecimal gastos;

    public IngresosGastosMesDTOImpl(String mes, BigDecimal ingresos, BigDecimal gastos) {
        this.mes = mes;
        this.ingresos = ingresos;
        this.gastos = gastos;
    }

    @Override
    public String getMes() {
        return mes;
    }

    @Override
    public BigDecimal getIngresos() {
        return ingresos;
    }

    @Override
    public BigDecimal getGastos() {
        return gastos;
    }
}
