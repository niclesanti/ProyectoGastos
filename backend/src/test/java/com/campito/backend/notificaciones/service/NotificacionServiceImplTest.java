package com.campito.backend.notificaciones.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.campito.backend.common.event.TipoNotificacion;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.common.test.NotificacionesTestDataFactory;
import com.campito.backend.config.MetricsConfig;
import com.campito.backend.notificaciones.domain.entity.Notificacion;
import com.campito.backend.notificaciones.domain.dto.NotificacionDTOResponse;
import com.campito.backend.notificaciones.mapper.NotificacionMapper;
import com.campito.backend.notificaciones.repository.NotificacionRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private NotificacionMapper notificacionMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ReflectionTestUtils.setField(notificacionService, "meterRegistry", meterRegistry);
        userId = TestIds.USUARIO_ADMIN_ID;
    }

    @Test
    void obtenerNotificacionesUsuario_retornaLista() {
        List<Notificacion> notificaciones = List.of(
            NotificacionesTestDataFactory.crearNotificacion(1L),
            NotificacionesTestDataFactory.crearNotificacion(2L)
        );
        List<NotificacionDTOResponse> expectedResponses = List.of(
            NotificacionesTestDataFactory.crearNotificacionResponse(1L),
            NotificacionesTestDataFactory.crearNotificacionResponse(2L)
        );

        when(notificacionRepository.findTop50ByIdUsuarioOrderByFechaCreacionDesc(userId))
            .thenReturn(notificaciones);
        when(notificacionMapper.toResponseList(notificaciones)).thenReturn(expectedResponses);

        List<NotificacionDTOResponse> result = notificacionService.obtenerNotificacionesUsuario(userId);

        assertEquals(2, result.size());
    }

    @Test
    void contarNoLeidas_retornaContador() {
        when(notificacionRepository.countByIdUsuarioAndLeidaFalse(userId)).thenReturn(5L);

        Long count = notificacionService.contarNoLeidas(userId);

        assertEquals(5L, count);
    }

    @Test
    void marcarComoLeida_notificacionNoLeida_marcaCorrectamente() {
        Notificacion notificacion = NotificacionesTestDataFactory.crearNotificacion(1L);
        notificacion.setLeida(false);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        notificacionService.marcarComoLeida(1L);

        assertTrue(notificacion.getLeida());
        assertNotNull(notificacion.getFechaLeida());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    void marcarComoLeida_notificacionYaLeida_noModifica() {
        Notificacion notificacion = NotificacionesTestDataFactory.crearNotificacion(1L);
        notificacion.setLeida(true);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        notificacionService.marcarComoLeida(1L);

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarComoLeida_notificacionNoEncontrada_lanzaEntityNotFoundException() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> notificacionService.marcarComoLeida(99L));
    }

    @Test
    void eliminarNotificacion_notificacionExistente_eliminaCorrectamente() {
        Notificacion notificacion = NotificacionesTestDataFactory.crearNotificacion(1L);
        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        notificacionService.eliminarNotificacion(1L);

        verify(notificacionRepository).delete(notificacion);
    }

    @Test
    void eliminarNotificacion_notificacionNoEncontrada_lanzaEntityNotFoundException() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> notificacionService.eliminarNotificacion(99L));
    }

    @Test
    void marcarTodasComoLeidas_invocaRepository() {
        when(notificacionRepository.marcarTodasComoLeidas(eq(userId), any(LocalDateTime.class))).thenReturn(3);

        notificacionService.marcarTodasComoLeidas(userId);

        verify(notificacionRepository).marcarTodasComoLeidas(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void limpiarNotificacionesLeidasAntiguas_invocaRepository() {
        when(notificacionRepository.eliminarNotificacionesLeidasAntiguas(any(LocalDateTime.class))).thenReturn(5);

        notificacionService.limpiarNotificacionesLeidasAntiguas();

        verify(notificacionRepository).eliminarNotificacionesLeidasAntiguas(any(LocalDateTime.class));
    }

    @Test
    void limpiarNotificacionesNoLeidasAntiguas_invocaRepository() {
        when(notificacionRepository.eliminarNotificacionesNoLeidasAntiguas(any(LocalDateTime.class))).thenReturn(2);

        notificacionService.limpiarNotificacionesNoLeidasAntiguas();

        verify(notificacionRepository).eliminarNotificacionesNoLeidasAntiguas(any(LocalDateTime.class));
    }

    @Test
    void enviarNotificacionPrueba_conMensajePublicaEvento() {
        notificacionService.enviarNotificacionPrueba(userId, TipoNotificacion.SISTEMA, "Mensaje custom");

        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void enviarNotificacionPrueba_sinMensajePublicaEvento() {
        notificacionService.enviarNotificacionPrueba(userId, TipoNotificacion.SISTEMA, null);

        verify(eventPublisher).publishEvent(any());
    }
}
