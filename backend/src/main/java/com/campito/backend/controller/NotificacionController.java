package com.campito.backend.controller;

import com.campito.backend.dto.NotificacionDTOResponse;
import com.campito.backend.model.TipoNotificacion;
import com.campito.backend.service.NotificacionService;
import com.campito.backend.service.SecurityService;
import com.campito.backend.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para la gestión de notificaciones.
 * 
 * Proporciona endpoints para consultar, marcar como leídas y eliminar
 * notificaciones del usuario autenticado.
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "API para gestión de notificaciones del usuario")
public class NotificacionController {
    
    private final NotificacionService notificacionService;
    private final SseEmitterService sseEmitterService;
    private final SecurityService securityService;
    
    /**
     * Obtiene todas las notificaciones del usuario autenticado.
     * 
     * @return Lista de notificaciones (máximo 50 más recientes)
     */
    @Operation(
        summary = "Obtener notificaciones",
        description = "Obtiene las últimas 50 notificaciones del usuario autenticado ordenadas por fecha de creación descendente."
    )
    @ApiResponse(responseCode = "200", description = "Notificaciones obtenidas correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping
    public ResponseEntity<List<NotificacionDTOResponse>> obtenerNotificaciones() {
        UUID idUsuario = securityService.getAuthenticatedUserId();
        
        List<NotificacionDTOResponse> notificaciones = 
                notificacionService.obtenerNotificacionesUsuario(idUsuario);
        return ResponseEntity.ok(notificaciones);
    }
    
    /**
     * Cuenta las notificaciones no leídas del usuario autenticado.
     * 
     * @return Objeto con la cantidad de notificaciones no leídas
     */
    @Operation(
        summary = "Contar notificaciones no leídas",
        description = "Obtiene la cantidad de notificaciones no leídas del usuario autenticado. Útil para mostrar badge en la UI."
    )
    @ApiResponse(responseCode = "200", description = "Contador obtenido correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        UUID idUsuario = securityService.getAuthenticatedUserId();

        Long count = notificacionService.contarNoLeidas(idUsuario);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * Marca una notificación como leída.
     * 
     * @param id ID de la notificación
     * @return Respuesta vacía con código 200
     */
    @Operation(
        summary = "Marcar notificación como leída",
        description = "Marca una notificación específica como leída y registra la fecha de lectura. Solo el propietario puede marcar su notificación."
    )
    @ApiResponse(responseCode = "200", description = "Notificación marcada como leída correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "403", description = "No tienes permiso para acceder a esta notificación")
    @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        // Validar que la notificación pertenece al usuario autenticado
        securityService.validateNotificacionOwnership(id);
        securityService.getAuthenticatedUserId();

        notificacionService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Marca todas las notificaciones del usuario como leídas.
     * 
     * @return Respuesta vacía con código 200
     */
    @Operation(
        summary = "Marcar todas las notificaciones como leídas",
        description = "Marca todas las notificaciones no leídas del usuario autenticado como leídas en una sola operación."
    )
    @ApiResponse(responseCode = "200", description = "Todas las notificaciones marcadas como leídas correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @PutMapping("/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        UUID idUsuario = securityService.getAuthenticatedUserId();

        notificacionService.marcarTodasComoLeidas(idUsuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Elimina una notificación.
     * 
     * @param id ID de la notificación
     * @return Respuesta vacía con código 200
     */
    @Operation(
        summary = "Eliminar notificación",
        description = "Elimina permanentemente una notificación específica. Solo el propietario puede eliminar su notificación."
    )
    @ApiResponse(responseCode = "200", description = "Notificación eliminada correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "403", description = "No tienes permiso para eliminar esta notificación")
    @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long id) {
        // Validar que la notificación pertenece al usuario autenticado
        securityService.validateNotificacionOwnership(id);
        securityService.getAuthenticatedUserId();

        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Endpoint SSE para recibir notificaciones en tiempo real.
     * 
     * @return Emitter SSE configurado
     */
    @Operation(
        summary = "Stream de notificaciones en tiempo real (SSE)",
        description = "Establece una conexión Server-Sent Events para recibir notificaciones en tiempo real. La conexión se mantiene abierta por 1 hora con reconexión automática."
    )
    @ApiResponse(responseCode = "200", description = "Conexión SSE establecida correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error al establecer la conexión SSE")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotificaciones() {
        UUID idUsuario = securityService.getAuthenticatedUserId();

        return sseEmitterService.crearEmitter(idUsuario);
    }
    
    /**
     * Envía una notificación de prueba al usuario autenticado.
     * Útil para testing del sistema de notificaciones en tiempo real via SSE.
     * 
     * @param tipo Tipo de notificación (opcional, por defecto SISTEMA)
     * @param mensaje Mensaje personalizado (opcional)
     * @return Respuesta de confirmación
     */
    @Operation(
        summary = "Enviar notificación de prueba",
        description = "Publica un evento de notificación para el usuario autenticado. Útil para probar el SSE en tiempo real durante el desarrollo."
    )
    @ApiResponse(responseCode = "200", description = "Notificación de prueba enviada correctamente")
    @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    @ApiResponse(responseCode = "500", description = "Error al enviar la notificación de prueba")
    @PostMapping("/test/enviar")
    public ResponseEntity<Map<String, Object>> enviarNotificacionPrueba(
            @RequestParam(required = false, defaultValue = "SISTEMA") TipoNotificacion tipo,
            @RequestParam(required = false) String mensaje
    ) {
        UUID idUsuario = securityService.getAuthenticatedUserId();
        
        notificacionService.enviarNotificacionPrueba(idUsuario, tipo, mensaje);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Notificación de prueba publicada exitosamente",
            "tipo", tipo,
            "timestamp", LocalDateTime.now()
        ));
    }
}
