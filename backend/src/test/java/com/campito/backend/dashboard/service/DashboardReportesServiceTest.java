package com.campito.backend.dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.common.dto.DistribucionGastoDTO;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.common.test.DashboardTestDataFactory;
import com.campito.backend.transacciones.api.CuotasCreditoApi;
import com.campito.backend.transacciones.api.ReportesTransaccionesApi;

@ExtendWith(MockitoExtension.class)
class DashboardReportesServiceTest {

    @Mock
    private ReportesTransaccionesApi reportesTransaccionesApi;

    @Mock
    private CuotasCreditoApi cuotasCreditoApi;

    @InjectMocks
    private DashboardReportesService dashboardReportesService;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = TestIds.ESPACIO_TRABAJO_ID;
    }

    @Test
    void distribucionGastos_delegaAReportesApi() {
        List<DistribucionGastoDTO> expected = List.of(
            DashboardTestDataFactory.crearDistribucionGasto("Alimentación", new BigDecimal("50"))
        );
        LocalDate fechaLimite = LocalDate.now().minusMonths(6);

        when(reportesTransaccionesApi.findDistribucionGastos(idEspacio, fechaLimite)).thenReturn(expected);

        List<DistribucionGastoDTO> result = dashboardReportesService.distribucionGastos(idEspacio, fechaLimite);

        assertEquals(expected, result);
        verify(reportesTransaccionesApi).findDistribucionGastos(idEspacio, fechaLimite);
    }

    @Test
    void distribucionComprasCredito_delegaAReportesApi() {
        List<DistribucionGastoDTO> expected = List.of(
            DashboardTestDataFactory.crearDistribucionGasto("Electrónica", new BigDecimal("30"))
        );
        LocalDate fechaLimite = LocalDate.now().minusMonths(6);

        when(reportesTransaccionesApi.findDistribucionComprasCredito(idEspacio, fechaLimite)).thenReturn(expected);

        List<DistribucionGastoDTO> result = dashboardReportesService.distribucionComprasCredito(idEspacio, fechaLimite);

        assertEquals(expected, result);
    }

    @Test
    void resumenMensual_delegaACuotasCreditoApi() {
        LocalDate fechaRef = LocalDate.now();
        when(cuotasCreditoApi.resumenMensual(idEspacio, fechaRef)).thenReturn(new BigDecimal("500.00"));

        BigDecimal result = dashboardReportesService.resumenMensual(idEspacio, fechaRef);

        assertEquals(new BigDecimal("500.00"), result);
    }
}
