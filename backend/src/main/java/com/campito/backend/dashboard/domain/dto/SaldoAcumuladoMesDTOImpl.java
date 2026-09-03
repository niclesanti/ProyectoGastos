package com.campito.backend.dashboard.domain.dto;

import java.math.BigDecimal;

public class SaldoAcumuladoMesDTOImpl implements SaldoAcumuladoMesDTO {
    private String mes;
    private BigDecimal saldoAcumulado;

    public SaldoAcumuladoMesDTOImpl(String mes, BigDecimal saldoAcumulado) {
        this.mes = mes;
        this.saldoAcumulado = saldoAcumulado;
    }

    @Override
    public String getMes() {
        return mes;
    }

    @Override
    public BigDecimal getSaldoAcumulado() {
        return saldoAcumulado;
    }
}
