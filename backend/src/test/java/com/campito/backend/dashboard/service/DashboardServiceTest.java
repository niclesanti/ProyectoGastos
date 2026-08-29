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

import com.campito.backend.transacciones.api.CuotasCreditoApi;
import com.campito.backend.transacciones.api.ReportesTransaccionesApi;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;
import com.campito.backend.dashboard.repository.*;
import com.campito.backend.dashboard.domain.dto.*;
import com.campito.backend.shared.dto.*;
import com.campito.backend.dashboard.domain.entity.*;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EspacioTrabajoApi espacioTrabajoApi;

    @Mock
    private CuotasCreditoApi cuotasCreditoApi;

    @Mock
    private ReportesTransaccionesApi reportesTransaccionesApi;

    @Mock
    private GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

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

    // --------------------------------------------------
    // Tests for obtenerDashboardStats
    // --------------------------------------------------

    @Test
    void obtenerDashboardStats_espacioNoExiste_lanzaEntityNotFound() {
        when(espacioTrabajoApi.obtenerSaldo(idEspacio))
            .thenThrow(new EntityNotFoundException("Espacio de trabajo con ID " + idEspacio + " no encontrado"));

        assertThrows(EntityNotFoundException.class, () -> dashboardService.obtenerDashboardStats(idEspacio));

        verify(espacioTrabajoApi).obtenerSaldo(idEspacio);
    }

    @Test
    void obtenerDashboardStats_gastosNoEncontrado_completaConCerosYCalculaOtrosCampos() {
        when(espacioTrabajoApi.obtenerSaldo(idEspacio)).thenReturn(new BigDecimal("123.45"));

        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(any(java.util.UUID.class), anyInt(), anyInt()))
            .thenReturn(Optional.empty());

        when(cuotasCreditoApi.calcularDeudaTotalPendiente(idEspacio)).thenReturn(new BigDecimal("500.00"));
        when(cuotasCreditoApi.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);
        when(reportesTransaccionesApi.findDistribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(reportesTransaccionesApi.findDistribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(eq(idEspacio), anyList())).thenReturn(new ArrayList<>());

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
        when(espacioTrabajoApi.obtenerSaldo(idEspacio)).thenReturn(new BigDecimal("123.45"));

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

        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(idEspacio, ultimosMeses))
                .thenReturn(List.of(g1, g2));

        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(eq(idEspacio), anyInt(), anyInt()))
                .thenReturn(Optional.of(g1));

        when(cuotasCreditoApi.calcularDeudaTotalPendiente(idEspacio)).thenReturn(new BigDecimal("250.50"));
        when(cuotasCreditoApi.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(new BigDecimal("200.00"));

        DistribucionGastoDTO distribMock = mock(DistribucionGastoDTO.class);
        when(reportesTransaccionesApi.findDistribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of(distribMock));
        when(reportesTransaccionesApi.findDistribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(idEspacio);

        assertNotNull(stats);
        assertEquals(new BigDecimal("123.45"), stats.balanceTotal());
        assertEquals(new BigDecimal("100.00"), stats.gastosMensuales());
        assertEquals(new BigDecimal("250.50"), stats.deudaTotalPendiente());
        assertEquals(new BigDecimal("200.00"), stats.resumenMensual(), "Resumen mensual proviene del facade de cuotas");
        assertEquals(12, stats.flujoMensual().size());
        assertEquals(1, stats.distribucionGastos().size());

        verify(cuotasCreditoApi).resumenMensual(eq(idEspacio), any(LocalDate.class));
    }

    @Test
    void obtenerDashboardStats_sinTarjetas_resumenMensualCero() {
        when(espacioTrabajoApi.obtenerSaldo(idEspacio)).thenReturn(new BigDecimal("123.45"));
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(eq(idEspacio), anyInt(), anyInt()))
                .thenReturn(Optional.of(GastosIngresosMensuales.builder().anio(LocalDate.now().getYear()).mes(LocalDate.now().getMonthValue()).gastos(new BigDecimal("10.00")).ingresos(new BigDecimal("20.00")).idEspacioTrabajo(idEspacio).build()));

        when(cuotasCreditoApi.calcularDeudaTotalPendiente(idEspacio)).thenReturn(BigDecimal.ZERO);
        when(cuotasCreditoApi.resumenMensual(eq(idEspacio), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);
        when(reportesTransaccionesApi.findDistribucionGastos(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of());
        when(reportesTransaccionesApi.findDistribucionComprasCredito(eq(idEspacio), any(LocalDate.class))).thenReturn(List.of());
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(eq(idEspacio), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(idEspacio);

        assertNotNull(stats);
        assertEquals(0, new BigDecimal("0.00").compareTo(stats.resumenMensual()));
    }

    @Test
    void obtenerDashboardStats_whenDebtCalcThrows_propagatesException() {
        when(espacioTrabajoApi.obtenerSaldo(idEspacio)).thenReturn(new BigDecimal("123.45"));
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(eq(idEspacio), anyInt(), anyInt()))
                .thenReturn(Optional.of(GastosIngresosMensuales.builder().anio(LocalDate.now().getYear()).mes(LocalDate.now().getMonthValue()).gastos(new BigDecimal("10.00")).ingresos(new BigDecimal("20.00")).idEspacioTrabajo(idEspacio).build()));

        when(cuotasCreditoApi.calcularDeudaTotalPendiente(idEspacio)).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dashboardService.obtenerDashboardStats(idEspacio));
        assertEquals("DB error", ex.getMessage());
    }

}
