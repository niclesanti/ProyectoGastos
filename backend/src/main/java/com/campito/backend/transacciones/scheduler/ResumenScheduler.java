package com.campito.backend.transacciones.scheduler;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Dependencias a revisar para reducir acoplamiento y mejorar la modularidad
import com.campito.backend.shared.event.NotificacionEvent;
import com.campito.backend.shared.event.TipoNotificacion;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;

import com.campito.backend.transacciones.repository.CuotaCreditoRepository;
import com.campito.backend.transacciones.repository.ResumenRepository;
import com.campito.backend.transacciones.repository.TarjetaRepository;
import com.campito.backend.transacciones.domain.entity.CuotaCredito;
import com.campito.backend.transacciones.domain.entity.EstadoResumen;
import com.campito.backend.transacciones.domain.entity.Resumen;
import com.campito.backend.transacciones.domain.entity.Tarjeta;
import com.campito.backend.util.MoneyUtils;

import lombok.RequiredArgsConstructor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import com.campito.backend.config.MetricsConfig;

/**
 * Scheduler que ejecuta el cierre automático de resúmenes de tarjeta a medianoche.
 * 
 * Este componente actúa como un "Cierre de Lote", identificando todas las tarjetas
 * cuyo día de cierre coincide con el día actual, agrupando las cuotas pendientes
 * y generando los resúmenes correspondientes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResumenScheduler {

    private final TarjetaRepository tarjetaRepository;
    private final CuotaCreditoRepository cuotaCreditoRepository;
    private final ResumenRepository resumenRepository;
    private final EspacioTrabajoApi espacioTrabajoApi;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;  // Para métricas de Prometheus/Grafana

    /**
     * Ejecuta el cierre de resúmenes todos los días a las 00:00hs.
     * Procesa el cierre del DÍA ANTERIOR para asegurar que todas las transacciones
     * de ese día ya estén registradas en el sistema.
     * 
     * Cron: segundo minuto hora día mes día_semana
     * TEMPORAL PARA TESTING: Ejecuta cada minuto
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void cerrarResumenesDiarios() {

        LocalDate ayer = LocalDate.now().minusDays(1);
        int diaACerrar = ayer.getDayOfMonth();
        
        log.info("Iniciando cierre automático de resúmenes para tarjetas que cerraron ayer: día {}", diaACerrar);
        
        // 📊 MÉTRICA: Medir tiempo total de ejecución del cierre de resúmenes
        Timer.Sample timerSample = Timer.start(meterRegistry);
        
        // Obtener todas las tarjetas cuyo día de cierre fue ayer
        List<Tarjeta> tarjetasACerrar = tarjetaRepository.findAll().stream()
            .filter(tarjeta -> tarjeta.getDiaCierre().equals(diaACerrar))
            .toList();
        
        log.info("Encontradas {} tarjetas con día de cierre {}", tarjetasACerrar.size(), diaACerrar);
        
        // Variables para métricas de negocio
        int resumenesGenerados = 0;
        int errores = 0;
        
        for (Tarjeta tarjeta : tarjetasACerrar) {
            try {
                boolean generado = cerrarResumenTarjeta(tarjeta, ayer);
                if (generado) {
                    resumenesGenerados++;
                }
            } catch (Exception e) {
                log.error("Error al cerrar resumen de tarjeta ID: {}", tarjeta.getId(), e);
                errores++;
                
                // 📊 MÉTRICA: Incrementar contador de errores
                Counter.builder(MetricsConfig.MetricNames.RESUMENES_ERRORES)
                        .description("Total de errores al generar resúmenes")
                        .tag("tarjeta_id", tarjeta.getId().toString())
                        .register(meterRegistry)
                        .increment();
            }
        }
        
        // 📊 MÉTRICA: Registrar tiempo de ejecución
        timerSample.stop(Timer.builder(MetricsConfig.MetricNames.RESUMENES_TIMER)
                .description("Tiempo de ejecución del scheduler de cierre de resúmenes")
                .tag("resultado", errores > 0 ? "con_errores" : "exitoso")
                .register(meterRegistry));
        
        // 📊 MÉTRICA: Incrementar contador de resúmenes generados
        if (resumenesGenerados > 0) {
            Counter.builder(MetricsConfig.MetricNames.RESUMENES_GENERADOS)
                    .description("Total de resúmenes generados por el scheduler")
                    .register(meterRegistry)
                    .increment(resumenesGenerados);
        }
        
        log.info("Cierre automático de resúmenes finalizado - Generados: {} - Errores: {}", resumenesGenerados, errores);
    }

    /**
     * Cierra el resumen de una tarjeta específica.
     * 
     * @param tarjeta La tarjeta a cerrar
     * @param fechaCierre La fecha de cierre
     * @return true si se generó el resumen, false si ya existía o no había cuotas
     */
    private boolean cerrarResumenTarjeta(Tarjeta tarjeta, LocalDate fechaCierre) {
        // Calcular mes y año del resumen
        YearMonth mesResumen = YearMonth.from(fechaCierre);
        int anio = mesResumen.getYear();
        int mes = mesResumen.getMonthValue();
        
        // Verificar si ya existe un resumen para este período
        if (resumenRepository.findByTarjetaAndAnioAndMes(tarjeta.getId(), anio, mes).isPresent()) {
            log.warn("Ya existe un resumen para tarjeta ID {} del período {}/{}", 
                tarjeta.getId(), mes, anio);
            return false;
        }
        
        // Calcular fechas del período del resumen
        // Las cuotas que vencen DESPUÉS del cierre actual y HASTA el día de vencimiento del pago
        LocalDate fechaInicio = fechaCierre.plusDays(1);
        LocalDate fechaFin = calcularFechaVencimiento(fechaCierre, tarjeta.getDiaVencimientoPago());
        
        log.info("Buscando cuotas para tarjeta ID {} con fechaVencimiento ENTRE {} y {}", 
            tarjeta.getId(), fechaInicio, fechaFin);
        
        // Buscar cuotas sin resumen asociado en el rango de fechas
        List<CuotaCredito> cuotasPendientes = cuotaCreditoRepository
            .findByTarjetaSinResumenEnRango(tarjeta.getId(), fechaInicio, fechaFin);
        
        log.info("Encontradas {} cuotas pendientes para tarjeta ID {}", 
            cuotasPendientes.size(), tarjeta.getId());
        
        if (cuotasPendientes.isEmpty()) {
            log.info("No hay cuotas pendientes para cerrar en tarjeta ID {} del período {}/{}", 
                tarjeta.getId(), mes, anio);
            return false;
        }
        
        // Calcular monto total del resumen
        BigDecimal montoTotal = MoneyUtils.sum(
            cuotasPendientes.stream()
                .map(CuotaCredito::getMontoCuota)
                .toList()
        );
        
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
        
        log.info("Resumen cerrado exitosamente para tarjeta ID {} - Período: {}/{} - Monto: ${} - Cuotas: {}",
            tarjeta.getId(), mes, anio, montoTotal, cuotasPendientes.size());
        
        // Emitir evento de notificación al administrador del espacio de trabajo
        try {
            UUID idUsuarioAdmin = espacioTrabajoApi.obtenerIdUsuarioAdmin(tarjeta.getIdEspacioTrabajo());
            String numeroTarjeta = tarjeta.getNumeroTarjeta();
            String redDePago = tarjeta.getRedDePago();
            String fechaVencimientoStr = fechaVencimiento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            String mensaje = String.format("Resumen cerrado de tarjeta %s terminada en %s. Vencimiento: %s", 
                                          redDePago, numeroTarjeta, fechaVencimientoStr);
            
            eventPublisher.publishEvent(new NotificacionEvent(
                this,
                idUsuarioAdmin,
                TipoNotificacion.CIERRE_TARJETA,
                mensaje
            ));
            log.info("Evento de notificación enviado al usuario {} por cierre de resumen de tarjeta {}", 
                       idUsuarioAdmin, tarjeta.getId());
        } catch (Exception e) {
            log.error("Error al enviar notificación de cierre de resumen para tarjeta ID: {}", tarjeta.getId(), e);
            // No propagamos la excepción para no afectar el cierre del resumen que ya fue guardado exitosamente
        }
        
        return true;
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
