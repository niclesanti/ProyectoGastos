package com.campito.backend.dashboard.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;
import com.campito.backend.dashboard.repository.GastosIngresosMensualesRepository;
import com.campito.backend.exception.SaldoInsuficienteException;
import com.campito.backend.shared.event.CompraCreditoEliminadaEvent;
import com.campito.backend.shared.event.CompraCreditoRegistradaEvent;
import com.campito.backend.shared.event.ResumenPagadoEvent;
import com.campito.backend.shared.event.TransaccionEliminadaEvent;
import com.campito.backend.shared.event.TransaccionRegistradaEvent;
import com.campito.backend.transacciones.domain.entity.TipoTransaccion;

@ExtendWith(MockitoExtension.class)
class DashboardEventListenerTest {

    @Mock
    private GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    @InjectMocks
    private DashboardEventListener listener;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private GastosIngresosMensuales registro(Integer anio, Integer mes, BigDecimal gastos, BigDecimal ingresos, BigDecimal compras, BigDecimal pagoResumen) {
        return GastosIngresosMensuales.builder()
            .anio(anio)
            .mes(mes)
            .gastos(gastos)
            .ingresos(ingresos)
            .comprasCredito(compras)
            .pagoResumen(pagoResumen)
            .idEspacioTrabajo(idEspacio)
            .build();
    }

    @Test
    void onTransaccionRegistrada_gasto_actualizaGastosDelMes() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        listener.onTransaccionRegistrada(new TransaccionRegistradaEvent(
            idEspacio, TipoTransaccion.GASTO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10)));

        assertEquals(0, new BigDecimal("100.00").compareTo(registro.getGastos()));
        verify(gastosIngresosMensualesRepository).save(registro);
    }

    @Test
    void onTransaccionRegistrada_ingreso_creaRegistroSiNoExiste() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(any(UUID.class), anyInt(), anyInt()))
            .thenReturn(Optional.empty());

        listener.onTransaccionRegistrada(new TransaccionRegistradaEvent(
            idEspacio, TipoTransaccion.INGRESO, new BigDecimal("50.00"), LocalDate.of(2026, 8, 10)));

        var captor = org.mockito.ArgumentCaptor.forClass(GastosIngresosMensuales.class);
        verify(gastosIngresosMensualesRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("50.00").compareTo(captor.getValue().getIngresos()));
        assertEquals(idEspacio, captor.getValue().getIdEspacioTrabajo());
    }

    @Test
    void onTransaccionEliminada_saldoSuficiente_revierteGastos() {
        GastosIngresosMensuales registro = registro(2026, 8, new BigDecimal("200.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        listener.onTransaccionEliminada(new TransaccionEliminadaEvent(
            idEspacio, TipoTransaccion.GASTO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10)));

        assertEquals(0, new BigDecimal("100.00").compareTo(registro.getGastos()));
        verify(gastosIngresosMensualesRepository).save(registro);
    }

    @Test
    void onTransaccionEliminada_montoMayorQueSaldoMensual_lanzaSaldoInsuficiente() {
        GastosIngresosMensuales registro = registro(2026, 8, new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        assertThrows(SaldoInsuficienteException.class, () -> listener.onTransaccionEliminada(
            new TransaccionEliminadaEvent(idEspacio, TipoTransaccion.GASTO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10))));
    }

    @Test
    void onCompraCreditoRegistrada_actualizaComprasCreditoDelMes() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500.00"), BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        listener.onCompraCreditoRegistrada(new CompraCreditoRegistradaEvent(
            idEspacio, new BigDecimal("300.00"), LocalDate.of(2026, 8, 15)));

        assertEquals(0, new BigDecimal("800.00").compareTo(registro.getComprasCredito()));
        verify(gastosIngresosMensualesRepository).save(registro);
    }

    @Test
    void onCompraCreditoEliminada_revierteComprasCreditoDelMes() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500.00"), BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        listener.onCompraCreditoEliminada(new CompraCreditoEliminadaEvent(
            idEspacio, new BigDecimal("200.00"), LocalDate.of(2026, 8, 15)));

        assertEquals(0, new BigDecimal("300.00").compareTo(registro.getComprasCredito()));
        verify(gastosIngresosMensualesRepository).save(registro);
    }

    @Test
    void onResumenPagado_anotaPagoResumenDelMesDelCiclo() {
        GastosIngresosMensuales registro = registro(2026, 2, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"));
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 2)).thenReturn(Optional.of(registro));

        listener.onResumenPagado(new ResumenPagadoEvent(idEspacio, new BigDecimal("300.00"), 2026, 2));

        assertEquals(0, new BigDecimal("400.00").compareTo(registro.getPagoResumen()));
        verify(gastosIngresosMensualesRepository).save(registro);
    }
}
