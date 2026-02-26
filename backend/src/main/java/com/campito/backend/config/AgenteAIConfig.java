package com.campito.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import com.campito.backend.service.agentAI.AgenteToolsService;

import java.util.Map;
import java.util.function.Function;

/**
 * Configuración de Spring AI para el Agente IA.
 * Registra funciones (tools) que el LLM puede llamar vía Function Calling.
 * Solo se activa si se configura agente.ia.enabled=true
 */
@Configuration
@ConditionalOnProperty(name = "agente.ia.enabled", havingValue = "true", matchIfMissing = false)
public class AgenteAIConfig {
    
    /**
     * Record para parámetros de búsqueda de transacciones
     */
    public record BuscarTransaccionesRequest(
        String workspaceId,
        Integer mes,
        Integer anio,
        String motivo,
        String contacto
    ) {}
    
    /**
     * Record para parámetros que solo requieren workspaceId
     */
    public record WorkspaceRequest(String workspaceId) {}

    /**
     * Record para parámetros que requieren el ID de una tarjeta de crédito
     */
    public record TarjetaIdRequest(Long tarjetaId) {}
    
    @Bean
    @Description("Obtiene el estado financiero completo de un espacio de trabajo: saldo total, " +
                 "gastos mensuales, ingresos, deuda pendiente, flujo de 12 meses y distribución " +
                 "de gastos por categoría. Útil para responder preguntas como '¿cuál es mi saldo?' " +
                 "o '¿cuánto gasté este mes?'")
    public Function<WorkspaceRequest, Map<String, Object>> obtenerDashboardFinanciero(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.obtenerDashboardFinanciero(request.workspaceId());
    }
    
    @Bean
    @Description("Busca transacciones (ingresos y gastos) dentro de un espacio de trabajo. " +
                 "Permite filtrar por mes (1-12), año (2020-2026), motivo/categoría " +
                 "(ej: 'Supermercado', 'Salario', 'Alquiler') y por nombre de contacto/destinatario " +
                 "(ej: 'Juan Pérez'). Los filtros son opcionales y combinables entre sí. " +
                 "Sin filtros devuelve las últimas 20 transacciones. " +
                 "Útil para consultas como 'gastos de supermercado en enero', " +
                 "'transacciones de este año' o 'pagos a Juan Pérez'.")
    public Function<BuscarTransaccionesRequest, Object> buscarTransacciones(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.buscarTransacciones(
            request.workspaceId(),
            request.mes(),
            request.anio(),
            request.motivo(),
            request.contacto()
        );
    }
    
    @Bean
    @Description("Lista todas las tarjetas de crédito registradas en el espacio de trabajo " +
                 "con su número, banco emisor, red de pago (Visa/Mastercard), día de cierre " +
                 "y día de vencimiento de pago. Útil para conocer qué tarjetas tiene el usuario.")
    public Function<WorkspaceRequest, Object> listarTarjetasCredito(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarTarjetasCredito(request.workspaceId());
    }
    
    @Bean
    @Description("Lista los resúmenes mensuales de tarjetas de crédito con sus estados: " +
                 "ABIERTO (período de compra), CERRADO (esperando pago), PAGADO (completamente pagado), " +
                 "PAGADO_PARCIAL (pago parcial). Incluye monto total del resumen y fecha de vencimiento. " +
                 "Útil para preguntas sobre deudas de tarjetas o cuánto hay que pagar.")
    public Function<WorkspaceRequest, Object> listarResumenesTarjetas(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarResumenesTarjetas(request.workspaceId());
    }
    
    @Bean
    @Description("Lista las cuentas bancarias del espacio de trabajo con su nombre, " +
                 "entidad financiera y saldo actual. Útil para consultar saldos de cuentas específicas " +
                 "o conocer cuántas cuentas hay disponibles.")
    public Function<WorkspaceRequest, Object> listarCuentasBancarias(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarCuentasBancarias(request.workspaceId());
    }
    
    @Bean
    @Description("Lista todas las categorías o motivos de gastos/ingresos registrados en el espacio " +
                 "(ej: Supermercado, Alquiler, Salario, Transporte, etc.). Útil para saber qué " +
                 "categorías existen antes de buscar transacciones por motivo.")
    public Function<WorkspaceRequest, Object> listarMotivosTransacciones(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarMotivosTransacciones(request.workspaceId());
    }

    @Bean
    @Description("Lista todas las compras realizadas con tarjetas de crédito en el espacio de trabajo, " +
                 "incluyendo las completamente pagadas. Muestra descripción, monto total, número de cuotas " +
                 "pagadas/totales y tarjeta asociada. Útil para ver el historial completo de compras en cuotas.")
    public Function<WorkspaceRequest, Object> buscarTodasComprasCredito(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.buscarTodasComprasCredito(request.workspaceId());
    }

    @Bean
    @Description("Lista únicamente las compras a crédito que aún tienen cuotas pendientes de pago " +
                 "en el espacio de trabajo. Útil para responder '¿qué compras en cuotas me faltan pagar?' " +
                 "o calcular la deuda total en cuotas activas.")
    public Function<WorkspaceRequest, Object> listarComprasCreditoPendientes(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarComprasCreditoPendientes(request.workspaceId());
    }

    @Bean
    @Description("Lista las cuotas del período actual de una tarjeta de crédito específica, incluyendo " +
                 "monto, número de cuota, estado (pagada/pendiente) y fecha. " +
                 "IMPORTANTE: Primero llamar a listarTarjetasCredito para obtener el ID (tarjetaId) de la tarjeta.")
    public Function<TarjetaIdRequest, Object> listarCuotasPorTarjeta(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarCuotasPorTarjeta(request.tarjetaId());
    }

    @Bean
    @Description("Lista el historial completo de resúmenes mensuales de una tarjeta de crédito específica, " +
                 "ordenados del más reciente al más antiguo. Útil para ver la evolución de gastos de una tarjeta " +
                 "en particular. IMPORTANTE: Primero llamar a listarTarjetasCredito para obtener el ID (tarjetaId).")
    public Function<TarjetaIdRequest, Object> listarResumenesPorTarjeta(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarResumenesPorTarjeta(request.tarjetaId());
    }

    @Bean
    @Description("Lista los contactos de transferencia registrados en el espacio de trabajo " +
                 "(personas o entidades a las que se les transfiere dinero), con nombre, alias o CBU/CVU. " +
                 "Útil para identificar destinatarios frecuentes de transferencias.")
    public Function<WorkspaceRequest, Object> listarContactosTransaccion(
        AgenteToolsService toolsService
    ) {
        return request -> toolsService.listarContactosTransaccion(request.workspaceId());
    }

}
