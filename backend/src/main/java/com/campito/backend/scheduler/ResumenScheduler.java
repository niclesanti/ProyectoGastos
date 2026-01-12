package com.campito.backend.scheduler;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.CuotaCreditoRepository;
import com.campito.backend.dao.ResumenRepository;
import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.model.CuotaCredito;
import com.campito.backend.model.EstadoResumen;
import com.campito.backend.model.Resumen;
import com.campito.backend.model.Tarjeta;

import lombok.RequiredArgsConstructor;

/**
 * Scheduler que ejecuta el cierre automático de resúmenes de tarjeta a medianoche.
 * 
 * Este componente actúa como un "Cierre de Lote", identificando todas las tarjetas
 * cuyo día de cierre coincide con el día actual, agrupando las cuotas pendientes
 * y generando los resúmenes correspondientes.
 */
@Component
@RequiredArgsConstructor
public class ResumenScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ResumenScheduler.class);

    private final TarjetaRepository tarjetaRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final ResumenRepository resumenRepository;

    /**
     * Ejecuta el cierre de resúmenes todos los días a las 00:00hs.
     * Procesa el cierre del DÍA ANTERIOR para asegurar que todas las transacciones
     * de ese día ya estén registradas en el sistema.
     * 
     * Cron: segundo minuto hora día mes día_semana
     * TEMPORAL PARA TESTING: Ejecuta cada minuto
     */
    @Scheduled(cron = "0 * * * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void cerrarResumenesDiarios() {
        // CORRECCIÓN: Procesar el cierre de AYER, no de HOY
        LocalDate ayer = LocalDate.now().minusDays(1);
        int diaACerrar = ayer.getDayOfMonth();
        
        logger.info("Iniciando cierre automático de resúmenes para tarjetas que cerraron ayer: día {}", diaACerrar);
        
        // Obtener todas las tarjetas cuyo día de cierre fue ayer
        List<Tarjeta> tarjetasACerrar = tarjetaRepository.findAll().stream()
            .filter(tarjeta -> tarjeta.getDiaCierre().equals(diaACerrar))
            .toList();
        
        logger.info("Encontradas {} tarjetas con día de cierre {}", tarjetasACerrar.size(), diaACerrar);
        
        for (Tarjeta tarjeta : tarjetasACerrar) {
            try {
                cerrarResumenTarjeta(tarjeta, ayer);
            } catch (Exception e) {
                logger.error("Error al cerrar resumen de tarjeta ID: {}", tarjeta.getId(), e);
            }
        }
        
        logger.info("Cierre automático de resúmenes finalizado");
    }

    /**
     * Cierra el resumen de una tarjeta específica.
     * 
     * @param tarjeta La tarjeta a cerrar
     * @param fechaCierre La fecha de cierre
     */
    private void cerrarResumenTarjeta(Tarjeta tarjeta, LocalDate fechaCierre) {
        // Calcular mes y año del resumen
        YearMonth mesResumen = YearMonth.from(fechaCierre);
        int anio = mesResumen.getYear();
        int mes = mesResumen.getMonthValue();
        
        // Verificar si ya existe un resumen para este período
        if (resumenRepository.findByTarjetaAndAnioAndMes(tarjeta.getId(), anio, mes).isPresent()) {
            logger.warn("Ya existe un resumen para tarjeta ID {} del período {}/{}", 
                tarjeta.getId(), mes, anio);
            return;
        }
        
        // Calcular fechas del período del resumen
        // Las cuotas que vencen DESPUÉS del cierre actual y HASTA el día de vencimiento del pago
        LocalDate fechaInicio = fechaCierre.plusDays(1);
        LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.getDiaVencimientoPago());
        
        logger.info("Buscando cuotas para tarjeta ID {} con fechaVencimiento ENTRE {} y {}", 
            tarjeta.getId(), fechaInicio, fechaFin);
        
        // Buscar cuotas sin resumen asociado en el rango de fechas
        List<CuotaCredito> cuotasPendientes = cuotaCreditoRepository
            .findByTarjetaSinResumenEnRango(tarjeta.getId(), fechaInicio, fechaFin);
        
        logger.info("Encontradas {} cuotas pendientes para tarjeta ID {}", 
            cuotasPendientes.size(), tarjeta.getId());
        
        if (cuotasPendientes.isEmpty()) {
            logger.info("No hay cuotas pendientes para cerrar en tarjeta ID {} del período {}/{}", 
                tarjeta.getId(), mes, anio);
            return;
        }
        
        // Calcular monto total del resumen
        float montoTotal = cuotasPendientes.stream()
            .map(CuotaCredito::getMontoCuota)
            .reduce(0.0f, Float::sum);
        
        // Calcular fecha de vencimiento del pago
        LocalDate fechaVencimiento = calcularFechaVencimiento(fechaCierre, tarjeta.getDiaVencimientoPago());
        
        // Crear el resumen
        Resumen resumen = Resumen.builder()
            .anio(anio)
            .mes(mes)
            .fechaVencimiento(fechaVencimiento)
            .estado(EstadoResumen.CERRADO)
            .montoTotal(montoTotal)
            .tarjeta(tarjeta)
            .build();
        
        resumen = resumenRepository.save(resumen);
        
        // Asociar cuotas al resumen
        for (CuotaCredito cuota : cuotasPendientes) {
            cuota.setResumenAsociado(resumen);
        }
        cuotaCreditoRepository.saveAll(cuotasPendientes);
        
        logger.info("Resumen cerrado exitosamente para tarjeta ID {} - Período: {}/{} - Monto: ${} - Cuotas: {}",
            tarjeta.getId(), mes, anio, montoTotal, cuotasPendientes.size());
    }

    /**
     * Calcula la fecha de vencimiento del pago del resumen.
     * Basado en el día de vencimiento configurado en la tarjeta.
     */
    private LocalDate calcularFechaVencimiento(LocalDate fechaCierre, int diaVencimiento) {
        YearMonth mesActual = YearMonth.from(fechaCierre);
        YearMonth mesSiguiente = mesActual.plusMonths(1);
        
        // Ajustar el día si excede el último día del mes
        int diaAjustado = Math.min(diaVencimiento, mesSiguiente.lengthOfMonth());
        
        return mesSiguiente.atDay(diaAjustado);
    }
}
