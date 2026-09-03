package com.campito.backend.dashboard.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;
import com.campito.backend.dashboard.domain.entity.ResumenFinanciero;
import com.campito.backend.dashboard.repository.GastosIngresosMensualesRepository;
import com.campito.backend.dashboard.repository.ResumenFinancieroRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DashboardReadModelServiceTest {

    @Mock
    private ResumenFinancieroRepository resumenFinancieroRepository;

    @Mock
    private GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    @InjectMocks
    private DashboardReadModelService dashboardReadModelService;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    @Test
    void obtenerResumenFinanciero_existe_devuelveResumen() {
        ResumenFinanciero expected = ResumenFinanciero.builder()
            .idEspacioTrabajo(idEspacio)
            .saldo(new BigDecimal("123.45"))
            .deudaTotal(new BigDecimal("500.00"))
            .build();
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(expected));

        ResumenFinanciero result = dashboardReadModelService.obtenerResumenFinanciero(idEspacio);

        assertNotNull(result);
        assertEquals(new BigDecimal("123.45"), result.getSaldo());
        assertEquals(new BigDecimal("500.00"), result.getDeudaTotal());
        verify(resumenFinancieroRepository).findById(idEspacio);
    }

    @Test
    void obtenerResumenFinanciero_noExiste_lanzaEntityNotFound() {
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
            () -> dashboardReadModelService.obtenerResumenFinanciero(idEspacio));

        assertTrue(ex.getMessage().contains(idEspacio.toString()));
        verify(resumenFinancieroRepository).findById(idEspacio);
    }

    @Test
    void obtenerRegistrosMensuales_devuelveRegistros() {
        List<String> meses = List.of("2026-06", "2026-07", "2026-08");
        GastosIngresosMensuales g1 = GastosIngresosMensuales.builder()
            .anio(2026).mes(8).gastos(new BigDecimal("100.00")).ingresos(new BigDecimal("200.00"))
            .comprasCredito(BigDecimal.ZERO).pagoResumen(BigDecimal.ZERO).idEspacioTrabajo(idEspacio).build();
        GastosIngresosMensuales g2 = GastosIngresosMensuales.builder()
            .anio(2026).mes(7).gastos(new BigDecimal("50.00")).ingresos(new BigDecimal("150.00"))
            .comprasCredito(BigDecimal.ZERO).pagoResumen(BigDecimal.ZERO).idEspacioTrabajo(idEspacio).build();
        when(gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(idEspacio, meses))
            .thenReturn(List.of(g1, g2));

        List<GastosIngresosMensuales> result = dashboardReadModelService.obtenerRegistrosMensuales(idEspacio, meses);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gastosIngresosMensualesRepository).findByEspacioTrabajoAndMeses(idEspacio, meses);
    }

    @Test
    void gastosMesActual_existe_devuelveGastos() {
        GastosIngresosMensuales registro = GastosIngresosMensuales.builder()
            .anio(2026).mes(8).gastos(new BigDecimal("350.00")).ingresos(new BigDecimal("500.00"))
            .comprasCredito(BigDecimal.ZERO).pagoResumen(BigDecimal.ZERO).idEspacioTrabajo(idEspacio).build();
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8))
            .thenReturn(Optional.of(registro));

        BigDecimal result = dashboardReadModelService.gastosMesActual(idEspacio, 2026, 8);

        assertEquals(0, new BigDecimal("350.00").compareTo(result));
        verify(gastosIngresosMensualesRepository).findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8);
    }

    @Test
    void gastosMesActual_noExiste_devuelveCero() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8))
            .thenReturn(Optional.empty());

        BigDecimal result = dashboardReadModelService.gastosMesActual(idEspacio, 2026, 8);

        assertEquals(0, BigDecimal.ZERO.compareTo(result));
        verify(gastosIngresosMensualesRepository).findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8);
    }
}
