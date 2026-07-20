package com.campito.backend.service;

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

import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.DashboardRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.GastosIngresosMensualesRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dto.DashboardStatsDTO;
import com.campito.backend.dto.DistribucionGastoDTO;
import com.campito.backend.model.CuotaCredito;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.GastosIngresosMensuales;
import com.campito.backend.model.Tarjeta;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EspacioTrabajoRepository espacioRepository;

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private CuotaCreditoRepository cuotaCreditoRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    @Mock
    private Executor taskExecutor;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Captor
    private ArgumentCaptor<java.util.UUID> uuidCaptor;

    private EspacioTrabajo espacio;

    @BeforeEach
    void setUp() {
        espacio = new EspacioTrabajo();
        espacio.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        espacio.setSaldo(new BigDecimal("123.45"));

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
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> dashboardService.obtenerDashboardStats(espacio.getId()));

        verify(espacioRepository).findById(espacio.getId());
    }

    @Test
    void obtenerDashboardStats_gastosNoEncontrado_completaConCerosYCalculaOtrosCampos() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));

        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(any(java.util.UUID.class), anyInt(), anyInt()))
            .thenReturn(Optional.empty());

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenReturn(new BigDecimal("500.00"));
        when(dashboardRepository.findDistribucionGastos(eq(espacio.getId()), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(dashboardRepository.findDistribucionComprasCredito(eq(espacio.getId()), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(tarjetaRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(new ArrayList<>());
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(eq(espacio.getId()), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(espacio.getId());

        assertNotNull(stats);
        assertEquals(espacio.getSaldo(), stats.balanceTotal());
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
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));

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
                .espacioTrabajo(espacio)
                .build();

        GastosIngresosMensuales g2 = GastosIngresosMensuales.builder()
                .anio(ym2.getYear())
                .mes(ym2.getMonthValue())
                .gastos(new BigDecimal("50.00"))
                .ingresos(new BigDecimal("150.00"))
                .espacioTrabajo(espacio)
                .build();

        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(espacio.getId(), ultimosMeses))
                .thenReturn(List.of(g1, g2));

        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(eq(espacio.getId()), anyInt(), anyInt()))
                .thenReturn(Optional.of(g1));

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenReturn(new BigDecimal("250.50"));

        DistribucionGastoDTO distribMock = mock(DistribucionGastoDTO.class);
        when(dashboardRepository.findDistribucionGastos(eq(espacio.getId()), any(LocalDate.class))).thenReturn(List.of(distribMock));
        when(dashboardRepository.findDistribucionComprasCredito(eq(espacio.getId()), any(LocalDate.class))).thenReturn(new ArrayList<>());

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(20L);
        tarjeta.setDiaCierre(5);
        tarjeta.setDiaVencimientoPago(10);
        tarjeta.setEspacioTrabajo(espacio);
        when(tarjetaRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(List.of(tarjeta));
        
        var compraCredito = new com.campito.backend.model.CompraCredito();
        compraCredito.setTarjeta(tarjeta);
        
        YearMonth ym = YearMonth.from(LocalDate.now());
        int diaAjustadoCierre = Math.min(5, ym.lengthOfMonth());
        LocalDate fechaCierre = ym.atDay(diaAjustadoCierre);
        if (!fechaCierre.isAfter(LocalDate.now())) {
            YearMonth siguiente = ym.plusMonths(1);
            diaAjustadoCierre = Math.min(5, siguiente.lengthOfMonth());
            fechaCierre = siguiente.atDay(diaAjustadoCierre);
        }
        LocalDate fechaInicio = fechaCierre.plusDays(1);
        YearMonth mesActual = YearMonth.from(fechaCierre);
        YearMonth mesSiguiente = mesActual.plusMonths(1);
        int diaAjustadoVenc = Math.min(10, mesSiguiente.lengthOfMonth());
        LocalDate fechaFin = mesSiguiente.atDay(diaAjustadoVenc);
        
        CuotaCredito cuota1 = new CuotaCredito();
        cuota1.setMontoCuota(new BigDecimal("120.00"));
        cuota1.setCompraCredito(compraCredito);
        cuota1.setFechaVencimiento(fechaInicio.plusDays(1));
        
        CuotaCredito cuota2 = new CuotaCredito();
        cuota2.setMontoCuota(new BigDecimal("80.00"));
        cuota2.setCompraCredito(compraCredito);
        cuota2.setFechaVencimiento(fechaInicio.plusDays(5));

        when(cuotaCreditoRepository.findByEspacioTrabajoSinResumenEnRango(eq(espacio.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(cuota1, cuota2));

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(espacio.getId());

        assertNotNull(stats);
        assertEquals(espacio.getSaldo(), stats.balanceTotal());
        assertEquals(new BigDecimal("100.00"), stats.gastosMensuales());
        assertEquals(new BigDecimal("250.50"), stats.deudaTotalPendiente());
        assertEquals(new BigDecimal("200.00"), stats.resumenMensual(), "Resumen mensual es la suma de las cuotas devueltas");
        assertEquals(12, stats.flujoMensual().size());
        assertEquals(1, stats.distribucionGastos().size());

        verify(cuotaCreditoRepository).findByEspacioTrabajoSinResumenEnRango(eq(espacio.getId()), any(LocalDate.class), any(LocalDate.class));
        verify(cuotaCreditoRepository, never()).findByTarjetaSinResumenEnRango(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void obtenerDashboardStats_sinTarjetas_resumenMensualCero() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(eq(espacio.getId()), anyInt(), anyInt()))
                .thenReturn(Optional.of(GastosIngresosMensuales.builder().anio(LocalDate.now().getYear()).mes(LocalDate.now().getMonthValue()).gastos(new BigDecimal("10.00")).ingresos(new BigDecimal("20.00")).espacioTrabajo(espacio).build()));

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenReturn(BigDecimal.ZERO);
        when(dashboardRepository.findDistribucionGastos(eq(espacio.getId()), any(LocalDate.class))).thenReturn(List.of());
        when(dashboardRepository.findDistribucionComprasCredito(eq(espacio.getId()), any(LocalDate.class))).thenReturn(List.of());
        when(tarjetaRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(List.of());
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(eq(espacio.getId()), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(espacio.getId());

        assertNotNull(stats);
        assertEquals(0, new BigDecimal("0.00").compareTo(stats.resumenMensual()));
    }

    @Test
    void obtenerDashboardStats_whenDebtCalcThrows_propagatesException() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(eq(espacio.getId()), anyInt(), anyInt()))
                .thenReturn(Optional.of(GastosIngresosMensuales.builder().anio(LocalDate.now().getYear()).mes(LocalDate.now().getMonthValue()).gastos(new BigDecimal("10.00")).ingresos(new BigDecimal("20.00")).espacioTrabajo(espacio).build()));

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dashboardService.obtenerDashboardStats(espacio.getId()));
        assertEquals("DB error", ex.getMessage());
    }

}
