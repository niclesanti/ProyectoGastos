package com.campito.backend.notificaciones.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.campito.backend.common.test.TestIds;
import com.campito.backend.common.test.NotificacionesTestDataFactory;
import com.campito.backend.notificaciones.domain.dto.NotificacionDTOResponse;

@ExtendWith(MockitoExtension.class)
class SseEmitterServiceImplTest {

    @Spy
    private AtomicInteger sseConexionesActivasGauge = new AtomicInteger(0);

    @InjectMocks
    private SseEmitterServiceImpl sseEmitterService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = TestIds.USUARIO_ADMIN_ID;
    }

    @Test
    void crearEmitter_retornaEmitterNoNulo() {
        SseEmitter emitter = sseEmitterService.crearEmitter(userId);
        assertNotNull(emitter);
    }

    @Test
    void crearEmitter_agregaAlMapa() {
        sseEmitterService.crearEmitter(userId);
        assertEquals(1, sseEmitterService.getActiveConnections());
    }

    @Test
    void crearEmitter_multipleEmitters_distintosUsuarios() {
        UUID user2 = UUID.randomUUID();
        sseEmitterService.crearEmitter(userId);
        sseEmitterService.crearEmitter(user2);
        assertEquals(2, sseEmitterService.getActiveConnections());
    }

    @Test
    void enviarNotificacion_usuarioConectado_envia() throws Exception {
        SseEmitter emitter = sseEmitterService.crearEmitter(userId);
        SseEmitter mockEmitter = mock(SseEmitter.class);

        // Usar el real emitter que fue creado
        NotificacionDTOResponse notif = NotificacionesTestDataFactory.crearNotificacionResponse(1L);

        // No debería lanzar excepción
        assertDoesNotThrow(() -> sseEmitterService.enviarNotificacion(userId, notif));
    }

    @Test
    void enviarNotificacion_usuarioNoConectado_noLanzaExcepcion() {
        NotificacionDTOResponse notif = NotificacionesTestDataFactory.crearNotificacionResponse(1L);
        UUID usuarioNoConectado = UUID.randomUUID();

        assertDoesNotThrow(() -> sseEmitterService.enviarNotificacion(usuarioNoConectado, notif));
    }

    @Test
    void getActiveConnections_sinConexiones_retornaCero() {
        assertEquals(0, sseEmitterService.getActiveConnections());
    }

    @Test
    void enviarNotificacion_emitterLanzaIOException_remueveDelMapa() throws Exception {
        // Create emitter for user
        sseEmitterService.crearEmitter(userId);
        assertEquals(1, sseEmitterService.getActiveConnections());

        // Now create a mock SseEmitter that throws IOException on send
        SseEmitter mockEmitter = mock(SseEmitter.class);
        doThrow(new IOException("Connection closed"))
            .when(mockEmitter).send(any(SseEmitter.SseEventBuilder.class));

        // Inject mock emitter into the map via reflection
        java.lang.reflect.Field emittersField = SseEmitterServiceImpl.class.getDeclaredField("emitters");
        emittersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<UUID, SseEmitter> emittersMap = (Map<UUID, SseEmitter>) emittersField.get(sseEmitterService);
        emittersMap.put(userId, mockEmitter);

        NotificacionDTOResponse notif = NotificacionesTestDataFactory.crearNotificacionResponse(1L);
        assertDoesNotThrow(() -> sseEmitterService.enviarNotificacion(userId, notif));
        assertEquals(0, sseEmitterService.getActiveConnections());
    }
}
