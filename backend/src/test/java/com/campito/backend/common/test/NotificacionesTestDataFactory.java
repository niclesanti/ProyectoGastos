package com.campito.backend.common.test;

import java.time.LocalDateTime;
import java.util.UUID;

import com.campito.backend.common.event.TipoNotificacion;
import com.campito.backend.notificaciones.domain.entity.Notificacion;
import com.campito.backend.notificaciones.domain.dto.NotificacionDTOResponse;

public final class NotificacionesTestDataFactory {

    private NotificacionesTestDataFactory() {}

    public static Notificacion crearNotificacion(Long id) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setIdUsuario(TestIds.USUARIO_ADMIN_ID);
        n.setTipo(TipoNotificacion.SISTEMA);
        n.setMensaje("Notificación de prueba");
        n.setLeida(false);
        n.setFechaCreacion(LocalDateTime.now());
        return n;
    }

    public static NotificacionDTOResponse crearNotificacionResponse(Long id) {
        return new NotificacionDTOResponse(
            id,
            TipoNotificacion.SISTEMA,
            "Notificación de prueba",
            false,
            LocalDateTime.now(),
            null
        );
    }
}
