package com.campito.backend.dashboard.domain.dto;

import java.math.BigDecimal;

public interface IngresosGastosMesDTO {
    String getMes();
    BigDecimal getIngresos();
    BigDecimal getGastos();
}