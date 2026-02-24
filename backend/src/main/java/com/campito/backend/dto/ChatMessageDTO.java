package com.campito.backend.dto;

/**
 * Representa un mensaje en el historial de conversación.
 * 
 * @param role Rol del emisor: "user" (usuario) o "assistant" (agente IA)
 * @param content Contenido del mensaje
 */
public record ChatMessageDTO(
    String role,
    String content
) {}
