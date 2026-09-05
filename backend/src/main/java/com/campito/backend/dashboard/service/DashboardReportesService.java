package com.campito.backend.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.campito.backend.common.dto.DistribucionGastoDTO;
import com.campito.backend.transacciones.api.CuotasCreditoApi;
import com.campito.backend.transacciones.api.ReportesTransaccionesApi;

import lombok.RequiredArgsConstructor;

/**
 * Límite de módulo hacia {@code transacciones} para las agregaciones temporales
 * del dashboard.
 *
 * Centraliza el uso de las fachadas {@link ReportesTransaccionesApi} y
 * {@link CuotasCreditoApi} en un bean separado para que las anotaciones
 * {@code @Cacheable} se apliquen correctamente (la invocación cacheada debe
 * pasar por el proxy de Spring, por lo que no puede ocurrir dentro del mismo
 * bean {@code DashboardServiceImpl}).
 */
@Service
@RequiredArgsConstructor
public class DashboardReportesService {

    private final ReportesTransaccionesApi reportesTransaccionesApi;
    private final CuotasCreditoApi cuotasCreditoApi;

    /**
     * Distribución de gastos por motivo desde {@code fechaLimite}.
     */
    @Cacheable(cacheNames = DashboardCacheNames.DISTRIBUCION_GASTOS, key = "#idEspacio")
    public List<DistribucionGastoDTO> distribucionGastos(UUID idEspacio, LocalDate fechaLimite) {
        return reportesTransaccionesApi.findDistribucionGastos(idEspacio, fechaLimite);
    }

    /**
     * Distribución de compras a crédito por motivo desde {@code fechaLimite}.
     */
    @Cacheable(cacheNames = DashboardCacheNames.DISTRIBUCION_COMPRAS_CREDITO, key = "#idEspacio")
    public List<DistribucionGastoDTO> distribucionComprasCredito(UUID idEspacio, LocalDate fechaLimite) {
        return reportesTransaccionesApi.findDistribucionComprasCredito(idEspacio, fechaLimite);
    }

    /**
     * Resumen mensual total pendiente de tarjetas de crédito para la fecha
     * de referencia.
     */
    @Cacheable(cacheNames = DashboardCacheNames.RESUMEN_MENSUAL, key = "#idEspacio")
    public BigDecimal resumenMensual(UUID idEspacio, LocalDate fechaReferencia) {
        return cuotasCreditoApi.resumenMensual(idEspacio, fechaReferencia);
    }
}
