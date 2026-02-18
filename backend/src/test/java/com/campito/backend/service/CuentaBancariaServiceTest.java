package com.campito.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.dao.CuentaBancariaRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dto.CuentaBancariaDTORequest;
import com.campito.backend.dto.CuentaBancariaDTOResponse;
import com.campito.backend.mapper.CuentaBancariaMapper;
import com.campito.backend.model.CuentaBancaria;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.ProveedorAutenticacion;
import com.campito.backend.model.TipoTransaccion;
import com.campito.backend.model.Usuario;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class CuentaBancariaServiceTest {

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private EspacioTrabajoRepository espacioTrabajoRepository;

    @Mock
    private CuentaBancariaMapper cuentaBancariaMapper;

    @InjectMocks
    private CuentaBancariaServiceImpl cuentaBancariaService;

    private CuentaBancariaDTORequest cuentaBancariaDTO;
    private EspacioTrabajo espacioTrabajo;
    private CuentaBancaria cuentaBancaria;

    @BeforeEach
    void setUp() {
        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setNombre("Admin User");
        usuarioAdmin.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setFechaRegistro(LocalDateTime.now());

        espacioTrabajo = new EspacioTrabajo();
        espacioTrabajo.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        espacioTrabajo.setNombre("Mi Espacio de Trabajo");
        espacioTrabajo.setSaldo(BigDecimal.ZERO);
        espacioTrabajo.setUsuarioAdmin(usuarioAdmin);
        espacioTrabajo.setUsuariosParticipantes(List.of(usuarioAdmin));

        cuentaBancariaDTO = new CuentaBancariaDTORequest("Cuenta de Ahorros", "Banco A", espacioTrabajo.getId(), BigDecimal.ZERO);

        cuentaBancaria = new CuentaBancaria();
        cuentaBancaria.setId(1L);
        cuentaBancaria.setNombre("Cuenta de Ahorros");
        cuentaBancaria.setEntidadFinanciera("Banco A");
        cuentaBancaria.setSaldoActual(BigDecimal.ZERO);
        cuentaBancaria.setEspacioTrabajo(espacioTrabajo);
        
        lenient().when(cuentaBancariaMapper.toEntity(any(CuentaBancariaDTORequest.class))).thenAnswer(invocation -> {
            CuentaBancariaDTORequest dto = invocation.getArgument(0);
            CuentaBancaria cuenta = new CuentaBancaria();
            cuenta.setNombre(dto.nombre());
            cuenta.setEntidadFinanciera(dto.entidadFinanciera());
            cuenta.setSaldoActual(dto.saldoActual());
            return cuenta;
        });
        lenient().when(cuentaBancariaMapper.toResponse(any(CuentaBancaria.class))).thenAnswer(invocation -> {
            CuentaBancaria cuenta = invocation.getArgument(0);
            return new CuentaBancariaDTOResponse(cuenta.getId(), cuenta.getNombre(), cuenta.getEntidadFinanciera(), cuenta.getSaldoActual());
        });
    }

    // Tests para crearCuentaBancaria
    @Test
    void testCrearCuentaBancaria_cuandoDTOEsNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.crearCuentaBancaria(null);
        });
        verify(espacioTrabajoRepository, never()).findById(any());
        verify(cuentaBancariaRepository, never()).save(any());
    }

    @Test
    void testCrearCuentaBancaria_cuandoIdEspacioTrabajoEsNulo_lanzaEntityNotFound() {
        CuentaBancariaDTORequest dtoSinEspacio = new CuentaBancariaDTORequest("Nombre", "Entidad", null, BigDecimal.ZERO);
        assertThrows(EntityNotFoundException.class, () -> {
            cuentaBancariaService.crearCuentaBancaria(dtoSinEspacio);
        });
        verify(cuentaBancariaRepository, never()).save(any());
    }



    @Test
    void testCrearCuentaBancaria_cuandoEspacioTrabajoNoExiste_lanzaExcepcion() {
        when(espacioTrabajoRepository.findById(espacioTrabajo.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cuentaBancariaService.crearCuentaBancaria(cuentaBancariaDTO);
        });
        verify(cuentaBancariaRepository, never()).save(any());
    }

    @Test
    void testCrearCuentaBancaria_conDatosValidos_guardaCuenta() {
        when(espacioTrabajoRepository.findById(espacioTrabajo.getId())).thenReturn(Optional.of(espacioTrabajo));
        cuentaBancariaService.crearCuentaBancaria(cuentaBancariaDTO);
        verify(cuentaBancariaRepository, times(1)).save(any(CuentaBancaria.class));
    }

    @Test
    void testCrearCuentaBancaria_conDatosValidos_guardaCuentaConSaldoCeroYEspacioAsignado() {
        // Arrange
        when(espacioTrabajoRepository.findById(espacioTrabajo.getId())).thenReturn(Optional.of(espacioTrabajo));

        // Act
        cuentaBancariaService.crearCuentaBancaria(cuentaBancariaDTO);

        // Assert: capture the entity saved and validate fields set by the service
        var captor = org.mockito.ArgumentCaptor.forClass(CuentaBancaria.class);
        verify(cuentaBancariaRepository, times(1)).save(captor.capture());
        CuentaBancaria saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(0, new BigDecimal("0.00").compareTo(saved.getSaldoActual()));
        assertEquals(espacioTrabajo, saved.getEspacioTrabajo());
        assertEquals("Cuenta de Ahorros", saved.getNombre());
        assertEquals("Banco A", saved.getEntidadFinanciera());
    }

    // Tests para actualizarCuentaBancaria
    @Test
    void testActualizarCuentaBancaria_conIdNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.actualizarCuentaBancaria(null, TipoTransaccion.INGRESO, new BigDecimal("100.00"));
        });
    }

    @Test
    void testActualizarCuentaBancaria_conTipoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.actualizarCuentaBancaria(1L, null, new BigDecimal("100.00"));
        });
    }

    @Test
    void testActualizarCuentaBancaria_conMontoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.actualizarCuentaBancaria(1L, TipoTransaccion.INGRESO, null);
        });
    }

    @Test
    void testActualizarCuentaBancaria_conGastoIgualASaldo_actualizaASaldoCero() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        CuentaBancaria cuentaActualizada = cuentaBancariaService.actualizarCuentaBancaria(1L, TipoTransaccion.GASTO, new BigDecimal("1000.00"));
        assertEquals(0, new BigDecimal("0.00").compareTo(cuentaActualizada.getSaldoActual()));
        verify(cuentaBancariaRepository, times(1)).save(cuentaConSaldo);
    }

    @Test
    void testActualizarCuentaBancaria_cuandoCuentaNoExiste_lanzaExcepcion() {
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cuentaBancariaService.actualizarCuentaBancaria(1L, TipoTransaccion.INGRESO, new BigDecimal("100.00"));
        });
    }

    @Test
    void testActualizarCuentaBancaria_conGastoYSaldoInsuficiente_lanzaExcepcion() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        assertThrows(com.campito.backend.exception.SaldoInsuficienteException.class, () -> {
            cuentaBancariaService.actualizarCuentaBancaria(1L, TipoTransaccion.GASTO, new BigDecimal("2000.00"));
        });
    }

    @Test
    void testActualizarCuentaBancaria_conIngreso_actualizaSaldoCorrectamente() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        CuentaBancaria cuentaActualizada = cuentaBancariaService.actualizarCuentaBancaria(1L, TipoTransaccion.INGRESO, new BigDecimal("500.00"));
        assertEquals(new BigDecimal("1500.00"), cuentaActualizada.getSaldoActual());
        verify(cuentaBancariaRepository, times(1)).save(cuentaConSaldo);
    }

    @Test
    void testActualizarCuentaBancaria_conGastoValido_actualizaSaldoCorrectamente() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        CuentaBancaria cuentaActualizada = cuentaBancariaService.actualizarCuentaBancaria(1L, TipoTransaccion.GASTO, new BigDecimal("500.00"));
        assertEquals(new BigDecimal("500.00"), cuentaActualizada.getSaldoActual());
        verify(cuentaBancariaRepository, times(1)).save(cuentaConSaldo);
    }

    // Tests para listarCuentasBancarias
    @Test
    void testListarCuentasBancarias_conIdEspacioTrabajoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.listarCuentasBancarias(null);
        });
    }

    @Test
    void testListarCuentasBancarias_cuandoNoExistenCuentas_retornaListaVacia() {
        when(cuentaBancariaRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioTrabajo.getId())).thenReturn(Collections.emptyList());
        List<CuentaBancariaDTOResponse> resultado = cuentaBancariaService.listarCuentasBancarias(espacioTrabajo.getId());
        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }

    @Test
    void testListarCuentasBancarias_cuandoExistenCuentas_retornaListaDTOs() {
        when(cuentaBancariaRepository.findByEspacioTrabajo_IdOrderByFechaModificacionDesc(espacioTrabajo.getId())).thenReturn(List.of(cuentaBancaria));
        List<CuentaBancariaDTOResponse> resultado = cuentaBancariaService.listarCuentasBancarias(espacioTrabajo.getId());
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Cuenta de Ahorros", resultado.get(0).nombre());
    }

    // Tests para transaccionEntreCuentas
    @Test
    void testTransaccionEntreCuentas_conIdOrigenNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.transaccionEntreCuentas(null, 2L, new BigDecimal("100.00"));
        });
    }

    @Test
    void testTransaccionEntreCuentas_conIdDestinoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.transaccionEntreCuentas(1L, null, new BigDecimal("100.00"));
        });
    }

    @Test
    void testTransaccionEntreCuentas_conMontoNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> {
            cuentaBancariaService.transaccionEntreCuentas(1L, 2L, null);
        });
    }

    @Test
    void testTransaccionEntreCuentas_cuandoCuentaOrigenNoExiste_lanzaExcepcion() {
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cuentaBancariaService.transaccionEntreCuentas(1L, 2L, new BigDecimal("100.00"));
        });
    }

    @Test
    void testTransaccionEntreCuentas_cuandoCuentaDestinoNoExiste_lanzaExcepcion() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        when(cuentaBancariaRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            cuentaBancariaService.transaccionEntreCuentas(1L, 2L, new BigDecimal("100.00"));
        });
    }

    @Test
    void testTransaccionEntreCuentas_conSaldoInsuficiente_lanzaExcepcion() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        CuentaBancaria cuentaDestino = new CuentaBancaria();
        cuentaDestino.setId(2L);
        cuentaDestino.setSaldoActual(new BigDecimal("500.00"));

        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        when(cuentaBancariaRepository.findById(2L)).thenReturn(Optional.of(cuentaDestino));

        assertThrows(com.campito.backend.exception.SaldoInsuficienteException.class, () -> {
            cuentaBancariaService.transaccionEntreCuentas(1L, 2L, new BigDecimal("2000.00"));
        });
    }

    @Test
    void testTransaccionEntreCuentas_conDatosValidos_actualizaSaldosCorrectamente() {
        // Crear cuenta con saldo para este test
        CuentaBancaria cuentaConSaldo = new CuentaBancaria();
        cuentaConSaldo.setId(1L);
        cuentaConSaldo.setNombre("Cuenta de Ahorros");
        cuentaConSaldo.setEntidadFinanciera("Banco A");
        cuentaConSaldo.setSaldoActual(new BigDecimal("1000.00"));
        cuentaConSaldo.setEspacioTrabajo(espacioTrabajo);
        
        CuentaBancaria cuentaDestino = new CuentaBancaria();
        cuentaDestino.setId(2L);
        cuentaDestino.setSaldoActual(new BigDecimal("500.00"));

        when(cuentaBancariaRepository.findById(1L)).thenReturn(Optional.of(cuentaConSaldo));
        when(cuentaBancariaRepository.findById(2L)).thenReturn(Optional.of(cuentaDestino));

        cuentaBancariaService.transaccionEntreCuentas(1L, 2L, new BigDecimal("500.00"));

        assertEquals(new BigDecimal("500.00"), cuentaConSaldo.getSaldoActual());
        assertEquals(new BigDecimal("1000.00"), cuentaDestino.getSaldoActual());
        verify(cuentaBancariaRepository, times(1)).save(cuentaConSaldo);
        verify(cuentaBancariaRepository, times(1)).save(cuentaDestino);
    }
}