package com.campito.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.campito.backend.dao.DescuentoRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dto.DescuentoDTORequest;
import com.campito.backend.dto.DescuentoDTOResponse;
import com.campito.backend.mapper.CuentaBancariaMapper;
import com.campito.backend.mapper.DescuentoMapper;
import com.campito.backend.model.Descuento;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.ProveedorAutenticacion;
import com.campito.backend.model.Usuario;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DescuentoServiceTest {

    @Mock
    private DescuentoRepository descuentoRepository;

    @Mock
    private EspacioTrabajoRepository espacioTrabajoRepository;

    @Mock
    private CuentaBancariaRepository cuentaBancariaRepository;

    @Mock
    private CuentaBancariaMapper cuentaBancariaMapper;

    @Mock
    private DescuentoMapper descuentoMapper;

    @InjectMocks
    private CuentaBancariaServiceImpl cuentaBancariaService;

    private UUID idEspacioTrabajo;
    private EspacioTrabajo espacioTrabajo;
    private DescuentoDTORequest descuentoDTORequest;
    private Descuento descuento;
    private DescuentoDTOResponse descuentoDTOResponse;

    @BeforeEach
    void setUp() {
        idEspacioTrabajo = UUID.fromString("00000000-0000-0000-0000-000000000001");

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setNombre("Admin User");
        usuarioAdmin.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setFechaRegistro(LocalDateTime.now());

        espacioTrabajo = new EspacioTrabajo();
        espacioTrabajo.setId(idEspacioTrabajo);
        espacioTrabajo.setNombre("Mi Espacio de Trabajo");
        espacioTrabajo.setSaldo(BigDecimal.ZERO);
        espacioTrabajo.setUsuarioAdmin(usuarioAdmin);
        espacioTrabajo.setUsuariosParticipantes(List.of(usuarioAdmin));

        descuentoDTORequest = new DescuentoDTORequest(
            "Lunes",
            "Santa Fe",
            "Galicia",
            false,
            "30%",
            "Carrefour",
            "Débito",
            "5000",
            true,
            "Solo productos seleccionados",
            idEspacioTrabajo
        );

        descuento = Descuento.builder()
            .id(1L)
            .dia("Lunes")
            .localidad("Santa Fe")
            .banco("Galicia")
            .modo(false)
            .porcentaje("30%")
            .comercio("Carrefour")
            .modoPago("Débito")
            .topeReintegro("5000")
            .esSemanal(true)
            .comentario("Solo productos seleccionados")
            .espacioTrabajo(espacioTrabajo)
            .build();

        descuentoDTOResponse = new DescuentoDTOResponse(
            1L, "Lunes", "Santa Fe", "Galicia", false,
            "30%", "Carrefour", "Débito", "5000", true,
            "Solo productos seleccionados", idEspacioTrabajo
        );
    }

    // =========================================================
    // crearDescuento
    // =========================================================

    @Test
    void crearDescuento_exitoso() {
        when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.of(espacioTrabajo));
        when(descuentoMapper.toEntity(descuentoDTORequest)).thenReturn(descuento);
        when(descuentoRepository.save(any(Descuento.class))).thenReturn(descuento);

        assertDoesNotThrow(() -> cuentaBancariaService.crearDescuento(descuentoDTORequest));

        verify(espacioTrabajoRepository, times(1)).findById(idEspacioTrabajo);
        verify(descuentoMapper, times(1)).toEntity(descuentoDTORequest);
        verify(descuentoRepository, times(1)).save(any(Descuento.class));
    }

    @Test
    void crearDescuento_espacioNoEncontrado_lanzaEntityNotFoundException() {
        when(espacioTrabajoRepository.findById(idEspacioTrabajo)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> cuentaBancariaService.crearDescuento(descuentoDTORequest));

        verify(descuentoRepository, never()).save(any());
    }

    // =========================================================
    // listarDescuentos
    // =========================================================

    @Test
    void listarDescuentos_retornaListaCorrectamente() {
        when(descuentoRepository.findByEspacioTrabajo_IdOrderByDiaAsc(idEspacioTrabajo))
            .thenReturn(List.of(descuento));
        when(descuentoMapper.toResponse(descuento)).thenReturn(descuentoDTOResponse);

        List<DescuentoDTOResponse> result = cuentaBancariaService.listarDescuentos(idEspacioTrabajo);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Carrefour", result.get(0).comercio());
        assertEquals("30%", result.get(0).porcentaje());

        verify(descuentoRepository, times(1)).findByEspacioTrabajo_IdOrderByDiaAsc(idEspacioTrabajo);
    }

    @Test
    void listarDescuentos_sinDescuentos_retornaListaVacia() {
        when(descuentoRepository.findByEspacioTrabajo_IdOrderByDiaAsc(idEspacioTrabajo))
            .thenReturn(List.of());

        List<DescuentoDTOResponse> result = cuentaBancariaService.listarDescuentos(idEspacioTrabajo);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================
    // eliminarDescuento
    // =========================================================

    @Test
    void eliminarDescuento_exitoso() {
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(descuentoRepository).deleteById(1L);

        assertDoesNotThrow(() -> cuentaBancariaService.eliminarDescuento(1L));

        verify(descuentoRepository, times(1)).existsById(1L);
        verify(descuentoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarDescuento_noEncontrado_lanzaEntityNotFoundException() {
        when(descuentoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
            () -> cuentaBancariaService.eliminarDescuento(99L));

        verify(descuentoRepository, never()).deleteById(any());
    }
}
