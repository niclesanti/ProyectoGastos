package com.campito.backend.common.test;

import java.math.BigDecimal;

import com.campito.backend.common.dto.DistribucionGastoDTO;
import com.campito.backend.dashboard.domain.dto.*;

public final class DashboardTestDataFactory {

    private DashboardTestDataFactory() {}

    public static DashboardStatsDTO crearDashboardStats() {
        return new DashboardStatsDTO(
            new BigDecimal("1000.00"),
            new BigDecimal("500.00"),
            new BigDecimal("200.00"),
            new BigDecimal("300.00"),
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of(),
            java.util.List.of()
        );
    }

    public static DistribucionGastoDTO crearDistribucionGasto(String motivo, BigDecimal porcentaje) {
        return new DistribucionGastoDTO() {
            @Override
            public String getMotivo() { return motivo; }
            @Override
            public BigDecimal getPorcentaje() { return porcentaje; }
        };
    }
}
