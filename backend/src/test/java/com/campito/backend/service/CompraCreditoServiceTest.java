package com.campito.backend.service;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.campito.backend.dao.CompraCreditoRepository;
import com.campito.backend.dao.ContactoTransferenciaRepository;
import com.campito.backend.dao.CuentaBancariaRepository;
import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.MotivoTransaccionRepository;
import com.campito.backend.dao.ResumenRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dao.TransaccionRepository;
import com.campito.backend.dto.CompraCreditoDTORequest;
import com.campito.backend.dto.CompraCreditoDTOResponse;
import com.campito.backend.dto.CuotaCreditoDTOResponse;
import com.campito.backend.dto.PagarResumenTarjetaRequest;
import com.campito.backend.dto.TransaccionDTOResponse;
import com.campito.backend.mapper.CompraCreditoMapper;
import com.campito.backend.mapper.CuotaCreditoMapper;
import com.campito.backend.mapper.ResumenMapper;
import com.campito.backend.mapper.TarjetaMapper;
import com.campito.backend.model.CompraCredito;
import com.campito.backend.model.ContactoTransferencia;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.CuotaCredito;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.EstadoResumen;
import com.campito.backend.model.MotivoTransaccion;
import com.campito.backend.model.Resumen;
import com.campito.backend.model.Tarjeta;
import com.campito.backend.model.TipoTransaccion;
import com.campito.backend.model.Transaccion;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CompraCreditoServiceTest {

    @Mock
    private CompraCreditoRepository compraCreditoRepository;

    @Mock
    private EspacioTrabajoRepository espacioRepository;

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

    @InjectMocks
    private CompraCreditoServiceImpl compraCreditoService;

    private EspacioTrabajo espacio;
    private Tarjeta tarjeta;
    private CompraCredito compraCreditoEntity;

    @BeforeEach
    void setUp() {
        espacio = new EspacioTrabajo();
        espacio.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        espacio.setNombre("Mi Espacio");

        tarjeta = new Tarjeta();
        tarjeta.setId(10L);
        tarjeta.setDiaCierre(25);
        tarjeta.setDiaVencimientoPago(5);
        tarjeta.setEspacioTrabajo(espacio);

        compraCreditoEntity = new CompraCredito();
        compraCreditoEntity.setId(100L);
        compraCreditoEntity.setTarjeta(tarjeta);
        compraCreditoEntity.setMontoTotal(1000f);
        compraCreditoEntity.setCantidadCuotas(5);
        compraCreditoEntity.setFechaCompra(LocalDate.of(2025, Month.JANUARY, 1));
        MotivoTransaccion motivoDefault = new MotivoTransaccion();
        motivoDefault.setId(1L);
        compraCreditoEntity.setMotivo(motivoDefault);
        compraCreditoEntity.setEspacioTrabajo(espacio);

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
                c.getEspacioTrabajo() != null ? c.getEspacioTrabajo().getId() : espacio.getId(),
                c.getEspacioTrabajo() != null ? c.getEspacioTrabajo().getNombre() : "esp",
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
    void registrarCompraCredito_nullDto_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.registrarCompraCredito(null));
        verify(compraCreditoRepository, never()).save(any());
    }

    @Test
    void registrarCompraCredito_espacioNoExiste_lanzaEntityNotFound() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), 100f, 2, "desc", "Aud", espacio.getId(), 1L, null, 1L);
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarCompraCredito(dto));
        verify(compraCreditoRepository, never()).save(any());
    }

    @Test
    void registrarCompraCredito_creaCuotasSiCantidadValida_yGuardaCompra() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.of(2025, Month.JULY, 20), 1000f, 3, "desc", "Aud", espacio.getId(), 1L, null, 10L);
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
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
    }

    @Test
    void registrarCompraCredito_conComercioOpcional_asignaComercio() {
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.of(2025, Month.JUNE, 10), 500f, 2, "desc", "Aud", espacio.getId(), 1L, 99L, 10L);

        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
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
        CompraCreditoDTORequest dto = new CompraCreditoDTORequest(LocalDate.now(), 1000f, 0, "desc", "Aud", espacio.getId(), 1L, null, 10L);
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
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
    }

    // ---------------------------------------------------------
    // Tests para registrarTarjeta
    // ---------------------------------------------------------

    @Test
    void registrarTarjeta_nullDto_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.registrarTarjeta(null));
    }

    @Test
    void registrarTarjeta_espacioNoExiste_lanzaEntityNotFound() {
        var req = new com.campito.backend.dto.TarjetaDTORequest("1234", "Entidad", "VISA", 1, 5, espacio.getId());
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.registrarTarjeta(req));
    }

    @Test
    void registrarTarjeta_exitoso_guardaYRetorna() {
        var req = new com.campito.backend.dto.TarjetaDTORequest("1234", "Entidad", "VISA", 1, 5, espacio.getId());
        when(espacioRepository.findById(espacio.getId())).thenReturn(Optional.of(espacio));
        when(tarjetaMapper.toEntity(any())).thenAnswer(inv -> {
            com.campito.backend.dto.TarjetaDTORequest r = inv.getArgument(0);
            Tarjeta t = new Tarjeta();
            t.setNumeroTarjeta(r.numeroTarjeta());
            t.setEntidadFinanciera(r.entidadFinanciera());
            t.setRedDePago(r.redDePago());
            t.setDiaCierre(r.diaCierre());
            t.setDiaVencimientoPago(r.diaVencimientoPago());
            t.setEspacioTrabajo(espacio);
            t.setId(555L);
            return t;
        });
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tarjetaMapper.toResponse(any(Tarjeta.class))).thenAnswer(inv -> {
            Tarjeta t = inv.getArgument(0);
            return new com.campito.backend.dto.TarjetaDTOResponse(t.getId(), t.getNumeroTarjeta(), t.getEntidadFinanciera(), t.getRedDePago(), t.getDiaCierre(), t.getDiaVencimientoPago(), t.getEspacioTrabajo().getId());
        });

        var resp = compraCreditoService.registrarTarjeta(req);
        assertNotNull(resp);
        verify(tarjetaRepository, times(1)).save(any(Tarjeta.class));
    }

    // ---------------------------------------------------------
    // Tests para modificarTarjeta
    // ---------------------------------------------------------

    @Test
    void modificarTarjeta_nullParams_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.modificarTarjeta(null, 1, 5));
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.modificarTarjeta(1L, null, 5));
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.modificarTarjeta(1L, 1, null));
    }

    @Test
    void modificarTarjeta_tarjetaNoExiste_lanzaEntityNotFound() {
        when(tarjetaRepository.findById(20L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.modificarTarjeta(20L, 15, 5));
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
            return new com.campito.backend.dto.TarjetaDTOResponse(saved.getId(), saved.getNumeroTarjeta(), saved.getEntidadFinanciera(), saved.getRedDePago(), saved.getDiaCierre(), saved.getDiaVencimientoPago(), espacio.getId());
        });

        var resp = compraCreditoService.modificarTarjeta(20L, 15, 7);
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
    void removerCompraCredito_idNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.removerCompraCredito(null));
    }

    @Test
    void removerCompraCredito_noExiste_lanzaEntityNotFound() {
        when(compraCreditoRepository.existsById(99L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.removerCompraCredito(99L));
    }

    @Test
    void removerCompraCredito_tieneCuotasPagadas_lanzaIllegalState() {
        when(compraCreditoRepository.existsById(100L)).thenReturn(true);
        when(cuotaCreditoRepository.findByCompraCredito_IdAndPagada(100L, true)).thenReturn(List.of(new CuotaCredito()));
        assertThrows(com.campito.backend.exception.OperacionNoPermitidaException.class, () -> compraCreditoService.removerCompraCredito(100L));
    }

    @Test
    void removerCompraCredito_sinCuotasPagadas_eliminaCompraYCuotas() {
        when(compraCreditoRepository.existsById(100L)).thenReturn(true);
        when(cuotaCreditoRepository.findByCompraCredito_IdAndPagada(100L, true)).thenReturn(List.of());

        compraCreditoService.removerCompraCredito(100L);

        verify(cuotaCreditoRepository, times(1)).deleteByCompraCredito_Id(100L);
        verify(compraCreditoRepository, times(1)).deleteById(100L);
    }

    // ---------------------------------------------------------
    // Tests para listarComprasCreditoDebeCuotas y BuscarComprasCredito
    // ---------------------------------------------------------

    @Test
    void listarComprasCreditoDebeCuotas_idNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.listarComprasCreditoDebeCuotas(null, null, null));
    }

    @Test
    void listarComprasCreditoDebeCuotas_retornaDTOs() {
        CompraCredito c = new CompraCredito();
        c.setId(200L);
        when(compraCreditoRepository.findByEspacioTrabajo_IdAndCuotasPendientesPageable(eq(espacio.getId()), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(c)));
        when(compraCreditoMapper.toResponse(any())).thenReturn(new CompraCreditoDTOResponse(200L, LocalDate.now(), 100f, 2, 0, "desc", "Aud", LocalDate.now().atStartOfDay(), espacio.getId(), "esp", 1L, "mot", null, null, 10L, "num", "ent", "red"));

        var res = compraCreditoService.listarComprasCreditoDebeCuotas(espacio.getId(), null, null);
        assertEquals(1, res.getContent().size());
    }

    @Test
    void buscarComprasCredito_idNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.BuscarComprasCredito(null));
    }

    @Test
    void buscarComprasCredito_retornaDTOs() {
        CompraCredito c = new CompraCredito();
        c.setId(201L);
        when(compraCreditoRepository.findByEspacioTrabajo_Id(espacio.getId())).thenReturn(List.of(c));
        when(compraCreditoMapper.toResponse(any())).thenReturn(new CompraCreditoDTOResponse(201L, LocalDate.now(), 50f, 1, 0, "desc2", "Aud", LocalDate.now().atStartOfDay(), espacio.getId(), "esp", 1L, "mot", null, null, 10L, "num", "ent", "red"));

        var res = compraCreditoService.BuscarComprasCredito(espacio.getId());
        assertEquals(1, res.size());
    }

    // ---------------------------------------------------------
    // Tests para listarCuotasPorTarjeta
    // ---------------------------------------------------------

    @Test
    void listarCuotasPorTarjeta_idNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.listarCuotasPorTarjeta(null));
    }

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
    void pagarResumenTarjeta_requestNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.pagarResumenTarjeta(null));
    }

    @Test
    void pagarResumenTarjeta_resumenNoExiste_lanzaEntityNotFound() {
        var req = new PagarResumenTarjetaRequest(999L, LocalDate.now(), 100f, "Aud", espacio.getId(), null);
        when(resumenRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_resumenEnEstadoInvalido_lanzaIllegalState() {
        Resumen resumen = new Resumen();
        resumen.setId(50L);
        resumen.setEstado(EstadoResumen.PAGADO);
        resumen.setTarjeta(tarjeta);
        var req = new PagarResumenTarjetaRequest(50L, LocalDate.now(), 100f, "Aud", espacio.getId(), null);
        when(resumenRepository.findById(50L)).thenReturn(Optional.of(resumen));
        assertThrows(IllegalStateException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_montoDistinto_lanzaIllegalArgument() {
        Resumen resumen = new Resumen();
        resumen.setId(51L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(200f);
        var req = new PagarResumenTarjetaRequest(51L, LocalDate.now(), 100f, "Aud", espacio.getId(), null);
        when(resumenRepository.findById(51L)).thenReturn(Optional.of(resumen));
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.pagarResumenTarjeta(req));
    }

    @Test
    void pagarResumenTarjeta_cuentaInsuficiente_lanzaIllegalState() {
        Resumen resumen = new Resumen();
        resumen.setId(52L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(100f);

        CuentaBancaria cuenta = new CuentaBancaria();
        cuenta.setId(3L);
        cuenta.setSaldoActual(50f);
        cuenta.setEspacioTrabajo(espacio);

        when(resumenRepository.findById(52L)).thenReturn(Optional.of(resumen));
        when(cuentaBancariaRepository.findById(3L)).thenReturn(Optional.of(cuenta));
        when(motivoRepository.findFirstByMotivoAndEspacioTrabajo_Id("Pago de tarjeta", espacio.getId())).thenReturn(Optional.empty());
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
        assertThrows(IllegalStateException.class, () -> compraCreditoService.pagarResumenTarjeta(new PagarResumenTarjetaRequest(52L, LocalDate.now(), 100f, "Aud", espacio.getId(), 3L)));
    }

    @Test
    void pagarResumenTarjeta_exitoso_registraTransaccionYMarcaCuotas() {
        Resumen resumen = new Resumen();
        resumen.setId(60L);
        resumen.setEstado(EstadoResumen.CERRADO);
        resumen.setTarjeta(tarjeta);
        resumen.setMontoTotal(300f);

        when(resumenRepository.findById(60L)).thenReturn(Optional.of(resumen));

        // Motivo no existe -> se crea
        when(motivoRepository.findFirstByMotivoAndEspacioTrabajo_Id("Pago de tarjeta", espacio.getId())).thenReturn(Optional.empty());
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> inv.getArgument(0));

        // Transaccion creada por TransaccionService
        TransaccionDTOResponse txResp = new TransaccionDTOResponse(700L, LocalDate.now(), 300f, TipoTransaccion.GASTO, "desc", "Aud", java.time.LocalDateTime.now(), espacio.getId(), "esp", 1L, "mot", 1L, "contact", "nombreCuenta");
        when(transaccionService.registrarTransaccion(any())).thenReturn(txResp);
        when(transaccionRepository.findById(700L)).thenReturn(Optional.of(new Transaccion()));

        // Cuotas asociadas
        CuotaCredito cuota1 = new CuotaCredito(); cuota1.setId(1L); cuota1.setPagada(false); CompraCredito compra1 = new CompraCredito(); compra1.setCantidadCuotas(2); compra1.setCuotasPagadas(0); cuota1.setCompraCredito(compra1);
        CuotaCredito cuota2 = new CuotaCredito(); cuota2.setId(2L); cuota2.setPagada(false); CompraCredito compra2 = new CompraCredito(); compra2.setCantidadCuotas(2); compra2.setCuotasPagadas(0); cuota2.setCompraCredito(compra2);
        when(cuotaCreditoRepository.findByResumenAsociado_Id(60L)).thenReturn(List.of(cuota1, cuota2));

        // Ejecutar
        PagarResumenTarjetaRequest req = new PagarResumenTarjetaRequest(60L, LocalDate.now(), 300f, "Aud", espacio.getId(), null);
        compraCreditoService.pagarResumenTarjeta(req);

        // Verificaciones
        verify(resumenRepository, times(1)).save(resumen);
        verify(cuotaCreditoRepository, times(1)).saveAll(List.of(cuota1, cuota2));
        verify(compraCreditoRepository, times(2)).save(any(CompraCredito.class));
    }

    // ---------------------------------------------------------
    // Tests para listarResumenesPorTarjeta y listarResumenesPorEspacioTrabajo
    // ---------------------------------------------------------

    @Test
    void listarResumenesPorTarjeta_idNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.listarResumenesPorTarjeta(null));
    }

    @Test
    void listarResumenesPorTarjeta_retornaLista() {
        Resumen r = new Resumen(); r.setId(1L); r.setTarjeta(tarjeta);
        when(resumenRepository.findByTarjetaIdAndEstadoIn(10L, List.of(EstadoResumen.CERRADO, EstadoResumen.PAGADO_PARCIAL))).thenReturn(List.of(r));
        when(cuotaCreditoRepository.findByResumenAsociado_Id(1L)).thenReturn(List.of());
        var res = compraCreditoService.listarResumenesPorTarjeta(10L);
        assertNotNull(res);
    }

    @Test
    void listarResumenesPorEspacioTrabajo_idNull_lanzaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> compraCreditoService.listarResumenesPorEspacioTrabajo(null));
    }

    @Test
    void listarResumenesPorEspacioTrabajo_retornaLista() {
        Resumen r = new Resumen(); r.setId(2L);
        when(resumenRepository.findByEspacioTrabajoId(espacio.getId())).thenReturn(List.of(r));
        when(resumenMapper.toResponse(any())).thenReturn(new com.campito.backend.dto.ResumenDTOResponse(2L, 2025, 6, LocalDate.now(), EstadoResumen.CERRADO, 100f, 10L, "num", "ent", "red", null, 1, List.of()));
        var res = compraCreditoService.listarResumenesPorEspacioTrabajo(espacio.getId());
        assertEquals(1, res.size());
    }

}

