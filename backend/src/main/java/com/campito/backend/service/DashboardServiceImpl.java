package com.campito.backend.service;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.DashboardRepository;
import com.campito.backend.dao.EspacioTrabajoRepository;
import com.campito.backend.dao.GastosIngresosMensualesRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.dto.DashboardStatsDTO;
import com.campito.backend.dto.DistribucionGastoDTO;
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
 */
@Service
@RequiredArgsConstructor  // Genera constructor con todos los campos final para inyección de dependencias
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final EspacioTrabajoRepository espacioRepository;
    private final DashboardRepository dashboardRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final GastosIngresosMensualesRepository gastosIngresosMensualesRepository;

    /**
     * Obtiene las estadísticas consolidadas del dashboard para un espacio de trabajo.
     * 
     * @param idEspacio ID del espacio de trabajo.
     * @return DTO con todas las estadísticas del dashboard (KPIs + charts).
     */
    @Override
    public DashboardStatsDTO obtenerDashboardStats(Long idEspacio) {
        logger.info("Obteniendo estadisticas consolidadas del dashboard para el espacio ID: {}", idEspacio);
        try {
            // 1. Balance total del espacio
            EspacioTrabajo espacio = espacioRepository.findById(idEspacio).orElseThrow(() -> {
                String msg = "Espacio de trabajo con ID " + idEspacio + " no encontrado";
                logger.warn(msg);
                return new EntityNotFoundException(msg);
            });
            Float balanceTotal = espacio.getSaldo();

            // 2. Gastos del mes actual
            ZoneId buenosAiresZone = ZoneId.of("America/Argentina/Buenos_Aires");
            ZonedDateTime nowInBuenosAires = ZonedDateTime.now(buenosAiresZone);
            Integer anioActual = nowInBuenosAires.getYear();
            Integer mesActual = nowInBuenosAires.getMonthValue();

            Optional<GastosIngresosMensuales> opt = gastosIngresosMensualesRepository.findByEspacioTrabajo_IdAndAnioAndMes(idEspacio, anioActual, mesActual);

            GastosIngresosMensuales registro = opt.orElseGet(() -> {
                return GastosIngresosMensuales.builder()
                        .anio(anioActual)
                        .mes(mesActual)
                        .gastos(0f)
                        .ingresos(0f)
                        .espacioTrabajo(espacio)
                        .build();
            });

            Float gastosMensuales = registro.getGastos();

            // 3. Deuda total pendiente (todas las cuotas impagadas)
            Float deudaTotalPendiente = cuotaCreditoRepository.calcularDeudaTotalPendiente(idEspacio);

            // 4. Flujo mensual (últimos 12 meses)
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            
            // Generar lista de los últimos 12 meses (del más antiguo al más reciente)
            List<String> ultimosMeses = new ArrayList<>();
            for (int i = 11; i >= 0; i--) {
                ultimosMeses.add(now.minusMonths(i).format(formatter));
            }
            
            // Obtener los registros existentes de GastosIngresosMensuales
            List<GastosIngresosMensuales> registrosMensuales = gastosIngresosMensualesRepository
                .findByEspacioTrabajoAndMeses(idEspacio, ultimosMeses);
            
            // Crear un mapa para acceso rápido por mes
            Map<String, GastosIngresosMensuales> mapRegistros = new HashMap<>();
            for (GastosIngresosMensuales reg : registrosMensuales) {
                String mesKey = String.format("%d-%02d", reg.getAnio(), reg.getMes());
                mapRegistros.put(mesKey, reg);
            }
            
            // Construir la lista completa con todos los meses (rellenar con ceros los faltantes)
            List<IngresosGastosMesDTO> flujoMensualCompleto = new ArrayList<>();
            for (String mes : ultimosMeses) {
                GastosIngresosMensuales reg = mapRegistros.get(mes);
                if (reg != null) {
                    flujoMensualCompleto.add(new IngresosGastosMesDTOImpl(
                        mes,
                        BigDecimal.valueOf(reg.getIngresos()),
                        BigDecimal.valueOf(reg.getGastos())
                    ));
                } else {
                    // Mes sin datos: ingresos y gastos en cero
                    flujoMensualCompleto.add(new IngresosGastosMesDTOImpl(
                        mes,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                    ));
                }
            }
            
            logger.debug("Flujo mensual calculado con {} registros encontrados de {} meses solicitados", 
                registrosMensuales.size(), ultimosMeses.size());

            // 5. Distribución de gastos por motivo (últimos 12 meses)
            LocalDate fechaLimite = now.minusMonths(12);
            List<DistribucionGastoDTO> distribucionGastos = dashboardRepository.findDistribucionGastos(idEspacio, fechaLimite);

            // 6. Resumen mensual (suma de las cuotas que entrarán en los próximos resúmenes por tarjeta)
            float resumenMensual = 0.0f;
            List<Tarjeta> tarjetas = tarjetaRepository.findByEspacioTrabajo_Id(idEspacio);
            for (Tarjeta tarjeta : tarjetas) {
                int diaCierre = tarjeta.getDiaCierre();

                YearMonth ym = YearMonth.from(now);
                int diaAjustadoCierre = Math.min(diaCierre, ym.lengthOfMonth());
                LocalDate fechaCierre = ym.atDay(diaAjustadoCierre);
                // Queremos el próximo cierre estrictamente en el futuro (si hoy es el día de cierre, tomar el siguiente mes)
                if (!fechaCierre.isAfter(now)) {
                    YearMonth siguiente = ym.plusMonths(1);
                    diaAjustadoCierre = Math.min(diaCierre, siguiente.lengthOfMonth());
                    fechaCierre = siguiente.atDay(diaAjustadoCierre);
                }

                LocalDate fechaInicio = fechaCierre.plusDays(1);
                LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.getDiaVencimientoPago());

                List<CuotaCredito> cuotasPendientes = cuotaCreditoRepository.findByTarjetaSinResumenEnRango(tarjeta.getId(), fechaInicio, fechaFin);
                float monto = cuotasPendientes.stream().map(CuotaCredito::getMontoCuota).reduce(0.0f, Float::sum);
                resumenMensual += monto;
            }

            DashboardStatsDTO stats = new DashboardStatsDTO(
                balanceTotal,
                gastosMensuales,
                resumenMensual,
                deudaTotalPendiente,
                flujoMensualCompleto,
                distribucionGastos
            );

            logger.info("Estadisticas del dashboard para el espacio ID {} generadas exitosamente.", idEspacio);
            return stats;
        } catch (Exception e) {
            logger.error("Error inesperado al obtener estadisticas del dashboard para el espacio ID {}: {}", idEspacio, e.getMessage(), e);
            throw e;
        }
    }

    /*
    ===========================================================================
        MÉTODOS AUXILIARES PRIVADOS
    ===========================================================================
    */

    /**
     * Calcula la fecha de vencimiento del pago del resumen (misma lógica del scheduler)
     */
    private LocalDate calcularFechaVencimiento(LocalDate fechaCierre, int diaVencimiento) {
        YearMonth mesActual = YearMonth.from(fechaCierre);
        YearMonth mesSiguiente = mesActual.plusMonths(1);
        int diaAjustado = Math.min(diaVencimiento, mesSiguiente.lengthOfMonth());
        return mesSiguiente.atDay(diaAjustado);
    }
}
