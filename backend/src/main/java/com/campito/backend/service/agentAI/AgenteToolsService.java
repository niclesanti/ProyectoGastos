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
     * Sin filtros, devuelve las últimas 50 transacciones.
     * 
     * @param workspaceId ID del espacio de trabajo (UUID como String)
     * @param mes Mes a filtrar (1-12, opcional)
     * @param anio Año a filtrar (2020-2026, opcional)
     * @param motivo Nombre del motivo/categoría (ej: "Supermercado", opcional)
     * @return Lista de transacciones que cumplen los criterios
     */
    public List<TransaccionDTOResponse> buscarTransacciones(
        String workspaceId, 
        Integer mes, 
        Integer anio, 
        String motivo
    ) {
        log.info("Agente llamando tool: buscarTransacciones({}, mes={}, anio={}, motivo={})", 
            workspaceId, mes, anio, motivo);
        
        UUID workspaceUuid = UUID.fromString(workspaceId);
        securityService.validateWorkspaceAccess(workspaceUuid);
        
        var busqueda = new TransaccionBusquedaDTO(
            mes,
            anio,
            motivo,
            null,          // contacto (nombre, no ID)
            workspaceUuid, // idEspacioTrabajo
            0,             // page
            50             // size máximo
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
}
