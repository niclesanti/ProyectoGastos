package com.campito.backend.notificaciones.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.campito.backend.common.test.BaseWebMvcTest;
import com.campito.backend.common.test.NotificacionesTestDataFactory;
import com.campito.backend.common.test.TestIds;
import com.campito.backend.notificaciones.service.NotificacionService;
import com.campito.backend.notificaciones.service.SseEmitterService;
import com.campito.backend.security.SecurityService;

@WebMvcTest(NotificacionController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificacionControllerTest extends BaseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificacionService notificacionService;

    @MockBean
    private SseEmitterService sseEmitterService;

    @MockBean
    private SecurityService securityService;

    @Test
    void obtenerNotificaciones_retorna200() throws Exception {
        when(securityService.getAuthenticatedUserId()).thenReturn(TestIds.USUARIO_ADMIN_ID);
        when(notificacionService.obtenerNotificacionesUsuario(TestIds.USUARIO_ADMIN_ID))
            .thenReturn(List.of(NotificacionesTestDataFactory.crearNotificacionResponse(1L)));

        mockMvc.perform(get("/api/notificaciones"))
            .andExpect(status().isOk());
    }

    @Test
    void contarNoLeidas_retorna200() throws Exception {
        when(securityService.getAuthenticatedUserId()).thenReturn(TestIds.USUARIO_ADMIN_ID);
        when(notificacionService.contarNoLeidas(TestIds.USUARIO_ADMIN_ID)).thenReturn(3L);

        mockMvc.perform(get("/api/notificaciones/no-leidas/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void marcarComoLeida_retorna200() throws Exception {
        mockMvc.perform(put("/api/notificaciones/{id}/leer", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void marcarTodasComoLeidas_retorna200() throws Exception {
        when(securityService.getAuthenticatedUserId()).thenReturn(TestIds.USUARIO_ADMIN_ID);

        mockMvc.perform(put("/api/notificaciones/marcar-todas-leidas"))
            .andExpect(status().isOk());
    }

    @Test
    void eliminarNotificacion_retorna204() throws Exception {
        mockMvc.perform(delete("/api/notificaciones/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void streamNotificaciones_retornaSseEmitter() throws Exception {
        when(securityService.getAuthenticatedUserId()).thenReturn(TestIds.USUARIO_ADMIN_ID);
        when(sseEmitterService.crearEmitter(TestIds.USUARIO_ADMIN_ID))
            .thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/api/notificaciones/stream"))
            .andExpect(status().isOk());
    }
}
