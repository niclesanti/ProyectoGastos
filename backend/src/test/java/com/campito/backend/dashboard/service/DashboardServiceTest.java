package com.campito.backend.dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.dashboard.domain.dto.*;
import com.campito.backend.common.dto.*;
import com.campito.backend.dashboard.domain.entity.*;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DashboardReportesService dashboardReportesService;

    @Mock
    private DashboardReadModelService dashboardReadModelService;

    @Mock
    private Executor taskExecutor;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Captor
    private ArgumentCaptor<java.util.UUID> uuidCaptor;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = UUID.fromString("00000000-0000-0000-0000-000000000001");

        // Configure mock executor to run tasks synchronously on the calling thread
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
    }

    private ResumenFinanciero resumenFinanciero(BigDecimal saldo, BigDecimal deuda) {
        return ResumenFinanciero.builder()
                .idEspacioTrabajo(idEspacio)
                .saldo(saldo)
                .deudaTotal(deuda)
                .build();
    }

    // --------------------------------------------------
    // Tests for obtenerDashboardStats
    // --------------------------------------------------

    @Test
    void obtenerDashboardStats_espacioNoExiste_lanzaEntityNotFound() {
        when(dashboardReadModelService.obtenerResumenFinanciero(idEspacio))
            .thenThrow(new EntityNotFoundException("Espacio de trabajo no encontrado para ID: " + idEspacio));

        assertThrows(EntityNotFoundException.class, () -> dashboardService.obtenerDashboardStats(idEspacio));

        verify(dashboardReadModelService).obtenerResumenFinanciero(idEspacio);
    }

    @Test
    void obtenerDashboardStats_gastosNoEncontrado_completaConCerosYCalculaOtrosCampos() {
        when(dashboardReadModelService.obtenerResumenFinanciero(idEspacio)).thenReturn(resumenFinanciero(new BigDecimal("123.45"), new BigDecimal("500.00")));

        when(dashboardReadModelService.gastosMesActual(eq(idEspacio), anyInt(), anyInt()))
            .thenReturn(BigDecimal.ZERO);

        when(dashboardReportesService.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);
        when(dashboardReportesService.distribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(dashboardReportesService.distribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(dashboardReadModelService.obtenerRegistrosMensuales(eq(idEspacio), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(idEspacio);

        assertNotNull(stats);
        assertEquals(new BigDecimal("123.45"), stats.balanceTotal());
        assertEquals(0, new BigDecimal("0.00").compareTo(stats.gastosMensuales()));
        assertEquals(new BigDecimal("500.00"), stats.deudaTotalPendiente());
        assertEquals(0, new BigDecimal("0.00").compareTo(stats.resumenMensual()));
        assertEquals(12, stats.flujoMensual().size(), "Debe devolver 12 meses en flujo mensual");
        assertEquals(12, stats.flujoTarjetaMensual().size(), "Debe devolver 12 meses en flujo tarjeta mensual");

        for (var mes : stats.flujoMensual()) {
            assertEquals(0, new BigDecimal("0.00").compareTo(mes.getIngresos()));
            assertEquals(0, new BigDecimal("0.00").compareTo(mes.getGastos()));
        }
    }

    @Test
    void obtenerDashboardStats_conDatos_mapeaValoresResumenYFlujoYDistribucion() {
        when(dashboardReadModelService.obtenerResumenFinanciero(idEspacio)).thenReturn(resumenFinanciero(new BigDecimal("123.45"), new BigDecimal("250.50")));

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<String> ultimosMeses = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            ultimosMeses.add(now.minusMonths(i).format(formatter));
        }

        YearMonth ym1 = YearMonth.from(now).minusMonths(2);
        YearMonth ym2 = YearMonth.from(now).minusMonths(5);

        GastosIngresosMensuales g1 = GastosIngresosMensuales.builder()
                .anio(ym1.getYear())
                .mes(ym1.getMonthValue())
                .gastos(new BigDecimal("100.00"))
                .ingresos(new BigDecimal("400.00"))
                .idEspacioTrabajo(idEspacio)
                .build();

        GastosIngresosMensuales g2 = GastosIngresosMensuales.builder()
                .anio(ym2.getYear())
                .mes(ym2.getMonthValue())
                .gastos(new BigDecimal("50.00"))
                .ingresos(new BigDecimal("150.00"))
                .idEspacioTrabajo(idEspacio)
                .build();

        when(dashboardReadModelService.obtenerRegistrosMensuales(idEspacio, ultimosMeses))
                .thenReturn(List.of(g1, g2));

        when(dashboardReadModelService.gastosMesActual(eq(idEspacio), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("100.00"));

        when(dashboardReportesService.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(new BigDecimal("200.00"));

        DistribucionGastoDTO distribMock = mock(DistribucionGastoDTO.class);
        when(dashboardReportesService.distribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of(distribMock));
        when(dashboardReportesService.distribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(idEspacio);

        assertNotNull(stats);
        assertEquals(new BigDecimal("123.45"), stats.balanceTotal());
        assertEquals(new BigDecimal("100.00"), stats.gastosMensuales());
        assertEquals(new BigDecimal("250.50"), stats.deudaTotalPendiente());
        assertEquals(new BigDecimal("200.00"), stats.resumenMensual(), "Resumen mensual proviene del read modelo/cache de reportes");
        assertEquals(12, stats.flujoMensual().size());
        assertEquals(1, stats.distribucionGastos().size());

        verify(dashboardReportesService).resumenMensual(eq(idEspacio), any(LocalDate.class));
    }

    @Test
    void obtenerDashboardStats_sinTarjetas_resumenMensualCero() {
        when(dashboardReadModelService.obtenerResumenFinanciero(idEspacio)).thenReturn(resumenFinanciero(new BigDecimal("123.45"), BigDecimal.ZERO));
        when(dashboardReadModelService.gastosMesActual(eq(idEspacio), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("10.00"));

        when(dashboardReportesService.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);
        when(dashboardReportesService.distribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of());
        when(dashboardReportesService.distribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of());
        when(dashboardReadModelService.obtenerRegistrosMensuales(eq(idEspacio), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(idEspacio);

        assertNotNull(stats);
        assertEquals(0, new BigDecimal("0.00").compareTo(stats.resumenMensual()));
    }

    @Test
    void obtenerDashboardStats_whenDebtCalcFailsFromReadModel_noEsRuta() {
        // La deuda ya no se calcula; viene del read-model. Verificamos flujo normal.
        when(dashboardReadModelService.obtenerResumenFinanciero(idEspacio)).thenReturn(resumenFinanciero(new BigDecimal("123.45"), new BigDecimal("10.00")));
        when(dashboardReadModelService.gastosMesActual(eq(idEspacio), anyInt(), anyInt()))
                .thenReturn(new BigDecimal("10.00"));

        when(dashboardReportesService.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);
        when(dashboardReportesService.distribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of());
        when(dashboardReportesService.distribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of());
        when(dashboardReadModelService.obtenerRegistrosMensuales(eq(idEspacio), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(idEspacio);
        assertEquals(new BigDecimal("10.00"), stats.deudaTotalPendiente());
    }

}
