package com.campito.backend.notificaciones.scheduler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campito.backend.notificaciones.service.NotificacionService;

@ExtendWith(MockitoExtension.class)
class NotificacionSchedulerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionScheduler notificacionScheduler;

    @Test
    void limpiarNotificacionesLeidasAntiguas_invocaServicio() {
        notificacionScheduler.limpiarNotificacionesLeidasAntiguas();
        verify(notificacionService).limpiarNotificacionesLeidasAntiguas();
    }

    @Test
    void limpiarNotificacionesLeidasAntiguas_excepcion_noPropaga() {
        doThrow(new RuntimeException("Error")).when(notificacionService)
            .limpiarNotificacionesLeidasAntiguas();

        // No debería lanzar excepción
        notificacionScheduler.limpiarNotificacionesLeidasAntiguas();
    }

    @Test
    void limpiarNotificacionesNoLeidasAntiguas_invocaServicio() {
        notificacionScheduler.limpiarNotificacionesNoLeidasAntiguas();
        verify(notificacionService).limpiarNotificacionesNoLeidasAntiguas();
    }

    @Test
    void limpiarNotificacionesNoLeidasAntiguas_excepcion_noPropaga() {
        doThrow(new RuntimeException("Error")).when(notificacionService)
            .limpiarNotificacionesNoLeidasAntiguas();

        notificacionScheduler.limpiarNotificacionesNoLeidasAntiguas();
    }
}
