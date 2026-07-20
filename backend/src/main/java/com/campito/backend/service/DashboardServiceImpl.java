package com.campito.backend.service;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.DashboardRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.GastosIngresosMensualesRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dto.DashboardStatsDTO;
import com.campito.backend.dto.DistribucionGastoDTO;
import com.campito.backend.dto.FlujoCreditoMesDTO;
import com.campito.backend.dto.FlujoCreditoMesDTOImpl;
import com.campito.backend.dto.IngresosGastosMesDTO;
import com.campito.backend.dto.IngresosGastosMesDTOImpl;
import com.campito.backend.model.CuotaCredito;
import com.campito.backend.model.EspacioTrabajo;
import com.campito.backend.model.GastosIngresosMensuales;
import com.campito.backend.model.Tarjeta;

import jakarta.persistence.EntityNotFoundException;
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

    private final EspacioTrabajoRepository espacioRepository;
    private final DashboardRepository dashboardRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

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

        /* === FASE 1: Ejecutar queries independientes en paralelo === */

        CompletableFuture<EspacioTrabajo> espacioFuture = CompletableFuture.supplyAsync(
            () -> buscarEspacioTrabajoPorId(idEspacio), taskExecutor);

        CompletableFuture<BigDecimal> deudaFuture = CompletableFuture.supplyAsync(
            () -> cuotaCreditoRepository.calcularDeudaTotalPendiente(idEspacio), taskExecutor);

        CompletableFuture<List<GastosIngresosMensuales>> registrosMensualesFuture = CompletableFuture.supplyAsync(
            () -> gastosIngresosMensualesRepository.findByEspacioTrabajoAndMeses(idEspacio, ultimosMeses), taskExecutor);

        CompletableFuture<List<DistribucionGastoDTO>> distribucionGastosFuture = CompletableFuture.supplyAsync(
            () -> dashboardRepository.findDistribucionGastos(idEspacio, fechaLimite), taskExecutor);

        CompletableFuture<List<DistribucionGastoDTO>> distribucionComprasCreditoFuture = CompletableFuture.supplyAsync(
            () -> dashboardRepository.findDistribucionComprasCredito(idEspacio, fechaLimite), taskExecutor);

        CompletableFuture<BigDecimal> resumenMensualFuture = CompletableFuture.supplyAsync(
            () -> resumenMensual(idEspacio, now), taskExecutor);

        /* === FASE 2: Resolver dependencias encadenadas === */

        // gastosMesActual depende del espacio (necesita el objeto EspacioTrabajo)
        CompletableFuture<BigDecimal> gastosMensualesFuture = espacioFuture.thenApplyAsync(
            espacio -> gastosMesActual(espacio, anioActual, mesActual), taskExecutor);

        /* === FASE 3: Combinar resultados y construir el DTO === */

        CompletableFuture<DashboardStatsDTO> statsFuture = CompletableFuture.allOf(
                espacioFuture, gastosMensualesFuture, deudaFuture,
                registrosMensualesFuture, distribucionGastosFuture,
                distribucionComprasCreditoFuture, resumenMensualFuture
            ).thenApplyAsync(v -> {

                EspacioTrabajo espacio = espacioFuture.join();
                BigDecimal balanceTotal = espacio.getSaldo();
                BigDecimal gastosMensuales = gastosMensualesFuture.join();
                BigDecimal deudaTotalPendiente = deudaFuture.join();
                List<GastosIngresosMensuales> registrosMensuales = registrosMensualesFuture.join();
                List<DistribucionGastoDTO> distribucionGastos = distribucionGastosFuture.join();
                List<DistribucionGastoDTO> distribucionComprasCredito = distribucionComprasCreditoFuture.join();
                BigDecimal resumenMensualTotal = resumenMensualFuture.join();

                Map<String, GastosIngresosMensuales> mapRegistros = new HashMap<>();
                for (GastosIngresosMensuales reg : registrosMensuales) {
                    String mesKey = String.format("%d-%02d", reg.getAnio(), reg.getMes());
                    mapRegistros.put(mesKey, reg);
                }

                List<IngresosGastosMesDTO> flujoMensualCompleto = FlujoMensual(now, idEspacio, ultimosMeses, mapRegistros);
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
            return statsFuture.orTimeout(30, java.util.concurrent.TimeUnit.SECONDS).join();
        } catch (java.util.concurrent.CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.util.concurrent.TimeoutException) {
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

    private EspacioTrabajo buscarEspacioTrabajoPorId(UUID idEspacio) {
        return espacioRepository.findById(idEspacio).orElseThrow(() -> {
            String msg = "Espacio de trabajo con ID " + idEspacio + " no encontrado";
            log.warn(msg);
            return new EntityNotFoundException(msg);
        });
    }

    /**
     * Calcula la fecha de vencimiento del pago del resumen (misma lógica del scheduler)
     */
    private LocalDate calcularFechaVencimiento(LocalDate fechaCierre, int diaVencimiento) {
        YearMonth mesActual = YearMonth.from(fechaCierre);
        YearMonth mesSiguiente = mesActual.plusMonths(1);
        int diaAjustado = Math.min(diaVencimiento, mesSiguiente.lengthOfMonth());
        return mesSiguiente.atDay(diaAjustado);
    }

    /**
     * Calcula el total de gastos del mes actual para el espacio dado.
     */
    private BigDecimal gastosMesActual(EspacioTrabajo espacio, Integer anioActual, Integer mesActual) {
        Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(espacio.getId(), anioActual, mesActual);

        GastosIngresosMensuales registro = opt.orElseGet(() -> {
            return GastosIngresosMensuales.builder()
                    .anio(anioActual)
                    .mes(mesActual)
                    .gastos(BigDecimal.ZERO)
                    .ingresos(BigDecimal.ZERO)
                    .espacioTrabajo(espacio)
                    .build();
        });

        return registro.getGastos();
    }

    /**
     * Obtiene el flujo mensual de ingresos y gastos para los últimos 12 meses.
     * Rellena con ceros los meses que no tengan registros.
     */
    private List<IngresosGastosMesDTO> FlujoMensual(LocalDate now, UUID idEspacio, List<String> ultimosMeses, Map<String, GastosIngresosMensuales> mapRegistros) {
        
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

    /**
     * Calcula el resumen mensual total (suma de cuotas que entrarán en próximos resúmenes).
     * Optimizado para minimizar queries: trae todas las tarjetas, calcula el rango máximo de 
     * fechas y luego trae todas las cuotas pendientes en ese rango para filtrar en memoria.
     */
    private BigDecimal resumenMensual(UUID idEspacio, LocalDate now) {
        List<Tarjeta> tarjetas = tarjetaRepository.findByEspacioTrabajo_Id(idEspacio);
        
        if (tarjetas.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 2. Calcular el rango de fechas más amplio posible para todas las tarjetas
        //    Esto permite traer todas las cuotas relevantes en una sola query
        LocalDate fechaInicioMinima = now;
        LocalDate fechaFinMaxima = now;
        
        for (Tarjeta tarjeta : tarjetas) {
            YearMonth ym = YearMonth.from(now);
            int diaAjustadoCierre = Math.min(tarjeta.getDiaCierre(), ym.lengthOfMonth());
            LocalDate fechaCierre = ym.atDay(diaAjustadoCierre);
            
            if (!fechaCierre.isAfter(now)) {
                YearMonth siguiente = ym.plusMonths(1);
                diaAjustadoCierre = Math.min(tarjeta.getDiaCierre(), siguiente.lengthOfMonth());
                fechaCierre = siguiente.atDay(diaAjustadoCierre);
            }
            
            LocalDate fechaInicio = fechaCierre.plusDays(1);
            LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.getDiaVencimientoPago());
            
            if (fechaInicio.isBefore(fechaInicioMinima)) {
                fechaInicioMinima = fechaInicio;
            }
            if (fechaFin.isAfter(fechaFinMaxima)) {
                fechaFinMaxima = fechaFin;
            }
        }
        
        // 3. Traer TODAS las cuotas pendientes sin resumen en el rango amplio (1 query batch)
        //    Usamos la nueva query optimizada que trae todo de una vez
        List<CuotaCredito> todasLasCuotasPendientes = cuotaCreditoRepository
            .findByEspacioTrabajoSinResumenEnRango(idEspacio, fechaInicioMinima, fechaFinMaxima);
        
        // 4. Filtrar y sumar en memoria según el período específico de cada tarjeta
        BigDecimal resumenMensual = BigDecimal.ZERO;
        
        for (Tarjeta tarjeta : tarjetas) {
            int diaCierre = tarjeta.getDiaCierre();
            
            YearMonth ym = YearMonth.from(now);
            int diaAjustadoCierre = Math.min(diaCierre, ym.lengthOfMonth());
            LocalDate fechaCierre = ym.atDay(diaAjustadoCierre);
            
            if (!fechaCierre.isAfter(now)) {
                YearMonth siguiente = ym.plusMonths(1);
                diaAjustadoCierre = Math.min(diaCierre, siguiente.lengthOfMonth());
                fechaCierre = siguiente.atDay(diaAjustadoCierre);
            }
            
            LocalDate fechaInicio = fechaCierre.plusDays(1);
            LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.getDiaVencimientoPago());
            
            // Filtrar cuotas de esta tarjeta en su período específico
            BigDecimal montoTarjeta = todasLasCuotasPendientes.stream()
                .filter(cuota -> cuota.getCompraCredito().getTarjeta().getId().equals(tarjeta.getId()))
                .filter(cuota -> !cuota.getFechaVencimiento().isBefore(fechaInicio) 
                              && !cuota.getFechaVencimiento().isAfter(fechaFin))
                .map(CuotaCredito::getMontoCuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            resumenMensual = resumenMensual.add(montoTarjeta);
        }
        
        return resumenMensual;
    }
}
