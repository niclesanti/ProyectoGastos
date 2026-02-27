package com.campito.backend.service.agentAI;

import com.campito.backend.dto.*;
import com.campito.backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio que expone herramientas (tools) para el agente IA.
 * Cada método público es una función que el LLM puede llamar vía Function Calling.
 * 
 * IMPORTANTE: Todas las herramientas validan permisos multi-tenant usando SecurityService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AgenteToolsService {
    
    private final DashboardService dashboardService;
    private final TransaccionService transaccionService;
    private final CompraCreditoService compraCreditoService;
    private final CuentaBancariaService cuentaBancariaService;
    private final SecurityService securityService;
    
    /**
     * Obtiene el resumen financiero completo del espacio de trabajo.
     * Incluye: balance total, gastos mensuales, ingresos, deuda pendiente, 
     * flujo de 12 meses y distribución de gastos por categoría.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Mapa con todas las estadísticas financieras
     */
    public Map<String, Object> obtenerDashboardFinanciero(String workspaceId) {
        log.info("Agente llamando tool: obtenerDashboardFinanciero({})", workspaceId);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        var stats = dashboardService.obtenerDashboardStats(workspaceUuid);
        
        return Map.of(
            "saldoTotal", stats.balanceTotal(),
            "gastosMensuales", stats.gastosMensuales(),
            "resumenMensual", stats.resumenMensual(),
            "deudaTotalPendiente", stats.deudaTotalPendiente(),
            "flujoUltimos12Meses", stats.flujoMensual(),
            "distribucionGastos", stats.distribucionGastos()
        );
    }
    
    /**
     * Busca transacciones con filtros opcionales.
     * Sin filtros, devuelve las últimas 20 transacciones.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @param mes Mes a filtrar (1-12, opcional)
     * @param anio Año a filtrar (2020-2026, opcional)
     * @param motivo Nombre del motivo/categoría (ej: "Supermercado", opcional)
     * @param contacto Nombre del contacto/destinatario (ej: "Juan Pérez", opcional)
     * @return Lista de transacciones que cumplen los criterios
     */
    public List<TransaccionDTOResponse> buscarTransacciones(
        String workspaceId,
        Integer mes,
        Integer anio,
        String motivo,
        String contacto
    ) {
        log.info("Agente llamando tool: buscarTransacciones({}, mes={}, anio={}, motivo={}, contacto={})",
            workspaceId, mes, anio, motivo, contacto);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        // Limitado a 20 items para no exceder el TPM de Groq en la llamada de síntesis
        var busqueda = new TransaccionBusquedaDTO(
            mes,
            anio,
            motivo,
            contacto,      // filtro por nombre de contacto
            workspaceUuid, // idEspacioTrabajo
            0,             // page
            20             // size máximo – reducido para control de tokens
        );
        
        return transaccionService.buscarTransaccion(busqueda).getContent();
    }
    
    /**
     * Lista las tarjetas de crédito del espacio de trabajo.
     * Incluye: número, banco emisor, red de pago, día cierre y vencimiento.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista de tarjetas con sus detalles
     */
    public List<TarjetaDTOResponse> listarTarjetasCredito(String workspaceId) {
        log.info("Agente llamando tool: listarTarjetasCredito({})", workspaceId);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        return compraCreditoService.listarTarjetas(workspaceUuid);
    }
    
    /**
     * Lista los resúmenes mensuales de tarjetas con sus estados.
     * Estados: ABIERTO (comprando), CERRADO (esperando pago), 
     *          PAGADO (completo), PAGADO_PARCIAL (parcial).
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista de resúmenes con monto total y fecha vencimiento
     */
    public List<ResumenDTOResponse> listarResumenesTarjetas(String workspaceId) {
        log.info("Agente llamando tool: listarResumenesTarjetas({})", workspaceId);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        return compraCreditoService.listarResumenesPorEspacioTrabajo(workspaceUuid);
    }
    
    /**
     * Lista las cuentas bancarias del espacio de trabajo.
     * Incluye: nombre, entidad financiera y saldo actual.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista de cuentas con sus saldos
     */
    public List<CuentaBancariaDTOResponse> listarCuentasBancarias(String workspaceId) {
        log.info("Agente llamando tool: listarCuentasBancarias({})", workspaceId);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        return cuentaBancariaService.listarCuentasBancarias(workspaceUuid);
    }
    
    /**
     * Lista las categorías/motivos de transacciones disponibles en el espacio.
     * Útil para conocer qué categorías existen antes de filtrar transacciones.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista de motivos (ej: Supermercado, Alquiler, Salario, etc.)
     */
    public List<MotivoDTOResponse> listarMotivosTransacciones(String workspaceId) {
        log.info("Agente llamando tool: listarMotivosTransacciones({})", workspaceId);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        return transaccionService.listarMotivos(workspaceUuid);
    }

    /**
     * Lista todas las compras a crédito del espacio de trabajo (incluyendo las totalmente pagadas).
     * Incluye: descripción, monto total, cuotas totales/pagas, tarjeta asociada y fecha.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista completa de compras a crédito
     */
    public List<CompraCreditoDTOResponse> buscarTodasComprasCredito(String workspaceId) {
        log.info("Agente llamando tool: buscarTodasComprasCredito({})", workspaceId);

        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);

        // Limitado a 20 items para no exceder el TPM de Groq
        return compraCreditoService.BuscarComprasCredito(workspaceUuid)
                .stream().limit(20).toList();
    }

    /**
     * Lista las compras a crédito que aún tienen cuotas pendientes de pago.
     * Útil para conocer las deudas en cuotas activas del espacio de trabajo.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista de compras con cuotas pendientes
     */
    public List<CompraCreditoDTOResponse> listarComprasCreditoPendientes(String workspaceId) {
        log.info("Agente llamando tool: listarComprasCreditoPendientes({})", workspaceId);

        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);

        // Limitado a 20 items para no exceder el TPM de Groq
        return compraCreditoService.listarComprasCreditoDebeCuotas(workspaceUuid, 0, 20).getContent();
    }

    /**
     * Lista las cuotas del período actual de una tarjeta de crédito específica.
     * Incluye: monto de la cuota, número de cuota, estado (pagada/pendiente) y fecha.
     * Llamar primero a listarTarjetasCredito para obtener los IDs disponibles.
     * 
     * @param idTarjeta ID numérico de la tarjeta de crédito
     * @return Lista de cuotas de la tarjeta en el período actual
     */
    public List<CuotaCreditoDTOResponse> listarCuotasPorTarjeta(Long idTarjeta) {
        log.info("Agente llamando tool: listarCuotasPorTarjeta({})", idTarjeta);

        securityService.validateTarjetaOwnership(idTarjeta);

        // Limitado a 24 cuotas para no exceder el TPM de Groq
        return compraCreditoService.listarCuotasPorTarjeta(idTarjeta)
                .stream().limit(24).toList();
    }

    /**
     * Lista el historial de resúmenes de una tarjeta de crédito específica ordenados por fecha descendente.
     * Útil para ver la evolución mensual de gastos de una tarjeta en particular.
     * Llamar primero a listarTarjetasCredito para obtener los IDs disponibles.
     * 
     * @param idTarjeta ID numérico de la tarjeta de crédito
     * @return Lista de resúmenes históricos de la tarjeta
     */
    public List<ResumenDTOResponse> listarResumenesPorTarjeta(Long idTarjeta) {
        log.info("Agente llamando tool: listarResumenesPorTarjeta({})", idTarjeta);

        securityService.validateTarjetaOwnership(idTarjeta);

        // Limitado a 6 resúmenes (últimos 6 meses) para no exceder el TPM de Groq
        return compraCreditoService.listarResumenesPorTarjeta(idTarjeta)
                .stream().limit(6).toList();
    }

    /**
     * Lista los contactos de transferencia registrados en el espacio de trabajo.
     * Incluye: nombre completo, alias o CBU/CVU. Útil para identificar destinatarios frecuentes.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @return Lista de contactos de transferencia
     */
    public List<ContactoDTOResponse> listarContactosTransaccion(String workspaceId) {
        log.info("Agente llamando tool: listarContactosTransaccion({})", workspaceId);

        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);

        return transaccionService.listarContactos(workspaceUuid);
    }

}
