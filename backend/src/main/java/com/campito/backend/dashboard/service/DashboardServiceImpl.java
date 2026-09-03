package com.campito.backend.dashboard.service;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import com.campito.backend.dashboard.domain.dto.DashboardStatsDTO;
import com.campito.backend.common.dto.DistribucionGastoDTO;
import com.campito.backend.dashboard.domain.dto.FlujoCreditoMesDTO;
import com.campito.backend.dashboard.domain.dto.FlujoCreditoMesDTOImpl;
import com.campito.backend.dashboard.domain.dto.IngresosGastosMesDTO;
import com.campito.backend.dashboard.domain.dto.IngresosGastosMesDTOImpl;

import com.campito.backend.dashboard.domain.entity.GastosIngresosMensuales;
import com.campito.backend.dashboard.domain.entity.ResumenFinanciero;

import lombok.RequiredArgsConstructor;

/**
 * Implementación del servicio para gestión del dashboard.
 * 
 * Proporciona métodos para obtener estadísticas y datos relevantes para el dashboard.
 * Las queries independientes a la DB se ejecutan en paralelo usando CompletableFuture
 * para minimizar el tiempo de respuesta total.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final DashboardReportesService dashboardReportesService;
    private final DashboardReadModelService dashboardReadModelService;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    /**
     * Obtiene las estadísticas consolidadas del dashboard para un espacio de trabajo.
     * Ejecuta queries independientes en paralelo para optimizar el tiempo de respuesta.
     * 
     * @param idEspacio ID del espacio de trabajo.
     * @return DTO con todas las estadísticas del dashboard (KPIs + charts).
     * @throws EntityNotFoundException si el espacio de trabajo no se encuentra.
     * @throws IllegalArgumentException si el ID del espacio es nulo.
     */
    @Override
    public DashboardStatsDTO obtenerDashboardStats(UUID idEspacio) {

        log.info("Obteniendo estadisticas consolidadas del dashboard para el espacio ID: {}", idEspacio);

        ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
        ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
        Integer anioActual = nowInBuenosAires.getYear();
        Integer mesActual = nowInBuenosAires.getMonthValue();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<String> ultimosMeses = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            ultimosMeses.add(now.minusMonths(i).format(formatter));
        }

        LocalDate fechaLimite = now.minusMonths(1);

        /* === FASE 1: Leer escalares del read-model desnormalizado en paralelo === */

        CompletableFuture<ResumenFinanciero> resumenFuture = CompletableFuture.supplyAsync(
            () -> dashboardReadModelService.obtenerResumenFinanciero(idEspacio), taskExecutor);

        /* === FASE 2: Ejecutar queries independientes en paralelo === */

        CompletableFuture<List<GastosIngresosMensuales>> registrosMensualesFuture = CompletableFuture.supplyAsync(
            () -> dashboardReadModelService.obtenerRegistrosMensuales(idEspacio, ultimosMeses), taskExecutor);

        CompletableFuture<List<DistribucionGastoDTO>> distribucionGastosFuture = CompletableFuture.supplyAsync(
            () -> dashboardReportesService.distribucionGastos(idEspacio, fechaLimite), taskExecutor);

        CompletableFuture<List<DistribucionGastoDTO>> distribucionComprasCreditoFuture = CompletableFuture.supplyAsync(
            () -> dashboardReportesService.distribucionComprasCredito(idEspacio, fechaLimite), taskExecutor);

        CompletableFuture<BigDecimal> resumenMensualFuture = CompletableFuture.supplyAsync(
            () -> dashboardReportesService.resumenMensual(idEspacio, now), taskExecutor);

        CompletableFuture<BigDecimal> gastosMensualesFuture = CompletableFuture.supplyAsync(
            () -> dashboardReadModelService.gastosMesActual(idEspacio, anioActual, mesActual), taskExecutor);

        /* === FASE 3: Combinar resultados y construir el DTO === */

        CompletableFuture<DashboardStatsDTO> statsFuture = CompletableFuture.allOf(
                resumenFuture,
                gastosMensualesFuture,
                registrosMensualesFuture, distribucionGastosFuture,
                distribucionComprasCreditoFuture, resumenMensualFuture
            ).thenApplyAsync(v -> {

                ResumenFinanciero resumen = resumenFuture.join();
                BigDecimal balanceTotal = resumen.getSaldo();
                BigDecimal deudaTotalPendiente = resumen.getDeudaTotal();
                BigDecimal gastosMensuales = gastosMensualesFuture.join();
                List<GastosIngresosMensuales> registrosMensuales = registrosMensualesFuture.join();
                List<DistribucionGastoDTO> distribucionGastos = distribucionGastosFuture.join();
                List<DistribucionGastoDTO> distribucionComprasCredito = distribucionComprasCreditoFuture.join();
                BigDecimal resumenMensualTotal = resumenMensualFuture.join();

                Map<String, GastosIngresosMensuales> mapRegistros = new HashMap<>();
                for (GastosIngresosMensuales reg : registrosMensuales) {
                    String mesKey = String.format("%d-%02d", reg.getAnio(), reg.getMes());
                    mapRegistros.put(mesKey, reg);
                }

                List<IngresosGastosMesDTO> flujoMensualCompleto = FlujoMensual(now, ultimosMeses, mapRegistros);
                List<FlujoCreditoMesDTO> flujoTarjetaMensualCompleto = FlujoCreditoMensual(ultimosMeses, mapRegistros);

                log.debug("Flujo mensual calculado con {} registros encontrados de {} meses solicitados",
                    registrosMensuales.size(), ultimosMeses.size());

                DashboardStatsDTO stats = new DashboardStatsDTO(
                    balanceTotal,
                    gastosMensuales,
                    resumenMensualTotal,
                    deudaTotalPendiente,
                    flujoMensualCompleto,
                    distribucionGastos,
                    flujoTarjetaMensualCompleto,
                    distribucionComprasCredito
                );

                log.info("Estadisticas del dashboard para el espacio ID {} generadas exitosamente.", idEspacio);
                return stats;
            }, taskExecutor);

        try {
            return statsFuture.orTimeout(30, TimeUnit.SECONDS).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof TimeoutException) {
                throw new RuntimeException("Timeout al obtener estadísticas del dashboard para el espacio " + idEspacio, cause);
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw e;
        }
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */

    /**
     * Obtiene el flujo mensual de ingresos y gastos para los últimos 12 meses.
     * Rellena con ceros los meses que no tengan registros.
     */
    private List<IngresosGastosMesDTO> FlujoMensual(LocalDate now, List<String> ultimosMeses, Map<String, GastosIngresosMensuales> mapRegistros) {
        
        // Construir la lista completa con todos los meses (rellenar con ceros los faltantes)
        List<IngresosGastosMesDTO> flujoMensualCompleto = new ArrayList<>();
        for (String mes : ultimosMeses) {
            GastosIngresosMensuales reg = mapRegistros.get(mes);
            if (reg != null) {
                flujoMensualCompleto.add(new IngresosGastosMesDTOImpl(
                    mes,
                    reg.getIngresos(),
                    reg.getGastos()
                ));
            } else {
                flujoMensualCompleto.add(new IngresosGastosMesDTOImpl(
                    mes,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
                ));
            }
        }
        return flujoMensualCompleto;
    }

    /* Obtener el flujo mensual de tarjetas para los últimos 12 meses */
    private List<FlujoCreditoMesDTO> FlujoCreditoMensual(List<String> ultimosMeses, Map<String, GastosIngresosMensuales> mapRegistros) {
        List<FlujoCreditoMesDTO> flujoTarjetaMensualCompleto = new ArrayList<>();
        for (String mes : ultimosMeses) {
            GastosIngresosMensuales reg = mapRegistros.get(mes);
            if (reg != null) {
                flujoTarjetaMensualCompleto.add(new FlujoCreditoMesDTOImpl(
                    mes,
                    reg.getComprasCredito() != null ? reg.getComprasCredito() : BigDecimal.ZERO,
                    reg.getPagoResumen() != null ? reg.getPagoResumen() : BigDecimal.ZERO
                ));
            } else {
                flujoTarjetaMensualCompleto.add(new FlujoCreditoMesDTOImpl(
                    mes,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
                ));
            }
        }
        return flujoTarjetaMensualCompleto;
    }
}
