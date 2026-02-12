package com.campito.backend.service;

import com.campito.backend.dao.*;
import com.campito.backend.dto.*;
import com.campito.backend.mapper.ContactoTransferenciaMapper;
import com.campito.backend.mapper.MotivoTransaccionMapper;
import com.campito.backend.mapper.TransaccionMapper;
import com.campito.backend.model.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
public class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;
    @Mock
    private EspacioTrabajoRepository espacioRepository;
    @Mock
    private MotivoTransaccionRepository motivoRepository;
    @Mock
    private ContactoTransferenciaRepository contactoRepository;
    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private CuentaBancariaService cuentaBancariaService;

    @Mock
    private GastosIngresosMensualesRepository gastosIngresosMensualesRepository;
    
    @Mock
    private TransaccionMapper transaccionMapper;
    @Mock
    private ContactoTransferenciaMapper contactoTransferenciaMapper;
    @Mock
    private MotivoTransaccionMapper motivoTransaccionMapper;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private EspacioTrabajo espacioTrabajo;
    private MotivoTransaccion motivoTransaccion;
    private ContactoTransferencia contactoTransferencia;
    private Usuario usuarioAdmin;

    // UUIDs para pruebas
    private UUID espacioId;

    @BeforeEach
    void setUp() {
        // Usar SimpleMeterRegistry real para evitar problemas con mocks
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        transaccionService = new TransaccionServiceImpl(
            transaccionRepository,
            espacioRepository,
            motivoRepository,
            contactoRepository,
            cuentaBancariaRepository,
            gastosIngresosMensualesRepository,
            cuentaBancariaService,
            transaccionMapper,
            contactoTransferenciaMapper,
            motivoTransaccionMapper,
            meterRegistry
        );
        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setFotoPerfil("foto.jpg");
        usuarioAdmin.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioAdmin.setIdProveedor("123");
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setFechaRegistro(LocalDateTime.now());
        usuarioAdmin.setFechaUltimoAcceso(LocalDateTime.now());

        espacioTrabajo = new EspacioTrabajo();
        espacioTrabajo.setId(espacioId = UUID.fromString("00000000-0000-0000-0000-000000000002"));
        espacioTrabajo.setNombre("Espacio de Prueba");
        espacioTrabajo.setSaldo(1000.0f);
        espacioTrabajo.setUsuarioAdmin(usuarioAdmin);

        motivoTransaccion = new MotivoTransaccion();
        motivoTransaccion.setId(1L);
        motivoTransaccion.setMotivo("Venta");
        motivoTransaccion.setEspacioTrabajo(espacioTrabajo);

        contactoTransferencia = new ContactoTransferencia();
        contactoTransferencia.setId(1L);
        contactoTransferencia.setNombre("Cliente A");
        contactoTransferencia.setEspacioTrabajo(espacioTrabajo);
        
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
                t.getEspacioTrabajo() != null ? t.getEspacioTrabajo().getId() : null,
                t.getEspacioTrabajo() != null ? t.getEspacioTrabajo().getNombre() : null,
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

        // Default: no GastosIngresosMensuales record for current year/month unless test overrides
        // Lenient because not every test needs a monthly registro; tests will override when necessary
        lenient().when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(any(UUID.class), anyInt(), anyInt()))
                .thenReturn(Optional.empty());
    }

    // Tests para registrarTransaccion

    @Test
    void registrarTransaccion_cuandoTransaccionDTONulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.registrarTransaccion(null);
        });
        assertEquals("La transaccion no puede ser nula", exception.getMessage());
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }



    @Test
    void registrarTransaccion_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 100f, TipoTransaccion.INGRESO, "Desc", "Auditor", UUID.fromString("00000000-0000-0000-0000-000000000099"), 1L, null, null);
        when(espacioRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarTransaccion(dto);
        });
        assertEquals("Espacio de trabajo con ID 00000000-0000-0000-0000-000000000099 no encontrado", exception.getMessage());
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void registrarTransaccion_cuandoMotivoNoExiste_entoncesLanzaExcepcion() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 100f, TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 99L, null, null);
        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
        when(motivoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarTransaccion(dto);
        });
        assertEquals("Motivo de transaccion con ID 99 no encontrado", exception.getMessage());
        verify(transaccionRepository, never()).save(any(Transaccion.class));
    }

    @Test
    void registrarTransaccion_cuandoIdContactoExistePeroContactoNoExiste_entoncesLanzaExcepcion() {
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 100f, TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 1L, 99L, null);
        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
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
        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 100f, TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 1L, 1L, null);
        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
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
        when(espacioRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);

        TransaccionDTOResponse result = transaccionService.registrarTransaccion(dto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
        verify(espacioRepository, times(1)).save(any(EspacioTrabajo.class));
        assertEquals(1100.0f, espacioTrabajo.getSaldo()); // 1000 inicial + 100 de ingreso
    }

    @Test
    void registrarTransaccion_cuandoCuentaBancariaNoNula_entoncesRegistroExitoso() {
        // Arrange
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setSaldoActual(500.0f);

        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 100f, TipoTransaccion.INGRESO, "Desc", "Auditor", espacioId, 1L, 1L, 1L);

        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
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
        when(cuentaBancariaService.actualizarCuentaBancaria(anyLong(), any(TipoTransaccion.class), anyFloat())).thenReturn(cuentaBancaria);
        when(transaccionRepository.save(any(Transaccion.class))).thenAnswer(invocation -> {
            Transaccion trans = invocation.getArgument(0);
            trans.setId(1L);
            return trans;
        });
        when(espacioRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);

        // Act
        TransaccionDTOResponse result = transaccionService.registrarTransaccion(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(transaccionRepository, times(1)).save(any(Transaccion.class));
        verify(espacioRepository, times(1)).save(any(EspacioTrabajo.class));
        verify(cuentaBancariaService, times(1)).actualizarCuentaBancaria(1L, TipoTransaccion.INGRESO, 100f);
        assertEquals(1100.0f, espacioTrabajo.getSaldo());
    }

    @Test
    void registrarTransaccion_cuandoRegistroMensualExistente_entoncesActualizaRegistroMensual() {
        // Arrange
        int año = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getYear();
        int mes = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getMonthValue();
        GastosIngresosMensuales registro = GastosIngresosMensuales.builder()
                .anio(año)
                .mes(mes)
                .gastos(0f)
                .ingresos(0f)
                .espacioTrabajo(espacioTrabajo)
                .build();

        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 200f, TipoTransaccion.INGRESO, "Venta mayor", "Auditor", espacioId, 1L, null, null);

        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
        when(motivoRepository.findById(1L)).thenReturn(Optional.of(motivoTransaccion));
        when(motivoRepository.save(any(MotivoTransaccion.class))).thenAnswer(inv -> {
            MotivoTransaccion m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(espacioId, año, mes)).thenReturn(Optional.of(registro));
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
        // registro debe haber sido actualizado y guardado
        verify(gastosIngresosMensualesRepository, times(1)).save(any(GastosIngresosMensuales.class));
        // El saldo del espacio se actualizó
        assertEquals(1200.0f, espacioTrabajo.getSaldo());
    }

    @Test
    void registrarTransaccion_cuandoRegistroMensualAusente_entoncesCreaRegistro() {

        TransaccionDTORequest dto = new TransaccionDTORequest(LocalDate.now(), 100f, TipoTransaccion.INGRESO, "Venta pequena", "Auditor", espacioId, 1L, null, null);

        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
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
        verify(gastosIngresosMensualesRepository, times(1)).save(any(GastosIngresosMensuales.class));
        assertEquals(1100.0f, espacioTrabajo.getSaldo());
    }

    // Tests para removerTransaccion

    @Test
    void removerTransaccion_cuandoIdNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.removerTransaccion(null);
        });
        assertEquals("El ID de la transacción no puede ser nulo", exception.getMessage());
        verify(transaccionRepository, never()).delete(any(Transaccion.class));
    }

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
            .monto(50.0f)
            .fecha(LocalDate.now())
            .descripcion("Gasto Test")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
            .motivo(motivoTransaccion)
            .contacto(null)
            .build();
        espacioTrabajo.setSaldo(950.0f); // Saldo después del gasto

        int año = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getYear();
        int mes = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getMonthValue();
        GastosIngresosMensuales registro = GastosIngresosMensuales.builder().anio(año).mes(mes).gastos(50f).ingresos(0f).espacioTrabajo(espacioTrabajo).build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        doNothing().when(transaccionRepository).delete(any(Transaccion.class));
        when(espacioRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(espacioId, año, mes)).thenReturn(Optional.of(registro));

        transaccionService.removerTransaccion(1L);

        verify(transaccionRepository, times(1)).findById(1L);
        verify(transaccionRepository, times(1)).delete(transaccion);
        verify(espacioRepository, times(1)).save(espacioTrabajo);
        assertEquals(1000.0f, espacioTrabajo.getSaldo()); // 950 + 50 de reversión
    }

    @Test
    void removerTransaccion_cuandoCuentaBancariaNoNulaYTipoGasto_entoncesRemueveTransaccionYActualizaEspacio() {
        // Arrange
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setSaldoActual(400.0f);

        Transaccion transaccion = Transaccion.builder()
            .id(1L)
            .tipo(TipoTransaccion.GASTO)
            .monto(100.0f)
            .fecha(LocalDate.now())
            .descripcion("Gasto Test")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
            .motivo(motivoTransaccion)
            .cuentaBancaria(cuentaBancaria)
            .build();
        espacioTrabajo.setSaldo(900.0f); // Saldo después del gasto

        int año = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getYear();
        int mes = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getMonthValue();
        GastosIngresosMensuales registro = GastosIngresosMensuales.builder().anio(año).mes(mes).gastos(100f).ingresos(0f).espacioTrabajo(espacioTrabajo).build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        doNothing().when(transaccionRepository).delete(any(Transaccion.class));
        when(espacioRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);
        when(cuentaBancariaRepository.save(any(CuentaBancaria.class))).thenReturn(cuentaBancaria);
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(espacioId, año, mes)).thenReturn(Optional.of(registro));

        // Act
        transaccionService.removerTransaccion(1L);

        // Assert
        verify(transaccionRepository, times(1)).findById(1L);
        verify(transaccionRepository, times(1)).delete(transaccion);
        verify(espacioRepository, times(1)).save(espacioTrabajo);
        verify(cuentaBancariaRepository, times(1)).save(cuentaBancaria);
        assertEquals(1000.0f, espacioTrabajo.getSaldo()); // 900 + 100 de reversión
        assertEquals(500.0f, cuentaBancaria.getSaldoActual()); // 400 + 100 de reversión
    }

    @Test
    void removerTransaccion_cuandoCuentaBancariaNoNulaYTipoIngreso_entoncesRemueveTransaccionYActualizaEspacio() {
        // Arrange
        CuentaBancaria cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setSaldoActual(600.0f);

        Transaccion transaccion = Transaccion.builder()
            .id(1L)
            .tipo(TipoTransaccion.INGRESO)
            .monto(100.0f)
            .fecha(LocalDate.now())
            .descripcion("Ingreso Test")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
            .motivo(motivoTransaccion)
            .cuentaBancaria(cuentaBancaria)
            .build();
        espacioTrabajo.setSaldo(1100.0f); // Saldo después del ingreso

        int año = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getYear();
        int mes = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getMonthValue();
        GastosIngresosMensuales registro = GastosIngresosMensuales.builder().anio(año).mes(mes).gastos(0f).ingresos(200f).espacioTrabajo(espacioTrabajo).build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        doNothing().when(transaccionRepository).delete(any(Transaccion.class));
        when(espacioRepository.save(any(EspacioTrabajo.class))).thenReturn(espacioTrabajo);
        when(cuentaBancariaRepository.save(any(CuentaBancaria.class))).thenReturn(cuentaBancaria);
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(espacioId, año, mes)).thenReturn(Optional.of(registro));

        // Act
        transaccionService.removerTransaccion(1L);

        // Assert
        verify(transaccionRepository, times(1)).findById(1L);
        verify(transaccionRepository, times(1)).delete(transaccion);
        verify(espacioRepository, times(1)).save(espacioTrabajo);
        verify(cuentaBancariaRepository, times(1)).save(cuentaBancaria);
        assertEquals(1000.0f, espacioTrabajo.getSaldo()); // 1100 - 100 de reversión
        assertEquals(500.0f, cuentaBancaria.getSaldoActual()); // 600 - 100 de reversión
    }

    @Test
    void removerTransaccion_cuandoRegistroMensualInsuficiente_entoncesLanzaExcepcion() {
        // Arrange
        int año = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getYear();
        int mes = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getMonthValue();

        GastosIngresosMensuales registro = GastosIngresosMensuales.builder()
                .anio(año)
                .mes(mes)
                .gastos(50f)
                .ingresos(0f)
                .espacioTrabajo(espacioTrabajo)
                .build();

        Transaccion transaccion = Transaccion.builder()
            .id(1L)
            .tipo(TipoTransaccion.GASTO)
            .monto(100.0f)
            .fecha(LocalDate.now())
            .descripcion("Gasto grande")
            .nombreCompletoAuditoria("Auditor")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
            .motivo(motivoTransaccion)
            .build();

        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        when(gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(espacioId, año, mes)).thenReturn(Optional.of(registro));

        // Act & Assert
        assertThrows(com.campito.backend.exception.SaldoInsuficienteException.class, () -> transaccionService.removerTransaccion(1L));
        verify(transaccionRepository, never()).delete(any(Transaccion.class));
        verify(gastosIngresosMensualesRepository, never()).save(any(GastosIngresosMensuales.class));
    }

    // Tests para buscarTransaccion

    @Test
    void buscarTransaccion_cuandoDatosBusquedaNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.buscarTransaccion(null);
        });
        assertEquals("Los datos de búsqueda no pueden ser nulos", exception.getMessage());
        verify(transaccionRepository, never()).findAll(any(Specification.class));
    }


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
            .monto(100f)
            .fecha(LocalDate.of(2023, 1, 1))
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
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
            .monto(100f)
            .fecha(LocalDate.of(2023, 1, 1))
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
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
            .monto(100f)
            .fecha(LocalDate.now())
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
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
            .monto(100f)
            .fecha(LocalDate.now())
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
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
            .monto(100f)
            .fecha(LocalDate.now())
            .descripcion("Test")
            .nombreCompletoAuditoria("User")
            .fechaCreacion(LocalDateTime.now())
            .espacioTrabajo(espacioTrabajo)
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
    void registrarContactoTransferencia_cuandoContactoDTONulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.registrarContactoTransferencia(null);
        });
        assertEquals("El contacto no puede ser nulo", exception.getMessage());
        verify(contactoRepository, never()).save(any(ContactoTransferencia.class));
    }



    @Test
    void registrarContactoTransferencia_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        ContactoDTORequest dto = new ContactoDTORequest("Nombre Contacto", UUID.fromString("00000000-0000-0000-0000-000000000099"));
        when(espacioRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.registrarContactoTransferencia(dto);
        });
        assertEquals("Espacio de trabajo con ID 00000000-0000-0000-0000-000000000099 no encontrado", exception.getMessage());
        verify(contactoRepository, never()).save(any(ContactoTransferencia.class));
    }

    @Test
    void registrarContactoTransferencia_cuandoOpcionCorrecta_entoncesRegistroExitoso() {
        ContactoDTORequest dto = new ContactoDTORequest("Nuevo Contacto", espacioId);
        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
        when(contactoRepository.findFirstByNombreAndEspacioTrabajo_Id("Nuevo Contacto", espacioId))
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
        
        when(contactoRepository.findFirstByNombreAndEspacioTrabajo_Id("Contacto Existente", espacioId))
                .thenReturn(Optional.of(contactoExistente));

        com.campito.backend.exception.EntidadDuplicadaException exception = 
                assertThrows(com.campito.backend.exception.EntidadDuplicadaException.class, () -> {
            transaccionService.registrarContactoTransferencia(dto);
        });
        
        assertTrue(exception.getMessage().contains("Ya existe un contacto con el nombre 'Contacto Existente'"));
        verify(contactoRepository, never()).save(any(ContactoTransferencia.class));
    }

    // Tests para nuevoMotivoTransaccion

    @Test
    void nuevoMotivoTransaccion_cuandoMotivoDTONulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.nuevoMotivoTransaccion(null);
        });
        assertEquals("El motivo no puede ser nulo", exception.getMessage());
        verify(motivoRepository, never()).save(any(MotivoTransaccion.class));
    }



    @Test
    void nuevoMotivoTransaccion_cuandoEspacioTrabajoNoExiste_entoncesLanzaExcepcion() {
        MotivoDTORequest dto = new MotivoDTORequest("Nuevo Motivo", UUID.fromString("00000000-0000-0000-0000-000000000099"));
        when(espacioRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000099"))).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            transaccionService.nuevoMotivoTransaccion(dto);
        });
        assertEquals("Espacio de trabajo con ID 00000000-0000-0000-0000-000000000099 no encontrado", exception.getMessage());
        verify(motivoRepository, never()).save(any(MotivoTransaccion.class));
    }

    @Test
    void nuevoMotivoTransaccion_cuandoOpcionCorrecta_entoncesRegistroExitoso() {
        MotivoDTORequest dto = new MotivoDTORequest("Nuevo Motivo", espacioId);
        when(espacioRepository.findById(espacioId)).thenReturn(Optional.of(espacioTrabajo));
        when(motivoRepository.findFirstByMotivoAndEspacioTrabajo_Id("Nuevo Motivo", espacioId))
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
        
        when(motivoRepository.findFirstByMotivoAndEspacioTrabajo_Id("Motivo Existente", espacioId))
                .thenReturn(Optional.of(motivoExistente));

        com.campito.backend.exception.EntidadDuplicadaException exception = 
                assertThrows(com.campito.backend.exception.EntidadDuplicadaException.class, () -> {
            transaccionService.nuevoMotivoTransaccion(dto);
        });
        
        assertTrue(exception.getMessage().contains("Ya existe un motivo con el nombre 'Motivo Existente'"));
        verify(motivoRepository, never()).save(any(MotivoTransaccion.class));
    }

    // Tests para listarContactos

    @Test
    void listarContactos_cuandoIdEspacioTrabajoNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.listarContactos(null);
        });
        assertEquals("El id del espacio de trabajo no puede ser nulo", exception.getMessage());
        verify(contactoRepository, never()).findByEspacioTrabajo_Id(any(UUID.class));
    }

    @Test
    void listarContactos_cuandoNoExistenContactos_entoncesRetornaListaVacia() {
        when(contactoRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId)).thenReturn(Collections.emptyList());

        List<ContactoDTOResponse> result = transaccionService.listarContactos(espacioId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(contactoRepository, times(1)).findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId);
    }

    @Test
    void listarContactos_cuandoExistenContactos_entoncesRetornaListaConContactos() {
        List<ContactoTransferencia> contactos = new ArrayList<>();
        contactos.add(contactoTransferencia);
        when(contactoRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId)).thenReturn(contactos);

        List<ContactoDTOResponse> result = transaccionService.listarContactos(espacioId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(contactoTransferencia.getId(), result.get(0).id());
        assertEquals(contactoTransferencia.getNombre(), result.get(0).nombre());
        verify(contactoRepository, times(1)).findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId);
    }

    // Tests para listarMotivos

    @Test
    void listarMotivos_cuandoIdEspacioTrabajoNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.listarMotivos(null);
        });
        assertEquals("El id del espacio de trabajo no puede ser nulo", exception.getMessage());
        verify(motivoRepository, never()).findByEspacioTrabajo_Id(any(UUID.class));
    }

    @Test
    void listarMotivos_cuandoNoExistenMotivos_entoncesRetornaListaVacia() {
        when(motivoRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId)).thenReturn(Collections.emptyList());

        List<MotivoDTOResponse> result = transaccionService.listarMotivos(espacioId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(motivoRepository, times(1)).findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId);
    }

    @Test
    void listarMotivos_cuandoExistenMotivos_entoncesRetornaListaConMotivos() {
        List<MotivoTransaccion> motivos = new ArrayList<>();
        motivos.add(motivoTransaccion);
        when(motivoRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId)).thenReturn(motivos);

        List<MotivoDTOResponse> result = transaccionService.listarMotivos(espacioId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(motivoTransaccion.getId(), result.get(0).id());
        assertEquals(motivoTransaccion.getMotivo(), result.get(0).motivo());
        verify(motivoRepository, times(1)).findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioId);
    }

    // Tests para buscarTransaccionesRecientes

    @Test
    void buscarTransaccionesRecientes_cuandoIdEspacioTrabajoNulo_entoncesLanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transaccionService.buscarTransaccionesRecientes(null);
        });
        assertEquals("El id del espacio de trabajo no puede ser nulo", exception.getMessage());
        verify(transaccionRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

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
                .monto(100f)
                .fecha(LocalDate.now())
                .descripcion("Desc " + i)
                .nombreCompletoAuditoria("User")
                .fechaCreacion(LocalDateTime.now().minusMinutes(i))
                .espacioTrabajo(espacioTrabajo)
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