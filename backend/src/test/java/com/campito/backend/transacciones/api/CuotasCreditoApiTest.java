package com.campito.backend.transacciones.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.common.test.TestIds;
import com.campito.backend.transacciones.domain.entity.CompraCredito;
import com.campito.backend.transacciones.domain.entity.CuotaCredito;
import com.campito.backend.transacciones.domain.entity.Tarjeta;
import com.campito.backend.transacciones.repository.CuotaCreditoRepository;

@ExtendWith(MockitoExtension.class)
class CuotasCreditoApiTest {

    @Mock
    private CuotaCreditoRepository cuotaCreditoRepository;

    @Mock
    private TarjetaApi tarjetaApi;

    @InjectMocks
    private CuotasCreditoApiImpl cuotasCreditoApi;

    private UUID espacioId;

    @BeforeEach
    void setUp() {
        espacioId = TestIds.ESPACIO_TRABAJO_ID;
    }

    @Test
    void calcularDeudaTotalPendiente_retornaMonto() {
        when(cuotaCreditoRepository.calcularDeudaTotalPendiente(espacioId))
            .thenReturn(new BigDecimal("1500.00"));

        BigDecimal result = cuotasCreditoApi.calcularDeudaTotalPendiente(espacioId);
        assertEquals(new BigDecimal("1500.00"), result);
    }

    @Test
    void resumenMensual_sinTarjetas_retornaCero() {
        when(tarjetaApi.listarParaCierre(espacioId)).thenReturn(Collections.emptyList());

        BigDecimal result = cuotasCreditoApi.resumenMensual(espacioId, LocalDate.now());
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void resumenMensual_conTarjetasSinCuotas_retornaCero() {
        TarjetaApi.TarjetaResumen tarjeta = new TarjetaApi.TarjetaResumen(10L, 25, 5);
        when(tarjetaApi.listarParaCierre(espacioId)).thenReturn(List.of(tarjeta));
        when(cuotaCreditoRepository.findByEspacioTrabajoSinResumenEnRango(
            eq(espacioId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        BigDecimal result = cuotasCreditoApi.resumenMensual(espacioId, LocalDate.now());
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void resumenMensual_conTarjetasYCuotas_retornaSuma() {
        TarjetaApi.TarjetaResumen tarjeta = new TarjetaApi.TarjetaResumen(10L, 25, 5);
        when(tarjetaApi.listarParaCierre(espacioId)).thenReturn(List.of(tarjeta));

        Tarjeta tarjetaEntity = new Tarjeta();
        tarjetaEntity.setId(10L);

        CompraCredito compra = new CompraCredito();
        compra.setId(1L);
        compra.setTarjeta(tarjetaEntity);

        CuotaCredito cuota1 = new CuotaCredito();
        cuota1.setId(1L);
        cuota1.setMontoCuota(new BigDecimal("500.00"));
        cuota1.setFechaVencimiento(LocalDate.now().plusMonths(1));
        cuota1.setCompraCredito(compra);

        CuotaCredito cuota2 = new CuotaCredito();
        cuota2.setId(2L);
        cuota2.setMontoCuota(new BigDecimal("500.00"));
        cuota2.setFechaVencimiento(LocalDate.now().plusMonths(1));
        cuota2.setCompraCredito(compra);

        when(cuotaCreditoRepository.findByEspacioTrabajoSinResumenEnRango(
            eq(espacioId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(cuota1, cuota2));

        BigDecimal result = cuotasCreditoApi.resumenMensual(espacioId, LocalDate.now());
        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    void resumenMensual_conMultiplesTarjetas_retornaSumaTotal() {
        TarjetaApi.TarjetaResumen tarjeta1 = new TarjetaApi.TarjetaResumen(10L, 25, 5);
        TarjetaApi.TarjetaResumen tarjeta2 = new TarjetaApi.TarjetaResumen(20L, 15, 10);
        when(tarjetaApi.listarParaCierre(espacioId)).thenReturn(List.of(tarjeta1, tarjeta2));

        Tarjeta tarjetaEntity1 = new Tarjeta();
        tarjetaEntity1.setId(10L);
        Tarjeta tarjetaEntity2 = new Tarjeta();
        tarjetaEntity2.setId(20L);

        CompraCredito compra1 = new CompraCredito();
        compra1.setId(1L);
        compra1.setTarjeta(tarjetaEntity1);
        CompraCredito compra2 = new CompraCredito();
        compra2.setId(2L);
        compra2.setTarjeta(tarjetaEntity2);

        CuotaCredito cuota1 = new CuotaCredito();
        cuota1.setId(1L);
        cuota1.setMontoCuota(new BigDecimal("300.00"));
        cuota1.setFechaVencimiento(LocalDate.now().plusMonths(1));
        cuota1.setCompraCredito(compra1);

        CuotaCredito cuota2 = new CuotaCredito();
        cuota2.setId(2L);
        cuota2.setMontoCuota(new BigDecimal("200.00"));
        cuota2.setFechaVencimiento(LocalDate.now().plusMonths(1));
        cuota2.setCompraCredito(compra2);

        when(cuotaCreditoRepository.findByEspacioTrabajoSinResumenEnRango(
            eq(espacioId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(cuota1, cuota2));

        BigDecimal result = cuotasCreditoApi.resumenMensual(espacioId, LocalDate.now());
        assertEquals(new BigDecimal("500.00"), result);
    }

    @Test
    void resumenMensual_diaCierreMayorQueLongitudMes_seAjusta() {
        // Tarjeta with diaCierre=31 but month is February (28 days) — tests Math.min adjustment
        TarjetaApi.TarjetaResumen tarjeta = new TarjetaApi.TarjetaResumen(10L, 31, 10);
        when(tarjetaApi.listarParaCierre(espacioId)).thenReturn(List.of(tarjeta));
        when(cuotaCreditoRepository.findByEspacioTrabajoSinResumenEnRango(
            eq(espacioId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        BigDecimal result = cuotasCreditoApi.resumenMensual(espacioId, LocalDate.of(2026, 2, 15));
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void resumenMensual_diaVencimientoMayorQueLongitudMes_siguienteMes_seAjusta() {
        // Tests that diaVencimiento is clamped to length of next month
        TarjetaApi.TarjetaResumen tarjeta = new TarjetaApi.TarjetaResumen(10L, 25, 31);
        when(tarjetaApi.listarParaCierre(espacioId)).thenReturn(List.of(tarjeta));
        when(cuotaCreditoRepository.findByEspacioTrabajoSinResumenEnRango(
            eq(espacioId), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        BigDecimal result = cuotasCreditoApi.resumenMensual(espacioId, LocalDate.of(2026, 1, 15));
        assertEquals(BigDecimal.ZERO, result);
    }
}
