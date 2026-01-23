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

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Captor
    private ArgumentCaptor<java.util.UUID> uuidCaptor;

    @Captor
    private ArgumentCaptor<LocalDate> dateCaptorStart;

    @Captor
    private ArgumentCaptor<LocalDate> dateCaptorEnd;

    private EspacioTrabajo espacio;

    @BeforeEach
    void setUp() {
        espacio = new EspacioTrabajo();
        espacio.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        espacio.setSaldo(123.45f);
    }

    // --------------------------------------------------
    // Tests for obtenerDashboardStats
    // --------------------------------------------------

    @Test
    void obtenerDashboardStats_espacioNoExiste_lanzaEntityNotFound() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> dashboardService.obtenerDashboardStats(espacio.getId()));

        verify(espacioRepository).findById(espacio.getId());
        verifyNoMoreInteractions(espacioRepository, dashboardRepository, cuotaCreditoRepository, tarjetaRepository, gastosIngresosMensualesRepository);
    }

    @Test
    void obtenerDashboardStats_gastosNoEncontrado_completaConCerosYCalculaOtrosCampos() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));

        // No gastosIngresosMensuales para el mes actual
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(any(java.util.UUID.class), anyInt(), anyInt()))
            .thenReturn(Optional.empty());

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenReturn(500.0f);
        when(dashboardRepository.findDistribucionGastos(eq(espacio.getId()), any(LocalDate.class))).thenReturn(new ArrayList<>());
        when(tarjetaRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(new ArrayList<>());
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(eq(espacio.getId()), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(espacio.getId());

        assertNotNull(stats);
        assertEquals(espacio.getSaldo(), stats.balanceTotal());
        assertEquals(0.0f, stats.gastosMensuales());
        assertEquals(500.0f, stats.deudaTotalPendiente());
        assertEquals(0.0f, stats.resumenMensual());
        assertEquals(12, stats.flujoMensual().size(), "Debe devolver 12 meses en flujo mensual");

        for (var mes : stats.flujoMensual()) {
            assertEquals(BigDecimal.ZERO, mes.getIngresos());
            assertEquals(BigDecimal.ZERO, mes.getGastos());
        }
    }

    @Test
    void obtenerDashboardStats_conDatos_mapeaValoresResumenYFlujoYDistribucion() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));

        // Simular que tenemos registro de gastos para dos meses dentro de los últimos 12
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<String> ultimosMeses = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            ultimosMeses.add(now.minusMonths(i).format(formatter));
        }

        // choose two months to return as existing records
        YearMonth ym1 = YearMonth.from(now).minusMonths(2);
        YearMonth ym2 = YearMonth.from(now).minusMonths(5);

        GastosIngresosMensuales g1 = GastosIngresosMensuales.builder()
                .anio(ym1.getYear())
                .mes(ym1.getMonthValue())
                .gastos(100f)
                .ingresos(400f)
                .espacioTrabajo(espacio)
                .build();

        GastosIngresosMensuales g2 = GastosIngresosMensuales.builder()
                .anio(ym2.getYear())
                .mes(ym2.getMonthValue())
                .gastos(50f)
                .ingresos(150f)
                .espacioTrabajo(espacio)
                .build();

        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(espacio.getId(), ultimosMeses))
                .thenReturn(List.of(g1, g2));

        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(eq(espacio.getId()), anyInt(), anyInt()))
                .thenReturn(Optional.of(g1));

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenReturn(250.5f);

        // Distribucion de gastos
        DistribucionGastoDTO distribMock = mock(DistribucionGastoDTO.class);
        when(dashboardRepository.findDistribucionGastos(eq(espacio.getId()), any(LocalDate.class))).thenReturn(List.of(distribMock));

        // Tarjeta y cuotas pendientes -> resumen mensual
        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(20L);
        tarjeta.setDiaCierre(5);
        tarjeta.setDiaVencimientoPago(10);
        when(tarjetaRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(List.of(tarjeta));
        CuotaCredito cuota1 = new CuotaCredito();
        cuota1.setMontoCuota(120.0f);
        CuotaCredito cuota2 = new CuotaCredito();
        cuota2.setMontoCuota(80.0f);

        // Capture the date range used for query to assert later
        when(cuotaCreditoRepository.findByTarjetaSinResumenEnRango(eq(20L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(cuota1, cuota2));

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(espacio.getId());

        assertNotNull(stats);
        assertEquals(espacio.getSaldo(), stats.balanceTotal());
        assertEquals(100f, stats.gastosMensuales()); // because findByEspacioTrabajo_IdAndAnioAndMes returned g1
        assertEquals(250.5f, stats.deudaTotalPendiente());
        assertEquals(200.0f, stats.resumenMensual(), "Resumen mensual es la suma de las cuotas devueltas");
        assertEquals(12, stats.flujoMensual().size());
        assertEquals(1, stats.distribucionGastos().size());

        // Verificar que la consulta de cuotas utilizó los parámetros de fecha calculados
        verify(cuotaCreditoRepository).findByTarjetaSinResumenEnRango(eq(20L), dateCaptorStart.capture(), dateCaptorEnd.capture());
        LocalDate start = dateCaptorStart.getValue();
        LocalDate end = dateCaptorEnd.getValue();

        // start debe ser posterior a la fecha de cierre y end debe ser la fecha de vencimiento calculada
        // Verificamos solo que start <= end y que ambas están en un rango razonable (próximo mes)
        assertTrue(!start.isAfter(end));
        assertTrue(!start.isBefore(LocalDate.now().minusMonths(1)));
    }

    @Test
    void obtenerDashboardStats_sinTarjetas_resumenMensualCero() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(eq(espacio.getId()), anyInt(), anyInt()))
                .thenReturn(Optional.of(GastosIngresosMensuales.builder().anio(LocalDate.now().getYear()).mes(LocalDate.now().getMonthValue()).gastos(10f).ingresos(20f).espacioTrabajo(espacio).build()));

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenReturn(0.0f);
        when(dashboardRepository.findDistribucionGastos(eq(espacio.getId()), any(LocalDate.class))).thenReturn(List.of());
        when(tarjetaRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(List.of());
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(eq(espacio.getId()), anyList())).thenReturn(new ArrayList<>());

        DashboardStatsDTO stats = dashboardService.obtenerDashboardStats(espacio.getId());

        assertNotNull(stats);
        assertEquals(0.0f, stats.resumenMensual());
    }

    @Test
    void obtenerDashboardStats_whenDebtCalcThrows_propagatesException() {
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(eq(espacio.getId()), anyInt(), anyInt()))
                .thenReturn(Optional.of(GastosIngresosMensuales.builder().anio(LocalDate.now().getYear()).mes(LocalDate.now().getMonthValue()).gastos(10f).ingresos(20f).espacioTrabajo(espacio).build()));

        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacio.getId())).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> dashboardService.obtenerDashboardStats(espacio.getId()));
        assertEquals("DB error", ex.getMessage());
    }

}

