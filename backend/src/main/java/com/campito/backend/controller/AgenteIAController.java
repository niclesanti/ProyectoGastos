package com.campito.backend.controller;

import com.campito.backend.dto.AgenteChatRequestDTO;
import com.campito.backend.dto.AgenteChatResponseDTO;
import com.campito.backend.exception.RateLimitExceededException;
import com.campito.backend.service.SecurityService;
import com.campito.backend.service.agentAI.AgenteIAService;
import com.campito.backend.service.agentAI.RateLimitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Controller REST para el Agente IA.
 * Expone endpoints para interactuar con el asistente financiero inteligente.
 * Solo se activa si agente.ia.enabled=true
 */
@RestController
@RequestMapping("/api/agente")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Agente IA", description = "Endpoints para interactuar con el asistente de IA financiero")
@SecurityRequirement(name = "bearer-jwt")
@ConditionalOnProperty(name = "agente.ia.enabled", havingValue = "true", matchIfMissing = false)
public class AgenteIAController {
    
    private final AgenteIAService agenteIAService;
    private final RateLimitService rateLimitService;
    private final SecurityService securityService;
    
    /**
     * Envía un mensaje al agente IA y recibe respuesta completa.
     * El agente puede llamar a funciones (tools) para obtener datos actualizados.
     * 
     * Rate limit: 60 mensajes por minuto con burst capacity de 10.
     * 
     * @param request Mensaje del usuario con contexto del workspace
     * @return Respuesta del agente con metadata
     */
    @PostMapping("/chat")
    @Operation(
        summary = "Enviar mensaje al agente IA", 
        description = "Procesa un mensaje del usuario y genera respuesta usando LLM con function calling. " +
                     "El agente puede consultar saldos, transacciones, tarjetas y más."
    )
    public ResponseEntity<AgenteChatResponseDTO> chat(
        @Valid
        @NotNull(message = "El request no puede ser nulo")
        @RequestBody AgenteChatRequestDTO request) {
        
        UUID userId = securityService.getAuthenticatedUserId();

        // Validar acceso al workspace
        securityService.validateWorkspaceAccess(request.workspaceId());
        
        log.info("Usuario {} enviando mensaje al agente en workspace {}", userId, request.workspaceId());
        
        // Validar rate limit
        if (!rateLimitService.allowRequest(userId)) {
            throw new RateLimitExceededException(
                "Has excedido el límite de mensajes al asistente. Por favor, espera un momento antes de continuar."
            );
        }
        
        // Procesar mensaje
        AgenteChatResponseDTO response = agenteIAService.chat(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Envía un mensaje al agente y recibe respuesta en streaming (SSE).
     * Los tokens de la respuesta llegan en tiempo real conforme se generan.
     * 
     * @param message Mensaje del usuario
     * @param workspaceId ID del espacio de trabajo
     * @return Flujo de texto (Server-Sent Events)
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Chat con streaming (SSE)", 
        description = "Recibe la respuesta del agente token por token en tiempo real. " +
                     "Útil para mejorar la percepción de velocidad en la UI."
    )
    public Flux<String> chatStream(
        @NotBlank(message = "El mensaje no puede estar vacío")
        @RequestParam String message,
        @NotNull(message = "El workspace ID es obligatorio")
        @RequestParam UUID workspaceId
    ) {

        UUID userId = securityService.getAuthenticatedUserId();

        // Validar acceso al workspace
        securityService.validateWorkspaceAccess(workspaceId);
        
        log.info("Usuario {} iniciando chat stream en workspace {}", userId, workspaceId);
        
        // Validar rate limit
        if (!rateLimitService.allowRequest(userId)) {
            return Flux.error(new RateLimitExceededException(
                "Has excedido el límite de mensajes al asistente."
            ));
        }
        
        // Crear request y procesar con streaming
        AgenteChatRequestDTO request = new AgenteChatRequestDTO(message, workspaceId, null);
        
        return agenteIAService.chatStream(request);
    }
    
    /**
     * Consulta el estado del rate limit del usuario actual.
     * Retorna cuántos mensajes le quedan disponibles.
     * 
     * @return Tokens restantes en la ventana actual
     */
    @GetMapping("/rate-limit/status")
    @Operation(
        summary = "Consultar límite de rate remaining", 
        description = "Retorna cuántos mensajes le quedan al usuario en la ventana temporal actual. " +
                     "Útil para mostrar feedback en la UI."
    )
    public ResponseEntity<RateLimitStatusDTO> getRateLimitStatus() {
        UUID userId = securityService.getAuthenticatedUserId();
        long tokensRemaining = rateLimitService.getAvailableTokens(userId);
        
        return ResponseEntity.ok(new RateLimitStatusDTO(tokensRemaining));
    }
    
    /**
     * DTO para respuesta de estado de rate limit
     */
    public record RateLimitStatusDTO(
        long tokensRemaining
    ) {}
}
