package com.campito.backend.dto;

import java.util.List;

public record DashboardStatsDTO(
    // KPIs
    Float balanceTotal,
    Float gastosMensuales,
    Float resumenMensual,
    Float deudaTotalPendiente,
    
    // Gráficos
    List<IngresosGastosMesDTO> flujoMensual,
    List<DistribucionGastoDTO> distribucionGastos
) {

} 
