package com.campito.backend.usuarios.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.usuarios.domain.entity.ProveedorAutenticacion;
import com.campito.backend.usuarios.domain.entity.Usuario;
import com.campito.backend.usuarios.repository.EspacioTrabajoRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class EspacioTrabajoApiTest {

    @Mock
    private EspacioTrabajoRepository espacioTrabajoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EspacioTrabajoApiImpl espacioTrabajoApi;

    private UUID idEspacio;
    private Usuario usuarioAdmin;
    private EspacioTrabajo espacio;

    @BeforeEach
    void setUp() {
        idEspacio = UUID.fromString("00000000-0000-0000-0000-000000000001");

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(UUID.fromString("00000000-0000-0000-0000-000000000099"));
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setProveedor(ProveedorAutenticacion.MANUAL);
        usuarioAdmin.setRol("ADMIN");
        usuarioAdmin.setActivo(true);

        espacio = EspacioTrabajo.builder()
            .id(idEspacio)
            .nombre("Mi Espacio")
            .saldo(new BigDecimal("100.00"))
            .usuarioAdmin(usuarioAdmin)
            .build();
    }

    @Test
    void existe_espacioExistente_devuelveTrue() {
        when(espacioTrabajoRepository.existsById(idEspacio)).thenReturn(true);
        assertTrue(espacioTrabajoApi.existe(idEspacio));
    }

    @Test
    void existe_espacioInexistente_devuelveFalse() {
        when(espacioTrabajoRepository.existsById(idEspacio)).thenReturn(false);
        assertFalse(espacioTrabajoApi.existe(idEspacio));
    }

    @Test
    void obtenerSaldo_espacioExistente_devuelveSaldo() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.of(espacio));
        assertEquals(0, new BigDecimal("100.00").compareTo(espacioTrabajoApi.obtenerSaldo(idEspacio)));
    }

    @Test
    void obtenerNombre_espacioExistente_devuelveNombre() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.of(espacio));
        assertEquals("Mi Espacio", espacioTrabajoApi.obtenerNombre(idEspacio));
    }

    @Test
    void obtenerIdUsuarioAdmin_devuelveAdmin() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.of(espacio));
        assertEquals(usuarioAdmin.getId(), espacioTrabajoApi.obtenerIdUsuarioAdmin(idEspacio));
    }

    @Test
    void aplicarMovimientoSaldo_deltaPositivo_sumaAlSaldo() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.of(espacio));
        when(espacioTrabajoRepository.save(any(EspacioTrabajo.class))).thenAnswer(inv -> inv.getArgument(0));

        espacioTrabajoApi.aplicarMovimientoSaldo(idEspacio, new BigDecimal("50.00"));

        assertEquals(0, new BigDecimal("150.00").compareTo(espacio.getSaldo()));
        verify(espacioTrabajoRepository).save(espacio);
    }

    @Test
    void aplicarMovimientoSaldo_deltaNegativo_restaAlSaldo() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.of(espacio));
        when(espacioTrabajoRepository.save(any(EspacioTrabajo.class))).thenAnswer(inv -> inv.getArgument(0));

        espacioTrabajoApi.aplicarMovimientoSaldo(idEspacio, new BigDecimal("-30.00"));

        assertEquals(0, new BigDecimal("70.00").compareTo(espacio.getSaldo()));
        verify(espacioTrabajoRepository).save(espacio);
    }

    @Test
    void aplicarMovimientoSaldo_espacioNoExiste_lanzaEntityNotFound() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> espacioTrabajoApi.aplicarMovimientoSaldo(idEspacio, BigDecimal.ONE));
    }

    @Test
    void obtenerSaldo_espacioNoExiste_lanzaEntityNotFound() {
        when(espacioTrabajoRepository.findById(idEspacio)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> espacioTrabajoApi.obtenerSaldo(idEspacio));
    }
}
