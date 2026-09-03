package com.campito.backend.transacciones.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.campito.backend.common.domain.TipoTransaccion;
import com.campito.backend.transacciones.repository.*;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;
import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.common.event.CompraCreditoEliminadaEvent;
import com.campito.backend.common.event.CompraCreditoRegistradaEvent;
import com.campito.backend.common.event.ResumenPagadoEvent;
import com.campito.backend.transacciones.domain.dto.*;
import com.campito.backend.transacciones.domain.entity.*;
import com.campito.backend.transacciones.mapper.*;


import jakarta.persistence.EntityNotFoundException;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
public class CompraCreditoServiceTest {

    @Mock
    private CompraCreditoRepository compraCreditoRepository;

    @Mock
    private EspacioTrabajoApi espacioTrabajoApi;

    @Mock
    private MotivoTransaccionRepository motivoRepository;

    @Mock
    private ContactoTransferenciaRepository contactoRepository;

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private CuotaCreditoRepository cuotaCreditoRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private ResumenRepository resumenRepository;

    @Mock
    private CompraCreditoMapper compraCreditoMapper;

    @Mock
    private TarjetaMapper tarjetaMapper;

    @Mock
    private CuotaCreditoMapper cuotaCreditoMapper;

    @Mock
    private ResumenMapper resumenMapper;

    @Mock
    private TransaccionService transaccionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CompraCreditoServiceImpl compraCreditoService;

    private EspacioTrabajo espacio;
    private Tarjeta tarjeta;
    private CompraCredito compraCreditoEntity;

    @BeforeEach
    void setUp() {
        // Usar SimpleMeterRegistry real para evitar problemas con mocks
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        compraCreditoService = new CompraCreditoServiceImpl(
            compraCreditoRepository,
            espacioTrabajoApi,
            motivoRepository,
            contactoRepository,
            cuentaBancariaRepository,
            cuotaCreditoRepository,
            tarjetaRepository,
            transaccionRepository,
            resumenRepository,
            compraCreditoMapper,
            tarjetaMapper,
            cuotaCreditoMapper,
            resumenMapper,
            transaccionService,
            eventPublisher,
            meterRegistry
        );
        espacio = new EspacioTrabajo();
        espacio.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        espacio.setNombre("Mi Espacio");

        tarjeta = new Tarjeta();
        tarjeta.setId(10L);
        tarjeta.setDiaCierre(25);
        tarjeta.setDiaVencimientoPago(5);
        tarjeta.setIdEspacioTrabajo(espacio.getId());

        compraCreditoEntity = new CompraCredito();
        compraCreditoEntity.setId(100L);
        compraCreditoEntity.setTarjeta(tarjeta);
        compraCreditoEntity.setMontoTotal(new BigDecimal("1000.00"));
        compraCreditoEntity.setCantidadCuotas(5);
        compraCreditoEntity.setFechaCompra(LocalDate.of(2025, Month.JANUARY, 1));
        MotivoTransaccion motivoDefault = new MotivoTransaccion();
        motivoDefault.setId(1L);
        compraCreditoEntity.setMotivo(motivoDefault);
        compraCreditoEntity.setIdEspacioTrabajo(espacio.getId());

        // Mapper behavior
        lenient().when(compraCreditoMapper.toEntity(any(CompraCreditoDTORequest.class))).thenAnswer(invocation -> {
            CompraCreditoDTORequest dto = invocation.getArgument(0);
            CompraCredito c = new CompraCredito();
            c.setMontoTotal(dto.montoTotal());
            c.setCantidadCuotas(dto.cantidadCuotas());
            c.setFechaCompra(dto.fechaCompra());
            return c;
        });

        lenient().when(compraCreditoMapper.toResponse(any(CompraCredito.class))).thenAnswer(invocation -> {
            CompraCredito c = invocation.getArgument(0);
            return new CompraCreditoDTOResponse(
                c.getId(),
                c.getFechaCompra() != null ? c.getFechaCompra() : LocalDate.now(),
                c.getMontoTotal(),
                c.getCantidadCuotas(),
                0,
                c.getDescripcion(),
                "Aud",
                c.getFechaCreacion() != null ? c.getFechaCreacion() : LocalDate.now().atStartOfDay(),
                c.getIdEspacioTrabajo() != null ? c.getIdEspacioTrabajo() : espacio.getId(),
                c.getNombreEspacioTrabajo() != null ? c.getNombreEspacioTrabajo() : "esp",
                c.getMotivo() != null ? c.getMotivo().getId() : 1L,
                c.getMotivo() != null ? c.getMotivo().getMotivo() : "mot",
                c.getComercio() != null ? c.getComercio().getId() : null,
                c.getComercio() != null ? c.getComercio().getNombre() : null,
                c.getTarjeta() != null ? c.getTarjeta().getId() : 10L,
                c.getTarjeta() != null ? c.getTarjeta().getNumeroTarjeta() : "num",
                c.getTarjeta() != null ? c.getTarjeta().getEntidadFinanciera() : "ent",
                c.getTarjeta() != null ? c.getTarjeta().getRedDePago() : "red"
            );
        });

        lenient().when(cuotaCreditoMapper.toResponse(any(CuotaCredito.class))).thenAnswer(invocation -> {
            CuotaCredito cuota = invocation.getArgument(0);
            return new CuotaCreditoDTOResponse(
                cuota.getId(),
                cuota.getNumeroCuota(),
                cuota.getFechaVencimiento(),
                cuota.getMontoCuota(),
                cuota.isPagada(),
                cuota.getCompraCredito() != null ? cuota.getCompraCredito().getId() : null,
                cuota.getResumenAsociado() != null ? cuota.getResumenAsociado().getId() : null
            );
        });
    }

    // ---------------------------------------------------------
    // Tests para registrarCompraCredito
    // ---------------------------------------------------------

    @Test
    void registrarCompraCredito_espacioNoExiste_lanzaEntityNotFound() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), new BigDecimal("100.00"), 2, "desc", "Aud", espacio.getId(), 1L, null, 1L);
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarCompraCredito(dto));
        verify(compraCreditoRepository, never()).save(any());
    }

    @Test
    void registrarCompraCredito_creaCuotasSiCantidadValida_yGuardaCompra() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.of(2025, Month.JULY, 20), new BigDecimal("1000.00"), 3, "desc", "Aud", espacio.getId(), 1L, null, 10L);
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        when(espacioTrabajoApi.obtenerNombre(espacio.getId())).thenReturn("Mi Espacio");
        MotivoTransaccion motivoConId = new MotivoTransaccion();
        motivoConId.setId(1L);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoConId));
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(tarjetaRepository.findById(10L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });
        when(compraCreditoRepository.save(any(CompraCredito.class))).thenAnswer(inv -> {
            CompraCredito c = inv.getArgument(0);
            c.setId(123L);
            return c;
        });

        compraCreditoService.registrarCompraCredito(dto);

        verify(compraCreditoRepository, times(1)).save(any(CompraCredito.class));
        // Crear cuotas debería invocar save en cuotaCreditoRepository tantas veces como cuotas
        verify(cuotaCreditoRepository, times(3)).save(any(CuotaCredito.class));
        verify(eventPublisher).publishEvent(any(CompraCreditoRegistradaEvent.class));
    }

    @Test
    void registrarCompraCredito_conComercioOpcional_asignaComercio() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.of(2025, Month.JUNE, 10), new BigDecimal("500.00"), 2, "desc", "Aud", espacio.getId(), 1L, 99L, 10L);

        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        when(espacioTrabajoApi.obtenerNombre(espacio.getId())).thenReturn("Mi Espacio");
        MotivoTransaccion motivoConId2 = new MotivoTransaccion();
        motivoConId2.setId(1L);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoConId2));
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(tarjetaRepository.findById(10L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });
        ContactoTransferencia comercio = new ContactoTransferencia();
        comercio.setId(99L);
        when(contactoRepository.findById(99L)).thenReturn(Optional.of(comercio));
        when(contactoRepository.save(any(ContactoTransferencia.class))).thenAnswer(inv -> {
            ContactoTransferencia c = inv.getArgument(0);
            c.setId(99L);
            return c;
        });
        when(compraCreditoRepository.save(any(CompraCredito.class))).thenAnswer(inv -> {
            CompraCredito c = inv.getArgument(0);
            c.setId(555L);
            return c;
        });

        CompraCreditoDTOResponse resp = compraCreditoService.registrarCompraCredito(dto);
        assertNotNull(resp);
        verify(contactoRepository, times(1)).findById(99L);
    }

    @Test
    void registrarCompraCredito_cantidadCuotasCero_noCreaCuotas() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), new BigDecimal("1000.00"), 0, "desc", "Aud", espacio.getId(), 1L, null, 10L);
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        when(espacioTrabajoApi.obtenerNombre(espacio.getId())).thenReturn("Mi Espacio");
        MotivoTransaccion motivoConId3 = new MotivoTransaccion();
        motivoConId3.setId(1L);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoConId3));
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(tarjetaRepository.findById(10L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });
        when(compraCreditoRepository.save(any(CompraCredito.class))).thenAnswer(inv -> {
            CompraCredito c = inv.getArgument(0);
            c.setId(999L);
            return c;
        });

        compraCreditoService.registrarCompraCredito(dto);

        verify(cuotaCreditoRepository, never()).save(any(CuotaCredito.class));
        verify(eventPublisher).publishEvent(any(CompraCreditoRegistradaEvent.class));
    }

    // ---------------------------------------------------------
    // Tests para registrarTarjeta
    // ---------------------------------------------------------

    @Test
    void registrarTarjeta_espacioNoExiste_lanzaEntityNotFound() {
        var req = new TarjetaDTORequest("1234", "Entidad", "VISA", 1, 5, espacio.getId());
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarTarjeta(req));
    }

    @Test
    void registrarTarjeta_exitoso_guardaYRetorna() {
        var req = new TarjetaDTORequest("1234", "Entidad", "VISA", 1, 5, espacio.getId());
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        when(tarjetaMapper.toEntity(any())).thenAnswer(inv -> {
            TarjetaDTORequest r = inv.getArgument(0);
            Tarjeta t = new Tarjeta();
            t.setNumeroTarjeta(r.numeroTarjeta());
            t.setEntidadFinanciera(r.entidadFinanciera());
            t.setRedDePago(r.redDePago());
            t.setDiaCierre(r.diaCierre());
            t.setDiaVencimientoPago(r.diaVencimientoPago());
            t.setIdEspacioTrabajo(espacio.getId());
            t.setId(555L);
            return t;
        });
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tarjetaMapper.toResponse(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta t = inv.getArgument(0);
            return new TarjetaDTOResponse(t.getId(), t.getNumeroTarjeta(), t.getEntidadFinanciera(), t.getRedDePago(), t.getDiaCierre(), t.getDiaVencimientoPago(), t.getIdEspacioTrabajo());
        });

        var resp = compraCreditoService.registrarTarjeta(req);
        assertNotNull(resp);
        verify(tarjetaRepository, times(1)).save(any(Tarjeta.class));
    }

    // ---------------------------------------------------------
    // Tests para modificarTarjeta
    // ---------------------------------------------------------

    @Test
    void modificarTarjeta_tarjetaNoExiste_lanzaEntityNotFound() {
        when(tarjetaRepository.findById(20L)).thenReturn(Optional.empty());
        TarjetaDTOUpdate dto = new TarjetaDTOUpdate(15, 5);
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.modificarTarjeta(20L, dto));
    }

    @Test
    void modificarTarjeta_exitoso_modificaYRetorna() {
        Tarjeta t = new Tarjeta();
        t.setId(20L);
        t.setDiaCierre(10);
        t.setDiaVencimientoPago(3);
        when(tarjetaRepository.findById(20L)).thenReturn(Optional.of(t));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tarjetaMapper.toResponse(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta saved = inv.getArgument(0);
            return new TarjetaDTOResponse(saved.getId(), saved.getNumeroTarjeta(), saved.getEntidadFinanciera(), saved.getRedDePago(), saved.getDiaCierre(), saved.getDiaVencimientoPago(), espacio.getId());
        });
        lenient().doAnswer(inv -> {
            TarjetaDTOUpdate dto = inv.getArgument(0);
            Tarjeta target = inv.getArgument(1);
            target.setDiaCierre(dto.diaCierre());
            target.setDiaVencimientoPago(dto.diaVencimientoPago());
            return null;
        }).when(tarjetaMapper).updateEntity(any(TarjetaDTOUpdate.class), any(Tarjeta.class));

        TarjetaDTOUpdate dto = new TarjetaDTOUpdate(15, 7);
        var resp = compraCreditoService.modificarTarjeta(20L, dto);
        assertNotNull(resp);

        ArgumentCaptor<Tarjeta> captor = ArgumentCaptor.forClass(Tarjeta.class);
        verify(tarjetaRepository, times(1)).save(captor.capture());
        Tarjeta saved = captor.getValue();
        assertEquals(15, saved.getDiaCierre());
        assertEquals(7, saved.getDiaVencimientoPago());
    }

    // ---------------------------------------------------------
    // Tests para removerCompraCredito
    // ---------------------------------------------------------

    @Test
    void removerCompraCredito_noExiste_lanzaEntityNotFound() {
        when(compraCreditoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.removerCompraCredito(99L));
    }

    @Test
    void removerCompraCredito_tieneCuotasPagadas_lanzaIllegalState() {
        when(compraCreditoRepository.findById(100L)).thenReturn(Optional.of(compraCreditoEntity));
        when(cuotaCreditoRepository.findByCompraCredito_IdAndPagada(100L, true)).thenReturn(List.of(new CuotaCredito()));
        assertThrows(com.campito.backend.common.exception.OperacionNoPermitidaException.class, () -> compraCreditoService.removerCompraCredito(100L));
    }

    @Test
    void removerCompraCredito_sinCuotasPagadas_eliminaCompraYCuotas() {
        when(compraCreditoRepository.findById(100L)).thenReturn(Optional.of(compraCreditoEntity));
        when(cuotaCreditoRepository.findByCompraCredito_IdAndPagada(100L, true)).thenReturn(List.of());

        compraCreditoService.removerCompraCredito(100L);

        verify(cuotaCreditoRepository, times(1)).deleteByCompraCredito_Id(100L);
        verify(compraCreditoRepository, times(1)).deleteById(100L);
        verify(eventPublisher).publishEvent(any(CompraCreditoEliminadaEvent.class));
    }

    // ---------------------------------------------------------
    // Tests para listarComprasCreditoDebeCuotas y BuscarComprasCredito
    // ---------------------------------------------------------

    @Test
    void listarComprasCreditoDebeCuotas_retornaDTOs() {
        CompraCredito c = new CompraCredito();
        c.setId(200L);
        when(compraCreditoRepository.findByIdEspacioTrabajoAndCuotasPendientesPageable(eq(espacio.getId()), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(c)));
        when(compraCreditoMapper.toResponse(any())).thenReturn(new CompraCreditoDTOResponse(200L, LocalDate.now(), new BigDecimal("100.00"), 2, 0, "desc", "Aud", LocalDate.now().atStartOfDay(), espacio.getId(), "esp", 1L, "mot", null, null, 10L, "num", "ent", "red"));

        var res = compraCreditoService.listarComprasCreditoDebeCuotas(espacio.getId(), null, null);
        assertEquals(1, res.getContent().size());
    }

    @Test
    void buscarComprasCredito_retornaDTOs() {
        CompraCredito c = new CompraCredito();
        c.setId(201L);
        when(compraCreditoRepository.findByIdEspacioTrabajo(espacio.getId())).thenReturn(List.of(c));
        when(compraCreditoMapper.toResponse(any())).thenReturn(new CompraCreditoDTOResponse(201L, LocalDate.now(), new BigDecimal("50.00"), 1, 0, "desc2", "Aud", LocalDate.now().atStartOfDay(), espacio.getId(), "esp", 1L, "mot", null, null, 10L, "num", "ent", "red"));

        var res = compraCreditoService.BuscarComprasCredito(espacio.getId());
        assertEquals(1, res.size());
    }

    // ---------------------------------------------------------
    // Tests para listarCuotasPorTarjeta
    // ---------------------------------------------------------

    @Test
    void listarCuotasPorTarjeta_tarjetaNoExiste_lanzaEntityNotFound() {
        when(tarjetaRepository.findById(20L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.listarCuotasPorTarjeta(20L));
    }

    @Test
    void listarCuotasPorTarjeta_exitoso_retornaCuotasDTO() {
        Tarjeta t = new Tarjeta();
        t.setId(20L);
        t.setDiaCierre(15);
        t.setDiaVencimientoPago(5);
        when(tarjetaRepository.findById(20L)).thenReturn(Optional.of(t));
        when(cuotaCreditoRepository.findByTarjetaAndFechaVencimientoBetween(eq(20L), any(), any())).thenReturn(List.of(new CuotaCredito()));

        var res = compraCreditoService.listarCuotasPorTarjeta(20L);
        assertEquals(1, res.size());
    }

    // ---------------------------------------------------------
    // Tests para pagarResumenTarjeta
    // ---------------------------------------------------------

    @Test
    void pagarResumenTarjeta_resumenNoExiste_lanzaEntityNotFound() {
        var req = new PagarResumenTarjetaRequest(999L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        when(resumenRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_resumenEnEstadoInvalido_lanzaIllegalState() {
        Resumen resumen = new Resumen();
        resumen.setId(50L);
        resumen.setEstado(EstadoResumen.PAGADO);
        resumen.setTarjeta(tarjeta);
        var req = new PagarResumenTarjetaRequest(50L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        when(resumenRepository.findById(50L)).thenReturn(Optional.of(resumen));
        assertThrows(IllegalStateException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_montoDistinto_lanzaIllegalArgument() {
        Resumen resumen = new Resumen();
        resumen.setId(51L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(new BigDecimal("200.00"));
        var req = new PagarResumenTarjetaRequest(51L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        when(resumenRepository.findById(51L)).thenReturn(Optional.of(resumen));
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_cuentaInsuficiente_lanzaIllegalState() {
        Resumen resumen = new Resumen();
        resumen.setId(52L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(new BigDecimal("100.00"));

        CuentaBancaria cuenta = new CuentaBancaria();
        cuenta.setId(3L);
        cuenta.setSaldoActual(new BigDecimal("50.00"));
        cuenta.setIdEspacioTrabajo(espacio.getId());

        when(resumenRepository.findById(52L)).thenReturn(Optional.of(resumen));
        when(cuentaBancariaRepository.findById(3L)).thenReturn(Optional.of(cuenta));
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Pago de tarjeta", espacio.getId())).thenReturn(Optional.empty());
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });
        // Mock para que transaccionService lance IllegalStateException por saldo insuficiente
        when(transaccionService.registrarTransaccion(any())).thenThrow(
            new IllegalStateException("Saldo insuficiente en la cuenta")
        );

        // request indicando idCuentaBancaria = 3L
        assertThrows(IllegalStateException.class, () -> compraCreditoService.pagarResumenTarjeta(new PagarResumenTarjetaRequest(52L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), 3L)));
    }

    @Test
    void pagarResumenTarjeta_exitoso_registraTransaccionYMarcaCuotas() {
        Resumen resumen = new Resumen();
        resumen.setId(60L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(new BigDecimal("300.00"));
        resumen.setAnio(2026);
        resumen.setMes(2); // Febrero 2026 — mes del ciclo del resumen

        when(resumenRepository.findById(60L)).thenReturn(Optional.of(resumen));

        // Motivo no existe -> se crea
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Pago de tarjeta", espacio.getId())).thenReturn(Optional.empty());
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        // Transaccion creada por TransaccionService
        TransaccionDTOResponse txResp = new TransaccionDTOResponse(700L, LocalDate.now(), new BigDecimal("300.00"), TipoTransaccion.GASTO, "desc", "Aud", java.time.LocalDateTime.now(), espacio.getId(), "esp", 1L, "mot", 1L, "contact", "nombreCuenta");
        when(transaccionService.registrarTransaccion(any())).thenReturn(txResp);
        when(transaccionRepository.findById(700L)).thenReturn(Optional.of(new Transaccion()));

        // Cuotas asociadas
        CuotaCredito cuota1 = new CuotaCredito(); cuota1.setId(1L); cuota1.setPagada(false); CompraCredito compra1 = new CompraCredito(); compra1.setCantidadCuotas(2); compra1.setCuotasPagadas(0); cuota1.setCompraCredito(compra1);
        CuotaCredito cuota2 = new CuotaCredito(); cuota2.setId(2L); cuota2.setPagada(false); CompraCredito compra2 = new CompraCredito(); compra2.setCantidadCuotas(2); compra2.setCuotasPagadas(0); cuota2.setCompraCredito(compra2);
        when(cuotaCreditoRepository.findByResumenAsociado_Id(60L)).thenReturn(List.of(cuota1, cuota2));

        // Ejecutar
        PagarResumenTarjetaRequest req = new PagarResumenTarjetaRequest(60L, LocalDate.now(), new BigDecimal("300.00"), "Aud", espacio.getId(), null);
        compraCreditoService.pagarResumenTarjeta(req);

        // Verificaciones
        verify(resumenRepository, times(1)).save(resumen);
        verify(cuotaCreditoRepository, times(1)).saveAll(List.of(cuota1, cuota2));
        verify(compraCreditoRepository, times(2)).save(any(CompraCredito.class));
        verify(eventPublisher).publishEvent(any(ResumenPagadoEvent.class));
    }

    // ---------------------------------------------------------
    // Tests para listarResumenesPorTarjeta y listarResumenesPorEspacioTrabajo
    // ---------------------------------------------------------

    @Test
    void listarResumenesPorTarjeta_retornaLista() {
        Resumen r = new Resumen(); r.setId(1L); r.setTarjeta(tarjeta);
        when(resumenRepository.findByTarjetaIdAndEstadoIn(10L, List.of(EstadoResumen.CERRADO, EstadoResumen.PAGADO_PARCIAL))).thenReturn(List.of(r));
        when(cuotaCreditoRepository.findByResumenAsociado_Id(1L)).thenReturn(List.of());
        var res = compraCreditoService.listarResumenesPorTarjeta(10L);
        assertNotNull(res);
    }

    @Test
    void listarResumenesPorEspacioTrabajo_retornaLista() {
        Resumen r = new Resumen(); r.setId(2L);
        when(resumenRepository.findByEspacioTrabajoId(espacio.getId())).thenReturn(List.of(r));
        when(resumenMapper.toResponse(any())).thenReturn(new ResumenDTOResponse(2L, 2025, 6, LocalDate.now(), EstadoResumen.CERRADO, new BigDecimal("100.00"), 10L, "num", "ent", "red", null, 1, List.of()));
        var res = compraCreditoService.listarResumenesPorEspacioTrabajo(espacio.getId());
        assertEquals(1, res.size());
    }

    // ---------------------------------------------------------
    // Tests adicionales para cubrir ramas faltantes
    // ---------------------------------------------------------

    @Test
    void registrarTarjeta_duplicada_lanzaEntidadDuplicada() {
        var req = new TarjetaDTORequest("1234", "Entidad", "VISA", 1, 5, espacio.getId());
        Tarjeta existente = new Tarjeta();
        existente.setId(10L);
        when(tarjetaRepository.findFirstByNumeroTarjetaAndEntidadFinancieraAndRedDePagoAndIdEspacioTrabajo(
            "1234", "Entidad", "VISA", espacio.getId()))
            .thenReturn(Optional.of(existente));

        assertThrows(com.campito.backend.common.exception.EntidadDuplicadaException.class,
            () -> compraCreditoService.registrarTarjeta(req));
    }

    @Test
    void removerTarjeta_noExiste_lanzaEntityNotFound() {
        when(tarjetaRepository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.removerTarjeta(99L));
    }

    @Test
    void removerTarjeta_tieneComprasAsociadas_lanzaOperacionNoPermitida() {
        when(tarjetaRepository.existsById(10L)).thenReturn(true);
        when(compraCreditoRepository.existsByTarjeta_Id(10L)).thenReturn(true);
        assertThrows(com.campito.backend.common.exception.OperacionNoPermitidaException.class,
            () -> compraCreditoService.removerTarjeta(10L));
    }

    @Test
    void removerTarjeta_sinComprasAsociadas_elimina() {
        when(tarjetaRepository.existsById(10L)).thenReturn(true);
        when(compraCreditoRepository.existsByTarjeta_Id(10L)).thenReturn(false);

        compraCreditoService.removerTarjeta(10L);
        verify(tarjetaRepository).deleteById(10L);
    }

    @Test
    void buscarComprasCreditoConPaginacion_conAnioYMes_filtraPorRango() {
        CompraCreditoBusquedaDTO busqueda = new CompraCreditoBusquedaDTO(
            6, 2025, null, null, espacio.getId(), 0, 10);
        when(compraCreditoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var res = compraCreditoService.buscarComprasCredito(busqueda);
        assertNotNull(res);
    }

    @Test
    void buscarComprasCreditoConPaginacion_conAnioSinMes_filtraAnioCompleto() {
        CompraCreditoBusquedaDTO busqueda = new CompraCreditoBusquedaDTO(
            null, 2025, null, null, espacio.getId(), 0, 10);
        when(compraCreditoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var res = compraCreditoService.buscarComprasCredito(busqueda);
        assertNotNull(res);
    }

    @Test
    void buscarComprasCreditoConPaginacion_mesSinAnio_lanzaIllegalArgument() {
        CompraCreditoBusquedaDTO busqueda = new CompraCreditoBusquedaDTO(
            6, null, null, null, espacio.getId(), 0, 10);
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.buscarComprasCredito(busqueda));
    }

    @Test
    void buscarComprasCreditoConPaginacion_conMotivo_filtra() {
        CompraCreditoBusquedaDTO busqueda = new CompraCreditoBusquedaDTO(
            null, null, "super", null, espacio.getId(), 0, 10);
        when(compraCreditoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var res = compraCreditoService.buscarComprasCredito(busqueda);
        assertNotNull(res);
    }

    @Test
    void buscarComprasCreditoConPaginacion_conContacto_filtra() {
        CompraCreditoBusquedaDTO busqueda = new CompraCreditoBusquedaDTO(
            null, null, null, "comercio", espacio.getId(), 0, 10);
        when(compraCreditoRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var res = compraCreditoService.buscarComprasCredito(busqueda);
        assertNotNull(res);
    }

    @Test
    void pagarResumenTarjeta_resumenAbierto_lanzaIllegalState() {
        Resumen resumen = new Resumen();
        resumen.setId(70L);
        resumen.setEstado(EstadoResumen.ABIERTO);
        resumen.setTarjeta(tarjeta);
        var req = new PagarResumenTarjetaRequest(70L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        when(resumenRepository.findById(70L)).thenReturn(Optional.of(resumen));
        assertThrows(IllegalStateException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_espacioNoCoincide_lanzaIllegalArgument() {
        Resumen resumen = new Resumen();
        resumen.setId(71L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setMontoTotal(new BigDecimal("100.00"));
        Tarjeta tarjetaOtroEspacio = new Tarjeta();
        tarjetaOtroEspacio.setId(10L);
        tarjetaOtroEspacio.setIdEspacioTrabajo(UUID.fromString("00000000-0000-0000-0000-000000000099"));
        resumen.setTarjeta(tarjetaOtroEspacio);
        var req = new PagarResumenTarjetaRequest(71L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        when(resumenRepository.findById(71L)).thenReturn(Optional.of(resumen));
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_cuentaNoPerteneceAEspacio_lanzaIllegalArgument() {
        Resumen resumen = new Resumen();
        resumen.setId(72L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setMontoTotal(new BigDecimal("100.00"));
        resumen.setTarjeta(tarjeta);

        CuentaBancaria cuentaOtra = new CuentaBancaria();
        cuentaOtra.setId(4L);
        cuentaOtra.setIdEspacioTrabajo(UUID.fromString("00000000-0000-0000-0000-000000000099"));

        when(resumenRepository.findById(72L)).thenReturn(Optional.of(resumen));
        when(cuentaBancariaRepository.findById(4L)).thenReturn(Optional.of(cuentaOtra));

        var req = new PagarResumenTarjetaRequest(72L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), 4L);
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_cuentaNoExiste_lanzaEntityNotFound() {
        Resumen resumen = new Resumen();
        resumen.setId(73L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setMontoTotal(new BigDecimal("100.00"));
        resumen.setTarjeta(tarjeta);

        when(resumenRepository.findById(73L)).thenReturn(Optional.of(resumen));
        when(cuentaBancariaRepository.findById(4L)).thenReturn(Optional.empty());

        var req = new PagarResumenTarjetaRequest(73L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), 4L);
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_conMotivoExistente_usaMotivoExistente() {
        Resumen resumen = new Resumen();
        resumen.setId(65L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(new BigDecimal("200.00"));
        resumen.setAnio(2026);
        resumen.setMes(3);

        MotivoTransaccion motivoExistente = new MotivoTransaccion();
        motivoExistente.setId(50L);
        motivoExistente.setMotivo("Pago de tarjeta");

        when(resumenRepository.findById(65L)).thenReturn(Optional.of(resumen));
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Pago de tarjeta", espacio.getId()))
            .thenReturn(Optional.of(motivoExistente));

        TransaccionDTOResponse txResp = new TransaccionDTOResponse(800L, LocalDate.now(), new BigDecimal("200.00"),
            TipoTransaccion.GASTO, "desc", "Aud", java.time.LocalDateTime.now(), espacio.getId(), "esp", 50L, "mot", 1L, "contact", "nombreCuenta");
        when(transaccionService.registrarTransaccion(any())).thenReturn(txResp);
        when(transaccionRepository.findById(800L)).thenReturn(Optional.of(new Transaccion()));
        when(cuotaCreditoRepository.findByResumenAsociado_Id(65L)).thenReturn(List.of());

        PagarResumenTarjetaRequest req = new PagarResumenTarjetaRequest(65L, LocalDate.now(), new BigDecimal("200.00"), "Aud", espacio.getId(), null);
        compraCreditoService.pagarResumenTarjeta(req);
        verify(motivoRepository, never()).save(any(MotivoTransaccion.class));
    }

    @Test
    void pagarResumenTarjeta_cuotaYaPagada_laSalta() {
        Resumen resumen = new Resumen();
        resumen.setId(66L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(new BigDecimal("100.00"));
        resumen.setAnio(2026);
        resumen.setMes(4);

        when(resumenRepository.findById(66L)).thenReturn(Optional.of(resumen));
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Pago de tarjeta", espacio.getId())).thenReturn(Optional.empty());
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        TransaccionDTOResponse txResp = new TransaccionDTOResponse(900L, LocalDate.now(), new BigDecimal("100.00"),
            TipoTransaccion.GASTO, "desc", "Aud", java.time.LocalDateTime.now(), espacio.getId(), "esp", 1L, "mot", 1L, "contact", "nombreCuenta");
        when(transaccionService.registrarTransaccion(any())).thenReturn(txResp);
        when(transaccionRepository.findById(900L)).thenReturn(Optional.of(new Transaccion()));

        CuotaCredito cuotaPagada = new CuotaCredito();
        cuotaPagada.setId(3L);
        cuotaPagada.setPagada(true); // Already paid
        CompraCredito compra = new CompraCredito();
        compra.setCantidadCuotas(1);
        compra.setCuotasPagadas(1);
        cuotaPagada.setCompraCredito(compra);

        when(cuotaCreditoRepository.findByResumenAsociado_Id(66L)).thenReturn(List.of(cuotaPagada));

        PagarResumenTarjetaRequest req = new PagarResumenTarjetaRequest(66L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        compraCreditoService.pagarResumenTarjeta(req);
        verify(compraCreditoRepository, never()).save(any(CompraCredito.class));
    }

    @Test
    void pagarResumenTarjeta_cuotasVacias_registraTransaccion() {
        Resumen resumen = new Resumen();
        resumen.setId(67L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(new BigDecimal("100.00"));
        resumen.setAnio(2026);
        resumen.setMes(5);

        when(resumenRepository.findById(67L)).thenReturn(Optional.of(resumen));
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Pago de tarjeta", espacio.getId())).thenReturn(Optional.empty());
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        TransaccionDTOResponse txResp = new TransaccionDTOResponse(950L, LocalDate.now(), new BigDecimal("100.00"),
            TipoTransaccion.GASTO, "desc", "Aud", java.time.LocalDateTime.now(), espacio.getId(), "esp", 1L, "mot", 1L, "contact", "nombreCuenta");
        when(transaccionService.registrarTransaccion(any())).thenReturn(txResp);
        when(transaccionRepository.findById(950L)).thenReturn(Optional.of(new Transaccion()));
        when(cuotaCreditoRepository.findByResumenAsociado_Id(67L)).thenReturn(List.of());

        PagarResumenTarjetaRequest req = new PagarResumenTarjetaRequest(67L, LocalDate.now(), new BigDecimal("100.00"), "Aud", espacio.getId(), null);
        compraCreditoService.pagarResumenTarjeta(req);
        verify(cuotaCreditoRepository).saveAll(List.of());
    }

    @Test
    void listarComprasCreditoDebeCuotas_conPaginaExplicita() {
        when(compraCreditoRepository.findByIdEspacioTrabajoAndCuotasPendientesPageable(eq(espacio.getId()), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));
        var res = compraCreditoService.listarComprasCreditoDebeCuotas(espacio.getId(), 2, 5);
        assertNotNull(res);
    }

    @Test
    void listarResumenesPorTarjeta_conCuotas_retornaConCuotas() {
        Resumen r = new Resumen();
        r.setId(1L);
        r.setTarjeta(tarjeta);

        CompraCredito compraRes = new CompraCredito();
        compraRes.setId(10L);
        compraRes.setDescripcion("Compra Test");
        compraRes.setCantidadCuotas(3);
        MotivoTransaccion motivoRes = new MotivoTransaccion();
        motivoRes.setMotivo("Motivo Test");
        compraRes.setMotivo(motivoRes);

        CuotaCredito cuotaRes = new CuotaCredito();
        cuotaRes.setId(1L);
        cuotaRes.setNumeroCuota(1);
        cuotaRes.setMontoCuota(new BigDecimal("100.00"));
        cuotaRes.setCompraCredito(compraRes);

        when(resumenRepository.findByTarjetaIdAndEstadoIn(10L, List.of(EstadoResumen.CERRADO, EstadoResumen.PAGADO_PARCIAL)))
            .thenReturn(List.of(r));
        when(cuotaCreditoRepository.findByResumenAsociado_Id(1L)).thenReturn(List.of(cuotaRes));

        var res = compraCreditoService.listarResumenesPorTarjeta(10L);
        assertEquals(1, res.size());
    }

    @Test
    void listarResumenesPorTarjeta_conCuotaSinDescripcion_usaFallback() {
        Resumen r = new Resumen();
        r.setId(1L);
        r.setTarjeta(tarjeta);

        CompraCredito compraSinDesc = new CompraCredito();
        compraSinDesc.setId(10L);
        compraSinDesc.setDescripcion(null); // null description
        compraSinDesc.setCantidadCuotas(1);
        MotivoTransaccion motivoRes = new MotivoTransaccion();
        motivoRes.setMotivo("Motivo");
        compraSinDesc.setMotivo(motivoRes);

        CuotaCredito cuota = new CuotaCredito();
        cuota.setId(1L);
        cuota.setNumeroCuota(1);
        cuota.setMontoCuota(new BigDecimal("50.00"));
        cuota.setCompraCredito(compraSinDesc);

        when(resumenRepository.findByTarjetaIdAndEstadoIn(10L, List.of(EstadoResumen.CERRADO, EstadoResumen.PAGADO_PARCIAL)))
            .thenReturn(List.of(r));
        when(cuotaCreditoRepository.findByResumenAsociado_Id(1L)).thenReturn(List.of(cuota));

        var res = compraCreditoService.listarResumenesPorTarjeta(10L);
        assertEquals(1, res.size());
    }

    @Test
    void registrarCompraCredito_comercioNoExiste_lanzaEntityNotFound() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), new BigDecimal("100.00"), 2, "desc", "Aud", espacio.getId(), 1L, 99L, 10L);
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        when(espacioTrabajoApi.obtenerNombre(espacio.getId())).thenReturn("Mi Espacio");
        MotivoTransaccion motivoConId = new MotivoTransaccion();
        motivoConId.setId(1L);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoConId));
        when(tarjetaRepository.findById(10L)).thenReturn(Optional.of(tarjeta));
        when(contactoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarCompraCredito(dto));
    }

    @Test
    void registrarCompraCredito_motivoNoExiste_lanzaEntityNotFound() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), new BigDecimal("100.00"), 2, "desc", "Aud", espacio.getId(), 99L, null, 10L);
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        when(motivoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarCompraCredito(dto));
    }

    @Test
    void registrarCompraCredito_tarjetaNoExiste_lanzaEntityNotFound() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), new BigDecimal("100.00"), 2, "desc", "Aud", espacio.getId(), 1L, null, 99L);
        when(espacioTrabajoApi.existe(espacio.getId())).thenReturn(true);
        MotivoTransaccion motivoConId = new MotivoTransaccion();
        motivoConId.setId(1L);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoConId));
        when(tarjetaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarCompraCredito(dto));
    }

    @Test
    void listarTarjetas_retornaLista() {
        Tarjeta t1 = new Tarjeta(); t1.setId(1L);
        when(tarjetaRepository.findByIdEspacioTrabajo(espacio.getId())).thenReturn(List.of(t1));
        when(tarjetaMapper.toResponse(any())).thenReturn(new TarjetaDTOResponse(1L, "1234", "Banco", "Visa", 15, 5, espacio.getId()));

        var res = compraCreditoService.listarTarjetas(espacio.getId());
        assertEquals(1, res.size());
    }

    @Test
    void listarTarjetas_vacia_retornaListaVacia() {
        when(tarjetaRepository.findByIdEspacioTrabajo(espacio.getId())).thenReturn(List.of());
        var res = compraCreditoService.listarTarjetas(espacio.getId());
        assertEquals(0, res.size());
    }

}
