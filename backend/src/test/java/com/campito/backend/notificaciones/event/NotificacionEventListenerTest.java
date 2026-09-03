package com.campito.backend.notificaciones.event;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.common.event.NotificacionEvent;
import com.campito.backend.common.event.TipoNotificacion;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.notificaciones.domain.dto.NotificacionDTOResponse;
import com.campito.backend.notificaciones.domain.entity.Notificacion;
import com.campito.backend.notificaciones.mapper.NotificacionMapper;
import com.campito.backend.notificaciones.repository.NotificacionRepository;
import com.campito.backend.notificaciones.service.SseEmitterService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class NotificacionEventListenerTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private NotificacionMapper notificacionMapper;

    @Mock
    private SseEmitterService sseEmitterService;

    private MeterRegistry meterRegistry;

    @InjectMocks
    private NotificacionEventListener notificacionEventListener;

    private UUID userId;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        userId = TestIds.USUARIO_ADMIN_ID;
    }

    @Test
    void handleNotificacionEvent_guardaNotificacionYEnviaSSE() {
        Notificacion notificacionGuardada = new Notificacion();
        notificacionGuardada.setId(1L);
        notificacionGuardada.setIdUsuario(userId);
        notificacionGuardada.setTipo(TipoNotificacion.SISTEMA);
        notificacionGuardada.setMensaje("Test");

        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionGuardada);

        NotificacionDTOResponse responseDto = new NotificacionDTOResponse(
            1L, TipoNotificacion.SISTEMA, "Test", false, null, null
        );
        when(notificacionMapper.toResponse(notificacionGuardada)).thenReturn(responseDto);

        NotificacionEvent event = new NotificacionEvent(
            this, userId, TipoNotificacion.SISTEMA, "Test"
        );

        notificacionEventListener.handleNotificacionEvent(event);

        verify(notificacionRepository).save(any(Notificacion.class));
        verify(sseEmitterService).enviarNotificacion(eq(userId), eq(responseDto));
    }

    @Test
    void handleNotificacionEvent_excepcionAlGuardar_noPropaga() {
        when(notificacionRepository.save(any(Notificacion.class)))
            .thenThrow(new RuntimeException("DB Error"));

        NotificacionEvent event = new NotificacionEvent(
            this, userId, TipoNotificacion.SISTEMA, "Test"
        );

        // No debería lanzar excepción
        notificacionEventListener.handleNotificacionEvent(event);
    }
}
