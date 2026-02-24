package com.campito.backend.service.agentAI;

import com.campito.backend.dao.AgenteAuditLogRepository;
import com.campito.backend.dto.AgenteChatRequestDTO;
import com.campito.backend.dto.AgenteChatResponseDTO;
import com.campito.backend.model.AgenteAuditLog;
import com.campito.backend.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio del Agente IA.
 * Orquesta la comunicación con el modelo LLM (Groq - Llama 3.3 70B Versatile), function calling y auditoría.
 * Solo se activa si agente.ia.enabled=true
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "agente.ia.enabled", havingValue = "true", matchIfMissing = false)
public class AgenteIAServiceImpl implements AgenteIAService {
    
    private final ChatClient.Builder chatClientBuilder;
    private final SecurityService securityService;
    private final AgenteAuditLogRepository auditLogRepository;
    
    private static final String SYSTEM_PROMPT = """
        Eres 'FinanceAgent AI', un asistente financiero experto integrado en la aplicación "Finanzas App" (Aplicación de Gestión de Gastos Personales).
        
        **TU MISIÓN**: Ayudar a los usuarios a gestionar y analizar sus finanzas personales y familiares.
        
        **CAPACIDADES**:
        - Consultar saldos, transacciones, compras con crédito, cuotas, resúmenes, contactos, motivos, cuentas bancarias y tarjetas de crédito
        - Analizar patrones de gastos e ingresos
        - Generar reportes financieros mensuales
        - Responder preguntas sobre el estado financiero del espacio de trabajo
        
        **RESTRICCIONES CRÍTICAS**:
        1. SOLO respondes sobre datos financieros del usuario actual y su espacio de trabajo
        2. NO puedes hacer operaciones de escritura (crear transacciones, compras con crédito, cuotas, resúmenes, contactos, motivos, etc.) - solo lectura
        3. Si te preguntan algo fuera del dominio financiero, responde amablemente que no estás capacitado
        4. NUNCA inventes datos. Si no tienes información, dilo claramente y ofrece al usuario si necesita otra cosa
        5. Usa las funciones (tools) SIEMPRE que necesites datos actualizados
        
        **ESTILO DE COMUNICACIÓN**:
        - Formal pero cercano (tutea al usuario si el contexto es familiar)
        - Respuestas concisas pero completas
        - USA EMOJIS para mejorar la lectura: 💰 (dinero), 📊 (análisis), 💳 (tarjetas), 🏦 (cuentas), ⚠️ (alertas)
        - Si hay deuda alta o gastos excesivos, menciona preocupación profesional sin alarmar
        - Formatea montos con separadores de miles: $1,234.56
        
        **CONTEXTO ARGENTINO**:
        - Los usuarios son argentinos, usa formato de moneda ARS cuando sea relevante
        - Las tarjetas de crédito siguen el modelo argentino (día de cierre y vencimiento)
        
        **EJEMPLO DE RESPUESTA BUENA**:
        "📊 Tu balance total es de $45,320.50. Este mes gastaste $12,400 (principalmente en Supermercado: $4,500). 
        
        💳 Tienes una deuda pendiente de $8,200 en tu Visa que vence el 15 de marzo. Te recomiendo priorizar ese pago."
        
        **IMPORTANTE**: Antes de responder, evalúa si necesitas llamar a alguna función para obtener datos actualizados.
        """;
    
    @Override
    @Transactional
    public AgenteChatResponseDTO chat(AgenteChatRequestDTO request) {
        log.info("Procesando chat del agente para workspace: {}", request.workspaceId());
        
        UUID userId = securityService.getAuthenticatedUserId();
        
        // Construir historial de mensajes
        var messages = buildMessageHistory(request);
        var prompt = new Prompt(messages);
        
        try {
            // Crear chat client con funciones habilitadas
            ChatClient chatClient = chatClientBuilder.build();
            
            // Llamar al LLM con function calling
            ChatResponse response = chatClient.prompt(prompt)
                .functions("obtenerDashboardFinanciero", "buscarTransacciones", 
                          "listarTarjetasCredito", "listarResumenesTarjetas", 
                          "listarCuentasBancarias", "listarMotivosTransacciones")
                .call()
                .chatResponse();
            
            String content = response.getResult().getOutput().getContent();
            List<String> functionsCalled = extractFunctionsCalled(response);
            Integer tokensUsed = extractTokensUsed(response);
            
            // Auditar la interacción exitosa
            auditLogRepository.save(AgenteAuditLog.builder()
                .userId(userId)
                .workspaceId(request.workspaceId())
                .userMessage(request.message())
                .agentResponse(content)
                .functionsCalled(String.join(", ", functionsCalled))
                .timestamp(LocalDateTime.now())
                .tokensUsed(tokensUsed)
                .success(true)
                .build());
            
            log.info("Chat completado. Tokens: {}, Funciones: {}", tokensUsed, functionsCalled);
            
            return new AgenteChatResponseDTO(content, functionsCalled, tokensUsed);
            
        } catch (Exception e) {
            log.error("Error procesando chat del agente", e);
            
            // Auditar el error
            auditLogRepository.save(AgenteAuditLog.builder()
                .userId(userId)
                .workspaceId(request.workspaceId())
                .userMessage(request.message())
                .timestamp(LocalDateTime.now())
                .success(false)
                .errorMessage(e.getMessage())
                .build());
            
            throw new RuntimeException("Error procesando mensaje del agente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Flux<String> chatStream(AgenteChatRequestDTO request) {
        log.info("Iniciando stream de chat para workspace: {}", request.workspaceId());
        
        // Construir historial de mensajes
        var messages = buildMessageHistory(request);
        var prompt = new Prompt(messages);
        
        try {
            // Crear chat client con funciones habilitadas
            ChatClient chatClient = chatClientBuilder.build();
            
            // Stream la respuesta
            return chatClient.prompt(prompt)
                .functions("obtenerDashboardFinanciero", "buscarTransacciones", 
                          "listarTarjetasCredito", "listarResumenesTarjetas", 
                          "listarCuentasBancarias", "listarMotivosTransacciones")
                .stream()
                .content();
                
        } catch (Exception e) {
            log.error("Error en streaming del agente", e);
            return Flux.error(new RuntimeException("Error en streaming: " + e.getMessage(), e));
        }
    }
    
    /**
     * Construye el historial de mensajes para el prompt.
     * Incluye: System Prompt + Historial de conversación + Mensaje actual con contexto.
     */
    private List<Message> buildMessageHistory(AgenteChatRequestDTO request) {
        var messages = new ArrayList<Message>();
        
        // System prompt con instrucciones del agente
        messages.add(new SystemMessage(SYSTEM_PROMPT));
        
        // Agregar historial de conversación si existe
        if (request.conversationHistory() != null && !request.conversationHistory().isEmpty()) {
            request.conversationHistory().forEach(msg -> {
                if ("user".equals(msg.role())) {
                    messages.add(new UserMessage(msg.content()));
                } else if ("assistant".equals(msg.role())) {
                    messages.add(new AssistantMessage(msg.content()));
                }
            });
        }
        
        // Agregar mensaje actual con contexto del workspace
        String enrichedMessage = String.format(
            "%s\n\n[CONTEXTO: Workspace ID = %s]",
            request.message(),
            request.workspaceId()
        );
        messages.add(new UserMessage(enrichedMessage));
        
        return messages;
    }
    
    /**
     * Extrae nombres de funciones llamadas del metadata de la respuesta.
     */
    private List<String> extractFunctionsCalled(ChatResponse response) {
        try {
            // TODO: Implementar extracción real del metadata de Groq cuando tengamos la estructura definida
            // Por ahora retornamos lista vacía hasta ver la estructura real
            return List.of();
        } catch (Exception e) {
            log.warn("No se pudieron extraer funciones llamadas", e);
            return List.of();
        }
    }
    
    /**
     * Extrae el número de tokens usados del metadata de la respuesta.
     */
    private Integer extractTokensUsed(ChatResponse response) {
        try {
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                return response.getMetadata().getUsage().getTotalTokens().intValue();
            }
            return 0;
        } catch (Exception e) {
            log.warn("No se pudo extraer tokens usados", e);
            return 0;
        }
    }
}
