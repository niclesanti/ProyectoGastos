package com.campito.backend.dashboard.domain.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsDTO(
    // KPIs
    BigDecimal balanceTotal,
    BigDecimal gastosMensuales,
    BigDecimal resumenMensual,
    BigDecimal deudaTotalPendiente,
    
    // Charts
    List<IngresosGastosMesDTO> flujoMensual,
    List<DistribucionGastoDTO> distribucionGastos,
    List<FlujoCreditoMesDTO> flujoTarjetaMensual,
    List<DistribucionGastoDTO> distribucionComprasCredito) {

}