package com.campito.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Request para interactuar con el agente IA.
 * 
 * @param message Mensaje del usuario al agente
 * @param workspaceId ID del espacio de trabajo (contexto financiero)
 * @param conversationHistory Historial de la conversación (opcional, para contexto)
 */
public record AgenteChatRequestDTO(
    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 400, message = "El mensaje no puede superar los 400 caracteres")
    String message,
    
    @NotNull(message = "El workspace ID es obligatorio")
    UUID workspaceId,
    
    List<ChatMessageDTO> conversationHistory
) {}
