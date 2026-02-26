package com.campito.backend.service.agentAI;

import com.campito.backend.dto.AgenteChatRequestDTO;
import com.campito.backend.dto.AgenteChatResponseDTO;
import reactor.core.publisher.Flux;

/**
 * Servicio principal del Agente IA.
 * Gestiona la interacción con el LLM (Groq), function calling y auditoría.
 */
public interface AgenteIAService {
    
    /**
     * Procesa un mensaje del usuario y genera una respuesta usando el LLM.
     * El LLM puede llamar a funciones (tools) si necesita datos actualizados.
     * 
     * @param request Mensaje del usuario con contexto del workspace
     * @return Respuesta del agente con metadata (funciones llamadas, tokens)
     */
    AgenteChatResponseDTO chat(AgenteChatRequestDTO request);
    
    /**
     * Procesa un mensaje con streaming (SSE).
     * Emite tokens de la respuesta conforme se generan en tiempo real.
     * 
     * @param request Mensaje del usuario con contexto del workspace
     * @return Flujo reactivo de chunks de texto
     */
    Flux<String> chatStream(AgenteChatRequestDTO request);
}
