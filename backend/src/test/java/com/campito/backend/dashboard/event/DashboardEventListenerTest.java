package com.campito.backend.dashboard.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import com.campito.backend.dashboard.domain.entity.ResumenFinanciero;
import com.campito.backend.dashboard.repository.GastosIngresosMensualesRepository;
import com.campito.backend.dashboard.repository.ResumenFinancieroRepository;
import com.campito.backend.common.exception.SaldoInsuficienteException;
import com.campito.backend.common.event.CompraCreditoEliminadaEvent;
import com.campito.backend.common.event.CompraCreditoRegistradaEvent;
import com.campito.backend.common.event.ResumenPagadoEvent;
import com.campito.backend.common.event.SaldoActualizadoEvent;
import com.campito.backend.common.event.TransaccionEliminadaEvent;
import com.campito.backend.common.event.TransaccionRegistradaEvent;
import com.campito.backend.common.domain.TipoTransaccion;

@ExtendWith(MockitoExtension.class)
class DashboardEventListenerTest {

    @Mock
    private GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    @Mock
    private ResumenFinancieroRepository resumenFinancieroRepository;

    @InjectMocks
    private DashboardEventListener listener;

    private UUID idEspacio;

    @BeforeEach
    void setUp() {
        idEspacio = UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private ResumenFinanciero resumenFinanciero(BigDecimal saldo, BigDecimal deuda) {
        return ResumenFinanciero.builder()
            .idEspacioTrabajo(idEspacio)
            .saldo(saldo)
            .deudaTotal(deuda)
            .build();
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

        ResumenFinanciero rf = resumenFinanciero(BigDecimal.ZERO, BigDecimal.ZERO);
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        listener.onCompraCreditoRegistrada(new CompraCreditoRegistradaEvent(
            idEspacio, new BigDecimal("300.00"), new BigDecimal("99.99"), LocalDate.of(2026, 8, 15)));

        assertEquals(0, new BigDecimal("800.00").compareTo(registro.getComprasCredito()));
        verify(gastosIngresosMensualesRepository).save(registro);
        assertEquals(0, new BigDecimal("99.99").compareTo(rf.getDeudaTotal()), "La deuda usa la suma de cuotas redondeadas, no el monto total");
        verify(resumenFinancieroRepository).save(rf);
    }

    @Test
    void onCompraCreditoEliminada_revierteComprasCreditoDelMes() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500.00"), BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        ResumenFinanciero rf = resumenFinanciero(BigDecimal.ZERO, new BigDecimal("500.00"));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        listener.onCompraCreditoEliminada(new CompraCreditoEliminadaEvent(
            idEspacio, new BigDecimal("200.00"), new BigDecimal("99.99"), LocalDate.of(2026, 8, 15)));

        assertEquals(0, new BigDecimal("300.00").compareTo(registro.getComprasCredito()));
        verify(gastosIngresosMensualesRepository).save(registro);
        assertEquals(0, new BigDecimal("400.01").compareTo(rf.getDeudaTotal()));
        verify(resumenFinancieroRepository).save(rf);
    }

    @Test
    void onResumenPagado_anotaPagoResumenDelMesDelCiclo() {
        GastosIngresosMensuales registro = registro(2026, 2, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100.00"));
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 2)).thenReturn(Optional.of(registro));

        ResumenFinanciero rf = resumenFinanciero(BigDecimal.ZERO, new BigDecimal("500.00"));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        listener.onResumenPagado(new ResumenPagadoEvent(idEspacio, new BigDecimal("300.00"), 2026, 2));

        assertEquals(0, new BigDecimal("400.00").compareTo(registro.getPagoResumen()));
        verify(gastosIngresosMensualesRepository).save(registro);
        assertEquals(0, new BigDecimal("200.00").compareTo(rf.getDeudaTotal()), "Pagar un resumen reduce la deuda por el monto pagado");
        verify(resumenFinancieroRepository).save(rf);
    }

    // --------------------------------------------------
    // Tests del read-model financiero (saldo y deuda)
    // --------------------------------------------------

    @Test
    void onSaldoActualizado_existente_actualizaSaldo() {
        ResumenFinanciero rf = resumenFinanciero(new BigDecimal("10.00"), new BigDecimal("50.00"));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        listener.onSaldoActualizado(new SaldoActualizadoEvent(idEspacio, new BigDecimal("123.45")));

        assertEquals(0, new BigDecimal("123.45").compareTo(rf.getSaldo()));
        verify(resumenFinancieroRepository).save(rf);
    }

    @Test
    void onSaldoActualizado_inexistente_creaFilaConDeudaCero() {
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.empty());

        listener.onSaldoActualizado(new SaldoActualizadoEvent(idEspacio, new BigDecimal("0.00")));

        var captor = org.mockito.ArgumentCaptor.forClass(ResumenFinanciero.class);
        verify(resumenFinancieroRepository).save(captor.capture());
        assertEquals(idEspacio, captor.getValue().getIdEspacioTrabajo());
        assertEquals(0, new BigDecimal("0.00").compareTo(captor.getValue().getSaldo()));
        assertEquals(0, new BigDecimal("0.00").compareTo(captor.getValue().getDeudaTotal()));
    }

    @Test
    void onCompraCreditoRegistrada_invarianteRedondeo_deudaEsSumaDeCuotasNoTotal() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        ResumenFinanciero rf = resumenFinanciero(BigDecimal.ZERO, BigDecimal.ZERO);
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        // Compra de 100.00 en 3 cuotas => montoCuota = 33.33, suma = 99.99 (NO 100.00)
        listener.onCompraCreditoRegistrada(new CompraCreditoRegistradaEvent(
            idEspacio, new BigDecimal("100.00"), new BigDecimal("99.99"), LocalDate.of(2026, 8, 15)));

        assertEquals(0, new BigDecimal("99.99").compareTo(rf.getDeudaTotal()));
    }

    // --------------------------------------------------
    // Tests para cubrir paths no testeados
    // --------------------------------------------------

    @Test
    void onTransaccionEliminada_ingreso_saldoSuficiente_revierteIngresos() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, new BigDecimal("300.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        listener.onTransaccionEliminada(new TransaccionEliminadaEvent(
            idEspacio, TipoTransaccion.INGRESO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10)));

        assertEquals(0, new BigDecimal("200.00").compareTo(registro.getIngresos()));
        verify(gastosIngresosMensualesRepository).save(registro);
    }

    @Test
    void onTransaccionEliminada_ingreso_montoMayorQueIngresos_lanzaSaldoInsuficiente() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));

        assertThrows(SaldoInsuficienteException.class, () -> listener.onTransaccionEliminada(
            new TransaccionEliminadaEvent(idEspacio, TipoTransaccion.INGRESO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10))));
    }

    @Test
    void onTransaccionEliminada_registroNoEncontrado_lanzaEntityNotFoundException() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> listener.onTransaccionEliminada(
            new TransaccionEliminadaEvent(idEspacio, TipoTransaccion.GASTO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10))));
    }

    @Test
    void onCompraCreditoRegistrada_registroNoExiste_creaRegistro() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(any(UUID.class), anyInt(), anyInt()))
            .thenReturn(Optional.empty());

        ResumenFinanciero rf = resumenFinanciero(BigDecimal.ZERO, BigDecimal.ZERO);
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        listener.onCompraCreditoRegistrada(new CompraCreditoRegistradaEvent(
            idEspacio, new BigDecimal("300.00"), new BigDecimal("99.99"), LocalDate.of(2026, 8, 15)));

        var captor = org.mockito.ArgumentCaptor.forClass(GastosIngresosMensuales.class);
        verify(gastosIngresosMensualesRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("300.00").compareTo(captor.getValue().getComprasCredito()));
        assertEquals(0, new BigDecimal("99.99").compareTo(rf.getDeudaTotal()));
    }

    @Test
    void onCompraCreditoRegistrada_resumenFinancieroNoExiste_creaConDeudaCero() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.empty());

        listener.onCompraCreditoRegistrada(new CompraCreditoRegistradaEvent(
            idEspacio, new BigDecimal("200.00"), new BigDecimal("100.00"), LocalDate.of(2026, 8, 15)));

        var captor = org.mockito.ArgumentCaptor.forClass(ResumenFinanciero.class);
        verify(resumenFinancieroRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("100.00").compareTo(captor.getValue().getDeudaTotal()));
    }

    @Test
    void onCompraCreditoEliminada_registroNoEncontrado_lanzaEntityNotFoundException() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> listener.onCompraCreditoEliminada(
            new CompraCreditoEliminadaEvent(idEspacio, new BigDecimal("200.00"), new BigDecimal("99.99"), LocalDate.of(2026, 8, 15))));
    }

    @Test
    void onCompraCreditoEliminada_resumenFinancieroNoExiste_decrementarDeudaEsNoOp() {
        GastosIngresosMensuales registro = registro(2026, 8, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500.00"), BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 8)).thenReturn(Optional.of(registro));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> listener.onCompraCreditoEliminada(
            new CompraCreditoEliminadaEvent(idEspacio, new BigDecimal("200.00"), new BigDecimal("99.99"), LocalDate.of(2026, 8, 15))));

        assertEquals(0, new BigDecimal("300.00").compareTo(registro.getComprasCredito()));
    }

    @Test
    void onResumenPagado_registroNoExiste_creaRegistro() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 2)).thenReturn(Optional.empty());

        ResumenFinanciero rf = resumenFinanciero(BigDecimal.ZERO, new BigDecimal("500.00"));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.of(rf));

        listener.onResumenPagado(new ResumenPagadoEvent(idEspacio, new BigDecimal("300.00"), 2026, 2));

        var captor = org.mockito.ArgumentCaptor.forClass(GastosIngresosMensuales.class);
        verify(gastosIngresosMensualesRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("300.00").compareTo(captor.getValue().getPagoResumen()));
        assertEquals(0, new BigDecimal("200.00").compareTo(rf.getDeudaTotal()));
    }

    @Test
    void onResumenPagado_resumenFinancieroNoExiste_decrementarDeudaEsNoOp() {
        GastosIngresosMensuales registro = registro(2026, 2, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(idEspacio, 2026, 2)).thenReturn(Optional.of(registro));
        when(resumenFinancieroRepository.findById(idEspacio)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> listener.onResumenPagado(
            new ResumenPagadoEvent(idEspacio, new BigDecimal("300.00"), 2026, 2)));

        assertEquals(0, new BigDecimal("300.00").compareTo(registro.getPagoResumen()));
    }

    @Test
    void onTransaccionRegistrada_gasto_registroNoExiste_creaRegistro() {
        when(gastosIngresosMensualesRepository.findByIdEspacioTrabajoAndAnioAndMes(any(UUID.class), anyInt(), anyInt()))
            .thenReturn(Optional.empty());

        listener.onTransaccionRegistrada(new TransaccionRegistradaEvent(
            idEspacio, TipoTransaccion.GASTO, new BigDecimal("100.00"), LocalDate.of(2026, 8, 10)));

        var captor = org.mockito.ArgumentCaptor.forClass(GastosIngresosMensuales.class);
        verify(gastosIngresosMensualesRepository).save(captor.capture());
        assertEquals(0, new BigDecimal("100.00").compareTo(captor.getValue().getGastos()));
        assertEquals(idEspacio, captor.getValue().getIdEspacioTrabajo());
    }
}
