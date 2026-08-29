package com.campito.backend.descuentos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.descuentos.domain.dto.DescuentoDTORequest;
import com.campito.backend.descuentos.domain.dto.DescuentoDTOResponse;
import com.campito.backend.descuentos.domain.entity.Descuento;
import com.campito.backend.descuentos.mapper.DescuentoMapper;
import com.campito.backend.descuentos.repository.DescuentoRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class DescuentoServiceTest {

    @Mock
    private DescuentoRepository descuentoRepository;

    @Mock
    private DescuentoMapper descuentoMapper;

    @InjectMocks
    private DescuentoServiceImpl descuentoService;

    private UUID idEspacioTrabajo;
    private DescuentoDTORequest descuentoDTORequest;
    private Descuento descuento;
    private DescuentoDTOResponse descuentoDTOResponse;

    @BeforeEach
    void setUp() {
        idEspacioTrabajo = UUID.fromString("00000000-0000-0000-0000-000000000001");

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
            .idEspacioTrabajo(idEspacioTrabajo)
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
        when(descuentoMapper.toEntity(descuentoDTORequest)).thenReturn(descuento);
        when(descuentoRepository.save(any(Descuento.class))).thenReturn(descuento);
        when(descuentoMapper.toResponse(descuento)).thenReturn(descuentoDTOResponse);

        DescuentoDTOResponse result = descuentoService.crearDescuento(descuentoDTORequest);

        assertNotNull(result);
        assertEquals("Carrefour", result.comercio());

        ArgumentCaptor<Descuento> captor = ArgumentCaptor.forClass(Descuento.class);
        verify(descuentoRepository, times(1)).save(captor.capture());
        Descuento saved = captor.getValue();
        assertEquals(idEspacioTrabajo, saved.getIdEspacioTrabajo());

        verify(descuentoMapper, times(1)).toEntity(descuentoDTORequest);
        verify(descuentoMapper, times(1)).toResponse(descuento);
    }

    // =========================================================
    // listarDescuentos
    // =========================================================

    @Test
    void listarDescuentos_retornaListaCorrectamente() {
        when(descuentoRepository.findByIdEspacioTrabajoOrderByDiaAsc(idEspacioTrabajo))
            .thenReturn(List.of(descuento));
        when(descuentoMapper.toResponse(descuento)).thenReturn(descuentoDTOResponse);

        List<DescuentoDTOResponse> result = descuentoService.listarDescuentos(idEspacioTrabajo);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Carrefour", result.get(0).comercio());
        assertEquals("30%", result.get(0).porcentaje());

        verify(descuentoRepository, times(1)).findByIdEspacioTrabajoOrderByDiaAsc(idEspacioTrabajo);
    }

    @Test
    void listarDescuentos_sinDescuentos_retornaListaVacia() {
        when(descuentoRepository.findByIdEspacioTrabajoOrderByDiaAsc(idEspacioTrabajo))
            .thenReturn(List.of());

        List<DescuentoDTOResponse> result = descuentoService.listarDescuentos(idEspacioTrabajo);

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

        assertDoesNotThrow(() -> descuentoService.eliminarDescuento(1L));

        verify(descuentoRepository, times(1)).existsById(1L);
        verify(descuentoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarDescuento_noEncontrado_lanzaEntityNotFoundException() {
        when(descuentoRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
            () -> descuentoService.eliminarDescuento(99L));

        verify(descuentoRepository, never()).deleteById(any());
    }
}
