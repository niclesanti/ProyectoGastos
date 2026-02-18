package com.campito.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsDTO(
    // KPIs
    BigDecimal balanceTotal,
    BigDecimal gastosMensuales,
    BigDecimal resumenMensual,
    BigDecimal deudaTotalPendiente,
    
    // Gráficos
    List<IngresosGastosMesDTO> flujoMensual,
    List<DistribucionGastoDTO> distribucionGastos
) {

} 
