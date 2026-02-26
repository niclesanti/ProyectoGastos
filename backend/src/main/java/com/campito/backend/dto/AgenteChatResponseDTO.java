package com.campito.backend.dto;

import java.util.List;

/**
 * Respuesta del agente IA con metadata de la interacción.
 * 
 * @param response Respuesta generada por el agente
 * @param functionsCalled Lista de funciones (tools) que el agente llamó para responder
 * @param tokensUsed Cantidad de tokens consumidos en la interacción
 */
public record AgenteChatResponseDTO(
    String response,
    List<String> functionsCalled,
    Integer tokensUsed
) {}
