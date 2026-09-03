package com.campito.backend.transacciones.service;

import com.campito.backend.common.domain.TipoTransaccion;
import com.campito.backend.transacciones.repository.*;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;
import com.campito.backend.common.event.TransaccionEliminadaEvent;
import com.campito.backend.common.event.TransaccionRegistradaEvent;
import com.campito.backend.transacciones.domain.dto.*;
import com.campito.backend.transacciones.domain.entity.*;
import com.campito.backend.transacciones.mapper.*;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
public class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private EspacioTrabajoApi espacioTrabajoApi;
    @Mock
    private MotivoTransaccionRepository motivoRepository;
    @Mock
    private ContactoTransferenciaRepository contactoRepository;
    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private CuentaBancariaService cuentaBancariaService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TransaccionMapper transaccionMapper;
    @Mock
    private ContactoTransferenciaMapper contactoTransferenciaMapper;
    @Mock
    private MotivoTransaccionMapper motivoTransaccionMapper;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private MotivoTransaccion motivoTransaccion;
    private ContactoTransferencia contactoTransferencia;

    // UUIDs para pruebas
    private UUID espacioId;

    @BeforeEach
    void setUp() {
        // Usar SimpleMeterRegistry real para evitar problemas con mocks
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        transaccionService = new TransaccionServiceImpl(
            transaccionRepository,
            espacioTrabajoApi,
            motivoRepository,
            contactoRepository,
            cuentaBancariaRepository,
            cuentaBancariaService,
            transaccionMapper,
            contactoTransferenciaMapper,
            motivoTransaccionMapper,
            eventPublisher,
            meterRegistry
        );
        espacioId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        lenient().when(espacioTrabajoApi.obtenerNombre(any(UUID.class))).thenReturn("Espacio de Prueba");
        lenient().when(espacioTrabajoApi.obtenerSaldo(any(UUID.class))).thenReturn(new BigDecimal("1000.00"));

        motivoTransaccion = new MotivoTransaccion();
        motivoTransaccion.setId(1L);
        motivoTransaccion.setMotivo("Venta");
        motivoTransaccion.setIdEspacioTrabajo(espacioId);

        contactoTransferencia = new ContactoTransferencia();
        contactoTransferencia.setId(1L);
        contactoTransferencia.setNombre("Cliente A");
        contactoTransferencia.setIdEspacioTrabajo(espacioId);
        
        // Mock transaccionMapper behavior
        lenient().when(transaccionMapper.toEntity(any(TransaccionDTORequest.class))).thenAnswer(invocation -> {
            TransaccionDTORequest dto = invocation.getArgument(0);
            Transaccion t = new Transaccion();
            t.setTipo(dto.tipo());
            t.setMonto(dto.monto());
            t.setFecha(dto.fecha());
            t.setDescripcion(dto.descripcion());
            t.setNombreCompletoAuditoria(dto.nombreCompletoAuditoria());
            return t;
        });
        
        lenient().when(transaccionMapper.toResponse(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            return new TransaccionDTOResponse(
                t.getId(),
                t.getFecha(),
                t.getMonto(),
                t.getTipo(),
                t.getDescripcion(),
                t.getNombreCompletoAuditoria(),
                t.getFechaCreacion(),
                t.getIdEspacioTrabajo(),
                t.getNombreEspacioTrabajo(),
                t.getMotivo() != null ? t.getMotivo().getId() : null,
                t.getMotivo() != null ? t.getMotivo().getMotivo() : null,
                t.getContacto() != null ? t.getContacto().getId() : null,
                t.getContacto() != null ? t.getContacto().getNombre() : null,
                t.getCuentaBancaria() != null ? t.getCuentaBancaria().getNombre() : null
            );
        });
        
        // Mock contactoTransferenciaMapper behavior
        lenient().when(contactoTransferenciaMapper.toEntity(any(ContactoDTORequest.class))).thenAnswer(invocation -> {
            ContactoDTORequest dto = invocation.getArgument(0);
            ContactoTransferencia c = new ContactoTransferencia(dto.nombre());
            return c;
        });
        
        lenient().when(contactoTransferenciaMapper.toResponse(any(ContactoTransferencia.class))).thenAnswer(invocation -> {
            ContactoTransferencia c = invocation.getArgument(0);
            return new ContactoDTOResponse(c.getId(), c.getNombre());
        });
        
        // Mock motivoTransaccionMapper behavior
        lenient().when(motivoTransaccionMapper.toEntity(any(MotivoDTORequest.class))).thenAnswer(invocation -> {
            MotivoDTORequest dto = invocation.getArgument(0);
            MotivoTransaccion m = MotivoTransaccion.builder()
                .motivo(dto.motivo())
                .build();
            return m;
        });
        
        lenient().when(motivoTransaccionMapper.toResponse(any(MotivoTransaccion.class))).thenAnswer(invocation -> {
            MotivoTransaccion m = invocation.getArgument(0);
            return new MotivoDTOResponse(m.getId(), m.getMotivo());
        });
    }

    // Tests para registrarTransaccion

    @Test
    void registrarTransaccion_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("100.00"), TipoTransaccion.INGRESO, "Desc", "Auditor", UUID.fromString("00000000-0000-0000-0000-000000000099"), 1L, null, null);
        when(espacioTrabajoApi.existe(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarTransaccion(dto);
        });
        assertEquals("Espacio de trabajo con ID 00000000-0000-0000-0000-000000000099 no encontrado", exception.getMessage());
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void registrarTransaccion_cuandoMotivoNoExiste_entoncesLanzaExcepcion() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("100.00"), TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 99L, null, null);
        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarTransaccion(dto);
        });
        assertEquals("Motivo de transaccion con ID 99 no encontrado", exception.getMessage());
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void registrarTransaccion_cuandoIdContactoExistePeroContactoNoExiste_entoncesLanzaExcepcion() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("100.00"), TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 1L, 99L, null);
        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoTransaccion));
        when(contactoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarTransaccion(dto);
        });
        assertEquals("Contacto de transferencia con ID 99 no encontrado", exception.getMessage());
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void registrarTransaccion_cuandoOpcionCorrecta_entoncesRegistroExitoso() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("100.00"), TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 1L, 1L, null);
        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoTransaccion));
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(contactoTransferencia));
        when(contactoRepository.save(any(ContactoTransferencia.class))).thenAnswer(inv -> {
            ContactoTransferencia c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion trans = invocation.getArgument(0);
            trans.setId(1L);
            return trans;
        });

        TransaccionDTOResponse result = transaccionService.registrarTransaccion(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
        verify(espacioTrabajoApi).aplicarMovimientoSaldo(espacioId, new BigDecimal("100.00"));
        verify(eventPublisher).publishEvent(any(TransaccionRegistradaEvent.class));
    }

    @Test
    void registrarTransaccion_cuandoCuentaBancariaNoNula_entoncesRegistroExitoso() {
        // Arrange
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setSaldoActual(new BigDecimal("500.00"));

        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("100.00"), TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 1L, 1L, 1L);

        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoTransaccion));
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(contactoTransferencia));
        when(contactoRepository.save(any(ContactoTransferencia.class))).thenAnswer(inv -> {
            ContactoTransferencia c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(cuentaBancariaService.actualizarCuentaBancaria(anyLong(), any(TipoTransaccion.class), any(BigDecimal.class))).thenReturn(cuentaBancaria);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion trans = invocation.getArgument(0);
            trans.setId(1L);
            return trans;
        });

        // Act
        TransaccionDTOResponse result = transaccionService.registrarTransaccion(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
        verify(cuentaBancariaService, times(1)).actualizarCuentaBancaria(1L, TipoTransaccion.INGRESO, new BigDecimal("100.00"));
        verify(espacioTrabajoApi).aplicarMovimientoSaldo(espacioId, new BigDecimal("100.00"));
    }

    @Test
    void registrarTransaccion_cuandoRegistroMensualExistente_entoncesPublicaEvento() {
        // Arrange
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("200.00"), TipoTransaccion.INGRESO, "Venta mayor", "Auditor", espacioId, 1L, null, null);

        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoTransaccion));
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        TransaccionDTOResponse result = transaccionService.registrarTransaccion(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        // La actualización del registro mensual se delega al DashboardEventListener via evento
        ArgumentCaptor<TransaccionRegistradaEvent> captor = ArgumentCaptor.forClass(TransaccionRegistradaEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(espacioId, captor.getValue().idEspacioTrabajo());
        assertEquals(new BigDecimal("200.00"), captor.getValue().monto());
    }

    @Test
    void registrarTransaccion_cuandoRegistroMensualAusente_entoncesPublicaEvento() {

        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), new BigDecimal("100.00"), TipoTransaccion.INGRESO, "Venta pequena", "Auditor", espacioId, 1L, null, null);

        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoTransaccion));
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        // Act
        TransaccionDTOResponse result = transaccionService.registrarTransaccion(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(eventPublisher).publishEvent(any(TransaccionRegistradaEvent.class));
    }

    // Tests para removerTransaccion

    @Test
    void removerTransaccion_cuandoTransaccionNoExiste_entoncesLanzaExcepcion() {
        when(transaccionRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.removerTransaccion(99L);
        });
        assertEquals("Transaccion con ID 99 no encontrada", exception.getMessage());
        verify(transaccionRepository, never()).delete(any(Transaccion.class));
    }

    @Test
    void removerTransaccion_cuandoOpcionCorrecta_entoncesRemueveTransaccionYActualizaEspacio() {
        Transaccion transaccion = Transaccion.builder()
            .id(1L)
            .tipo(TipoTransaccion.GASTO)
            .monto(new BigDecimal("50.00"))
            .fecha(LocalDate.now())
            .descripcion("Gasto Test")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .contacto(null)
            .build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        doNothing().when(transaccionRepository).delete(any(Transaccion.class));

        transaccionService.removerTransaccion(1L);

        verify(transaccionRepository, times(1)).findById(1L);
        verify(transaccionRepository, times(1)).delete(transaccion);
        // GASTO eliminado => delta positivo para revertir el saldo del espacio
        verify(espacioTrabajoApi).aplicarMovimientoSaldo(espacioId, new BigDecimal("50.00"));
        verify(eventPublisher).publishEvent(any(TransaccionEliminadaEvent.class));
    }

    @Test
    void removerTransaccion_cuandoCuentaBancariaNoNulaYTipoGasto_entoncesRemueveTransaccionYActualizaEspacio() {
        // Arrange
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setSaldoActual(new BigDecimal("400.00"));

        Transaccion transaccion = Transaccion.builder()
            .id(1L)
            .tipo(TipoTransaccion.GASTO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.now())
            .descripcion("Gasto Test")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .cuentaBancaria(cuentaBancaria)
            .build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        doNothing().when(transaccionRepository).delete(any(Transaccion.class));
        when(cuentaBancariaRepository.save(any(CuentaBancaria.class))).thenReturn(cuentaBancaria);

        // Act
        transaccionService.removerTransaccion(1L);

        // Assert
        verify(transaccionRepository, times(1)).findById(1L);
        verify(transaccionRepository, times(1)).delete(transaccion);
        verify(cuentaBancariaRepository, times(1)).save(cuentaBancaria);
        verify(espacioTrabajoApi).aplicarMovimientoSaldo(espacioId, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("500.00"), cuentaBancaria.getSaldoActual()); // 400 + 100 de reversión
    }

    @Test
    void removerTransaccion_cuandoCuentaBancariaNoNulaYTipoIngreso_entoncesRemueveTransaccionYActualizaEspacio() {
        // Arrange
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setSaldoActual(new BigDecimal("600.00"));

        Transaccion transaccion = Transaccion.builder()
            .id(1L)
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.now())
            .descripcion("Ingreso Test")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .cuentaBancaria(cuentaBancaria)
            .build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        doNothing().when(transaccionRepository).delete(any(Transaccion.class));
        when(cuentaBancariaRepository.save(any(CuentaBancaria.class))).thenReturn(cuentaBancaria);

        // Act
        transaccionService.removerTransaccion(1L);

        // Assert
        verify(transaccionRepository, times(1)).findById(1L);
        verify(transaccionRepository, times(1)).delete(transaccion);
        verify(cuentaBancariaRepository, times(1)).save(cuentaBancaria);
        // INGRESO eliminado => delta negativo para revertir el saldo del espacio
        verify(espacioTrabajoApi).aplicarMovimientoSaldo(espacioId, new BigDecimal("-100.00"));
        assertEquals(new BigDecimal("500.00"), cuentaBancaria.getSaldoActual()); // 600 - 100 de reversión
    }

    // Tests para buscarTransaccion

    @Test
    void buscarTransaccion_cuandoAnioNuloYMesNoNulo_entoncesLanzaExcepcion() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(1, null, null, null, espacioId, null, null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.buscarTransaccion(dto);
        });
        assertEquals("Si no se especifica el año, no se puede especificar el mes", exception.getMessage());
        verify(transaccionRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccion_cuandoBusquedaConAnio_entoncesBusquedaExitosa() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(null, 2023, null, null, espacioId, null, null);
        Transaccion t = Transaccion.builder()
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.of(2023, 1, 1))
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .build();
        List<Transaccion> transacciones = Collections.singletonList(t);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(transacciones));

        PaginatedResponse<TransaccionDTOResponse> result = transaccionService.buscarTransaccion(dto);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccion_cuandoBusquedaConAnioYMes_entoncesBusquedaExitosa() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(1, 2023, null, null, espacioId, null, null);
        Transaccion t = Transaccion.builder()
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.of(2023, 1, 1))
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .build();
        List<Transaccion> transacciones = Collections.singletonList(t);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(transacciones));

        PaginatedResponse<TransaccionDTOResponse> result = transaccionService.buscarTransaccion(dto);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccion_cuandoBusquedaConContacto_entoncesBusquedaExitosa() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(null, null, null, "Cliente A", espacioId, null, null);
        Transaccion t = Transaccion.builder()
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.now())
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .contacto(contactoTransferencia)
            .build();
        List<Transaccion> transacciones = Collections.singletonList(t);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(transacciones));

        PaginatedResponse<TransaccionDTOResponse> result = transaccionService.buscarTransaccion(dto);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccion_cuandoBusquedaConMotivo_entoncesBusquedaExitosa() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(null, null, "Venta", null, espacioId, null, null);
        Transaccion t = Transaccion.builder()
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.now())
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .build();
        List<Transaccion> transacciones = Collections.singletonList(t);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(transacciones));

        PaginatedResponse<TransaccionDTOResponse> result = transaccionService.buscarTransaccion(dto);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccion_cuandoBusquedaSinFiltros_entoncesBusquedaExitosa() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(null, null, null, null, espacioId, null, null);
        Transaccion t = Transaccion.builder()
            .tipo(TipoTransaccion.INGRESO)
            .monto(new BigDecimal("100.00"))
            .fecha(LocalDate.now())
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .idEspacioTrabajo(espacioId)
            .nombreEspacioTrabajo("Espacio de Prueba")
            .motivo(motivoTransaccion)
            .build();
        List<Transaccion> transacciones = Collections.singletonList(t);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(transacciones));

        PaginatedResponse<TransaccionDTOResponse> result = transaccionService.buscarTransaccion(dto);

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccion_cuandoBusquedaSinResultados_entoncesRetornaListaVacia() {
        TransaccionBusquedaDTO dto = new TransaccionBusquedaDTO(null, null, null, null, espacioId, null, null);
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        PaginatedResponse<TransaccionDTOResponse> result = transaccionService.buscarTransaccion(dto);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    // Tests para registrarContactoTransferencia

    @Test
    void registrarContactoTransferencia_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        ContactoDTORequest dto = new ContactoDTORequest("Nombre Contacto", UUID.fromString("00000000-0000-0000-0000-000000000099"));
        when(espacioTrabajoApi.existe(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarContactoTransferencia(dto);
        });
        assertEquals("Espacio de trabajo con ID 00000000-0000-0000-0000-000000000099 no encontrado", exception.getMessage());
        verify(contactoRepository, never()).save(any(ContactoTransferencia.class));
    }

    @Test
    void registrarContactoTransferencia_cuandoOpcionCorrecta_entoncesRegistroExitoso() {
        ContactoDTORequest dto = new ContactoDTORequest("Nuevo Contacto", espacioId);
        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(contactoRepository.findFirstByNombreAndIdEspacioTrabajo("Nuevo Contacto", espacioId))
                .thenReturn(Optional.empty());
        when(contactoRepository.save(any(ContactoTransferencia.class))).thenAnswer(invocation -> {
            ContactoTransferencia contacto = invocation.getArgument(0);
            contacto.setId(1L);
            return contacto;
        });

        ContactoDTOResponse result = transaccionService.registrarContactoTransferencia(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Nuevo Contacto", result.nombre());
        verify(contactoRepository, times(1)).save(any(ContactoTransferencia.class));
    }

    @Test
    void registrarContactoTransferencia_cuandoContactoDuplicado_entoncesLanzaExcepcion() {
        ContactoDTORequest dto = new ContactoDTORequest("Contacto Existente", espacioId);
        ContactoTransferencia contactoExistente = new ContactoTransferencia();
        contactoExistente.setId(10L);
        contactoExistente.setNombre("Contacto Existente");
        
        when(contactoRepository.findFirstByNombreAndIdEspacioTrabajo("Contacto Existente", espacioId))
                .thenReturn(Optional.of(contactoExistente));

        com.campito.backend.common.exception.EntidadDuplicadaException exception = 
                assertThrows(com.campito.backend.common.exception.EntidadDuplicadaException.class, () -> {
            transaccionService.registrarContactoTransferencia(dto);
        });
        
        assertTrue(exception.getMessage().contains("Ya existe un contacto con el nombre 'Contacto Existente'"));
        verify(contactoRepository, never()).save(any(ContactoTransferencia.class));
    }

    // Tests para nuevoMotivoTransaccion

    @Test
    void nuevoMotivoTransaccion_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        MotivoDTORequest dto = new MotivoDTORequest("Nuevo Motivo", UUID.fromString("00000000-0000-0000-0000-000000000099"));
        when(espacioTrabajoApi.existe(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.nuevoMotivoTransaccion(dto);
        });
        assertEquals("Espacio de trabajo con ID 00000000-0000-0000-0000-000000000099 no encontrado", exception.getMessage());
        verify(motivoRepository, never()).save(any(MotivoTransaccion.class));
    }

    @Test
    void nuevoMotivoTransaccion_cuandoOpcionCorrecta_entoncesRegistroExitoso() {
        MotivoDTORequest dto = new MotivoDTORequest("Nuevo Motivo", espacioId);
        when(espacioTrabajoApi.existe(espacioId)).thenReturn(true);
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Nuevo Motivo", espacioId))
                .thenReturn(Optional.empty());
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(invocation -> {
            MotivoTransaccion motivo = invocation.getArgument(0);
            motivo.setId(1L);
            return motivo;
        });

        MotivoDTOResponse result = transaccionService.nuevoMotivoTransaccion(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Nuevo Motivo", result.motivo());
        verify(motivoRepository, times(1)).save(any(MotivoTransaccion.class));
    }

    @Test
    void nuevoMotivoTransaccion_cuandoMotivoDuplicado_entoncesLanzaExcepcion() {
        MotivoDTORequest dto = new MotivoDTORequest("Motivo Existente", espacioId);
        MotivoTransaccion motivoExistente = new MotivoTransaccion();
        motivoExistente.setId(10L);
        motivoExistente.setMotivo("Motivo Existente");
        
        when(motivoRepository.findFirstByMotivoAndIdEspacioTrabajo("Motivo Existente", espacioId))
                .thenReturn(Optional.of(motivoExistente));

        com.campito.backend.common.exception.EntidadDuplicadaException exception = 
                assertThrows(com.campito.backend.common.exception.EntidadDuplicadaException.class, () -> {
            transaccionService.nuevoMotivoTransaccion(dto);
        });
        
        assertTrue(exception.getMessage().contains("Ya existe un motivo con el nombre 'Motivo Existente'"));
        verify(motivoRepository, never()).save(any(MotivoTransaccion.class));
    }

    // Tests para listarContactos

    @Test
    void listarContactos_cuandoNoExistenContactos_entoncesRetornaListaVacia() {
        when(contactoRepository.findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId)).thenReturn(Collections.emptyList());

        List<ContactoDTOResponse> result = transaccionService.listarContactos(espacioId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(contactoRepository, times(1)).findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId);
    }

    @Test
    void listarContactos_cuandoExistenContactos_entoncesRetornaListaConContactos() {
        List<ContactoTransferencia> contactos = new ArrayList<>();
        contactos.add(contactoTransferencia);
        when(contactoRepository.findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId)).thenReturn(contactos);

        List<ContactoDTOResponse> result = transaccionService.listarContactos(espacioId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(contactoTransferencia.getId(), result.get(0).id());
        assertEquals(contactoTransferencia.getNombre(), result.get(0).nombre());
        verify(contactoRepository, times(1)).findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId);
    }

    // Tests para listarMotivos

    @Test
    void listarMotivos_cuandoNoExistenMotivos_entoncesRetornaListaVacia() {
        when(motivoRepository.findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId)).thenReturn(Collections.emptyList());

        List<MotivoDTOResponse> result = transaccionService.listarMotivos(espacioId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(motivoRepository, times(1)).findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId);
    }

    @Test
    void listarMotivos_cuandoExistenMotivos_entoncesRetornaListaConMotivos() {
        List<MotivoTransaccion> motivos = new ArrayList<>();
        motivos.add(motivoTransaccion);
        when(motivoRepository.findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId)).thenReturn(motivos);

        List<MotivoDTOResponse> result = transaccionService.listarMotivos(espacioId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(motivoTransaccion.getId(), result.get(0).id());
        assertEquals(motivoTransaccion.getMotivo(), result.get(0).motivo());
        verify(motivoRepository, times(1)).findByIdEspacioTrabajoOrderByFechaModificacionDesc(espacioId);
    }

    // Tests para buscarTransaccionesRecientes

    @Test
    void buscarTransaccionesRecientes_cuandoNoExistenTransacciones_entoncesRetornaListaVacia() {
        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        List<TransaccionDTOResponse> result = transaccionService.buscarTransaccionesRecientes(espacioId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void buscarTransaccionesRecientes_cuandoOpcionCorrecta_entoncesRetornaUltimas6Transacciones() {
        List<Transaccion> transacciones = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Transaccion t = Transaccion.builder()
                .tipo(TipoTransaccion.INGRESO)
                .monto(new BigDecimal("100.00"))
                .fecha(LocalDate.now())
                .descripcion("Desc " + i)
                .nombreCompletoAuditoria("User")
                .fechaCreacion(LocalDateTime.now().minusMinutes(i))
                .idEspacioTrabajo(espacioId)
                .nombreEspacioTrabajo("Espacio de Prueba")
                .motivo(motivoTransaccion)
                .build();
            transacciones.add(t);
        }
        // Asegurarse de que las transacciones estén ordenadas por fechaCreacion descendente para el mock
        transacciones.sort((t1, t2) -> t2.getFechaCreacion().compareTo(t1.getFechaCreacion()));

        when(transaccionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(transacciones.subList(0, 6)));

        List<TransaccionDTOResponse> result = transaccionService.buscarTransaccionesRecientes(espacioId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(6, result.size());
        assertEquals("Desc 0", result.get(0).descripcion()); // La más reciente
        verify(transaccionRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }
}
