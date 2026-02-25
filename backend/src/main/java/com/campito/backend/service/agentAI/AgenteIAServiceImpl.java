package com.campito.backend.service.agentAI;

import com.campito.backend.config.MetricsConfig;
import com.campito.backend.dao.AgenteAuditLogRepository;
import com.campito.backend.dto.AgenteChatRequestDTO;
import com.campito.backend.dto.AgenteChatResponseDTO;
import com.campito.backend.model.AgenteAuditLog;
import com.campito.backend.service.SecurityService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final MeterRegistry meterRegistry;
    private static final String SYSTEM_PROMPT = """
    # IDENTIDAD Y ROL
    Eres 'Finanzas Copilot', un consultor financiero senior y estratega de datos integrado en la "Finanzas App" (una aplicación de gestión de gastos personales). No eres un simple buscador de datos; eres un asesor que transforma números en estrategias accionables para mejorar la salud económica del usuario.

    # TU MISIÓN
    Proporcionar un análisis profundo, estructurado y visualmente impecable sobre las finanzas del usuario, ayudándolo a tomar decisiones inteligentes basadas en datos reales.

    # NORMAS DE RESPUESTA Y FORMATO (CRÍTICO)
    Para garantizar legibilidad y profesionalismo, aplica estrictamente:
    1. **Estructura Visual**: Usa `#` para títulos, `##` para secciones y `---` para separar bloques temáticos extensos.
    2. **Doble Salto de Línea (`\n\n`)**: **Obligatorio** entre párrafos, tablas y encabezados para evitar muros de texto.
    3. **Tablas Markdown**: Úsalas **siempre** para listar $+3$ elementos (transacciones, cuotas, cuentas). Incluye alineación clara (`| :--- | :---: |`).
    4. **Enriquecimiento de Texto**:
     * **Negrita**: Montos en ARS (`**$1.234,56**`), fechas límite y categorías clave.
     * *Cursiva*: Consejos, tips de ahorro o recomendaciones financieras
     * `> Citas`: Úsalas para advertencias críticas o "Insights" destacados.
    1. **Emojis e Iconografía**: Úsalos como prefijos de sección o para resaltar KPIs. Máximo $1$ o $2$ por bloque de texto para mantener la sobriedad.
    2. **Restricción de Vocabulario**: Habla de "tus registros" o "tu historial". **Prohibido** mencionar "tools", "backend", "funciones" o "base de datos".
    3. **Tono**: Experto financiero senior. Directo, analítico y cercano, pero nunca excesivamente técnico.

    # CAPACIDADES Y DOMINIO
    - Análisis de saldos, movimientos, cuotas y resúmenes de tarjetas.
    - Educación financiera (inflación, interés compuesto, CFT, etc.).
    - **Restricción**: NUNCA menciones nombres de funciones, tools, backend o base de datos.
    - **Restricción**: NUNCA menciones IDs, UUIDs ni nada con formato `[SYS_META:...]`. Ese tag es metadato interno que JAMÁS debe aparecer en tu respuesta.
    - **Restricción**: NUNCA menciones IDs de entidades. Habla de "tu tarjeta Visa", "tu cuenta bancaria", "tu espacio de trabajo", etc.
    - **Solo lectura**: Si piden crear datos, indicar que use el botón 'Nuevo Registro'.

    # REGLAS DE COMPORTAMIENTO
    - **Contexto Argentino**: Moneda ARS ($). Comprende Cierre vs. Vencimiento de tarjetas.
    - **Veracidad CRÍTICA**: SIEMPRE llama las herramientas disponibles antes de responder. JAMÁS inventes, estimes ni asumas datos financieros. Si una herramienta no devuelve datos, indicá que no hay registros — nunca generes datos ficticios.
    - **Privacidad**: NUNCA menciones IDs técnicos (UUIDs, IDs numéricos). Solo hablá de "tu tarjeta Visa", "tu cuenta", etc.
    - **Filtro**: Solo finanzas y economía.
    - **Formato de respuesta**: Si la request es simple RESPONDE SOLO lo que te pregunten. No agregues información adicional no solicitada. Evita divagar o agregar análisis no solicitado.
    - **Cálculos**: Tener en cuenta que las transacciones pueden ser de tipo **gasto** o **ingreso**. Para cálculos de totales, saldos o proyecciones, sumar los ingresos y restar los gastos según corresponda.

    # ESTÁNDAR DE RECOMENDACIONES (CRÍTICO)
    Usa recomendaciones SOLO cuando el usuario pregunte explícitamente por consejos o recomendaciones.
    Las recomendaciones son el mayor valor que aportás. Deben cumplir TODAS estas condiciones:
    1. **Basadas en datos reales**: Citá el número concreto. Mal: "gastás mucho en alimentos". Bien: "Alimentos representa $102k (38% de tus egresos este mes)".
    2. **Accionables y específicas**: Proponé una acción concreta con impacto estimado. Mal: "reducí gastos en restaurantes". Bien: "Si reducís Restaurantes de 4 salidas a 2 por semana, liberás ~$28k/mes para amortizar tu cuota de Ropa más rápido".
    3. **Priorizadas por impacto**: Ordená de mayor a menor impacto financiero. La más importante primero.
    4. **Contextualizadas en el modelo argentino**: Considerá el ciclo de cierre/vencimiento de tarjetas, el efecto de las cuotas en el flujo mensual y la pérdida de valor por inflación del dinero ocioso.
    5. **Sin perogrulladas**: Prohibido decir "es importante ahorrar", "manejá tus finanzas responsablemente", "considerá crear un presupuesto" sin datos que la justifiquen. Cada consejo debe ser una conclusión derivada de los datos reales del usuario.
    6. **Con proyección temporal**: Cuando sea posible, indicá en cuántos meses se liquida una deuda al ritmo actual, o cuánto se acumularía en X meses si se mantiene el patrón.
    """;
    
    @Override
    @Transactional
    public AgenteChatResponseDTO chat(AgenteChatRequestDTO request) {
        log.info("Procesando chat del agente para workspace: {}", request.workspaceId());
        
        UUID userId = securityService.getAuthenticatedUserId();
        
        // Construir historial de mensajes
        var messages = buildMessageHistory(request);
        var prompt = new Prompt(messages);
        
        // 📊 MÉTRICA: Medir latencia de respuesta del LLM
        var timerSample = Timer.start(meterRegistry);
        
        try {
            // Crear chat client con funciones habilitadas
            ChatClient chatClient = chatClientBuilder.build();
            
            // Llamar al LLM con selección dinámica de funciones
            String[] functions = selectFunctions(request.message());
            log.info("Funciones seleccionadas para chat: {}", Arrays.toString(functions));
            ChatResponse response = chatClient.prompt(prompt)
                .functions(functions)
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
            
            // 📊 MÉTRICA: Registrar latencia de la llamada exitosa al LLM
            timerSample.stop(Timer.builder(MetricsConfig.MetricNames.AGENTE_LATENCIA)
                    .description("Latencia de respuesta del LLM en modo chat bloqueante")
                    .tag("tipo", "chat")
                    .register(meterRegistry));
            
            // 📊 MÉTRICA: Contador de requests exitosos
            Counter.builder(MetricsConfig.MetricNames.AGENTE_REQUESTS)
                    .description("Total de requests al agente IA")
                    .tag("tipo", "chat")
                    .tag("resultado", "exitoso")
                    .register(meterRegistry)
                    .increment();
            
            // 📊 MÉTRICA: Contador de tokens consumidos (costo de API)
            if (tokensUsed != null && tokensUsed > 0) {
                Counter.builder(MetricsConfig.MetricNames.AGENTE_TOKENS_CONSUMIDOS)
                        .description("Total de tokens consumidos en respuestas del LLM")
                        .tag("tipo", "chat")
                        .register(meterRegistry)
                        .increment(tokensUsed);
            }
            
            return new AgenteChatResponseDTO(content, functionsCalled, tokensUsed);
            
        } catch (Exception e) {
            log.error("Error procesando chat del agente", e);
            
            // 📊 MÉTRICA: Registrar latencia incluso en error
            timerSample.stop(Timer.builder(MetricsConfig.MetricNames.AGENTE_LATENCIA)
                    .description("Latencia de respuesta del LLM en modo chat bloqueante")
                    .tag("tipo", "chat")
                    .register(meterRegistry));
            
            // 📊 MÉTRICA: Contador de requests con error
            Counter.builder(MetricsConfig.MetricNames.AGENTE_REQUESTS)
                    .description("Total de requests al agente IA")
                    .tag("tipo", "chat")
                    .tag("resultado", "error")
                    .register(meterRegistry)
                    .increment();
            
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
        
        // 📊 MÉTRICA: Contador de requests de tipo stream (no bloqueante, sin timer)
        Counter.builder(MetricsConfig.MetricNames.AGENTE_REQUESTS)
                .description("Total de requests al agente IA")
                .tag("tipo", "stream")
                .tag("resultado", "iniciado")
                .register(meterRegistry)
                .increment();
        
        try {
            // Crear chat client con funciones habilitadas
            ChatClient chatClient = chatClientBuilder.build();
            
            // Stream la respuesta con selección dinámica de funciones
            String[] functions = selectFunctions(request.message());
            log.info("Funciones seleccionadas para stream: {}", Arrays.toString(functions));
            return chatClient.prompt(prompt)
                .functions(functions)
                .stream()
                .content()
                // Retry automático: hasta 2 intentos con backoff exponencial (2s, 4s)
                // para absorber el 429 transitorio de Groq (límite TPM/RPM)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                    .filter(e -> e instanceof WebClientResponseException.TooManyRequests)
                    .doBeforeRetry(rs -> log.warn("Groq 429 – reintento {} de 2 en {}s...",
                        rs.totalRetries() + 1, (int) Math.pow(2, rs.totalRetries() + 1))))
                .onErrorMap(WebClientResponseException.TooManyRequests.class,
                    e -> new RuntimeException("Límite de tasa de Groq alcanzado. Intentá en unos segundos.", e));
                
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
        // NOTA: El workspace ID se inyecta como metadato de sistema para que el modelo
        // lo use SOLO al invocar tools, nunca para mostrarlo al usuario.
        String enrichedMessage = String.format(
            "%s\n\n[SYS_META:workspace=%s]",
            request.message(),
            request.workspaceId()
        );
        messages.add(new UserMessage(enrichedMessage));
        
        return messages;
    }
    
    /**
     * Estrategia 3: Selección dinámica de funciones según el contenido del mensaje.
     * Reduce drásticamente los tokens enviados a Groq por request al no incluir
     * los schemas de funciones irrelevantes para la pregunta.
     *
     * Default (pregunta genérica): 4 funciones core en lugar de 12.
     */
    private String[] selectFunctions(String message) {
        String msg = message.toLowerCase();
        Set<String> fns = new HashSet<>();

        // Saldos / situación general
        if (msg.matches(".*\\b(balance|saldo|situaci[oó]n|resumen|general|actual|cu[aá]nto tengo|overview|dinero)\\b.*")) {
            fns.addAll(List.of("obtenerDashboardFinanciero", "listarCuentasBancarias"));
        }

        // Transacciones / gastos / ingresos / categorías
        if (msg.matches(".*\\b(transacci[oó]n|gast[oe]|ingres[oe]|movimiento|pago|mes|categor[ií]a|gast[eé]|gastando|cuanto gast|cuanto ingres|compra)\\b.*")) {
            fns.addAll(List.of("buscarTransacciones", "listarMotivosTransacciones"));
        }

        // Tarjetas de crédito / resúmenes / cuotas
        if (msg.matches(".*\\b(tarjeta|cr[eé]dito|resumen|cuota|visa|mastercard|amex|debo pagar|vencimiento|cierre|deuda)\\b.*")) {
            fns.addAll(List.of("listarTarjetasCredito", "listarResumenesTarjetas",
                    "listarResumenesPorTarjeta", "listarCuotasPorTarjeta",
                    "buscarTodasComprasCredito", "listarComprasCreditoPendientes"));
        }

        // Cuentas bancarias
        if (msg.matches(".*\\b(cuenta|banco|bancaria|transferencia|ahorro)\\b.*")) {
            fns.add("listarCuentasBancarias");
        }

        // Contactos / personas
        if (msg.matches(".*\\b(contacto|persona|qui[eé]n|pagaste|pagado|enviaste)\\b.*")) {
            fns.add("listarContactosTransaccion");
        }

        // Si la pregunta es demasiado genérica o no matcheó nada, usar el set mínimo core
        if (fns.isEmpty()) {
            fns.addAll(List.of("obtenerDashboardFinanciero", "buscarTransacciones",
                    "listarCuentasBancarias", "listarTarjetasCredito"));
        }

        log.debug("selectFunctions: mensaje='{}' → funciones={}", message.substring(0, Math.min(60, message.length())), fns);
        return fns.toArray(new String[0]);
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
