package com.campito.backend.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.dao.TarjetaRepository;
import com.campito.backend.event.NotificacionEvent;
import com.campito.backend.model.Tarjeta;
import com.campito.backend.model.TipoNotificacion;

import lombok.RequiredArgsConstructor;

/**
 * Scheduler que ejecuta recordatorios de cierre de tarjetas.
 */
@Component
@RequiredArgsConstructor
public class TarjetaCierreScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TarjetaCierreScheduler.class);

    private final TarjetaRepository tarjetaRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Ejecuta el recordatorio de cierres todos los días a las 00:00hs.
     * Busca las tarjetas cuyo día de cierre sea dentro de 5 días.
     * 
     * Cron: segundo minuto hora día mes día_semana
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void recordarProximosCierres() {

        LocalDate fechaObjetivo = LocalDate.now().plusDays(5);
        int diaACerrar = fechaObjetivo.getDayOfMonth();

        logger.info("Iniciando envío de recordatorios para tarjetas que cierran el día: {}", diaACerrar);

        List<Tarjeta> tarjetasPorCerrar = new ArrayList<>();
        tarjetasPorCerrar.addAll(tarjetaRepository.findByDiaCierre(diaACerrar));

        // Manejar caso borde de fin de mes
        int longitudMes = fechaObjetivo.lengthOfMonth();
        if (diaACerrar == longitudMes) {
            // Si el día objetivo es el último del mes, incluir todos los días hasta el 31
            for (int d = diaACerrar + 1; d <= 31; d++) {
                tarjetasPorCerrar.addAll(tarjetaRepository.findByDiaCierre(d));
            }
        }

        logger.info("Encontradas {} tarjetas para enviar recordatorio", tarjetasPorCerrar.size());

        int recordatoriosEnviados = 0;
        int errores = 0;

        for (Tarjeta tarjeta : tarjetasPorCerrar) {
            try {
                enviarRecordatorio(tarjeta, fechaObjetivo);
                recordatoriosEnviados++;
            } catch (Exception e) {
                logger.error("Error al enviar recordatorio de cierre para tarjeta ID: {}", tarjeta.getId(), e);
                errores++;
            }
        }

        logger.info("Envío de recordatorios finalizado - Enviados: {} - Errores: {}", recordatoriosEnviados, errores);
    }

    private void enviarRecordatorio(Tarjeta tarjeta, LocalDate fechaCierre) {
        UUID idUsuarioAdmin = tarjeta.getEspacioTrabajo().getUsuarioAdmin().getId();
        String numeroTarjeta = tarjeta.getNumeroTarjeta();
        String redDePago = tarjeta.getRedDePago();
        String fechaCierreStr = fechaCierre.format(DateTimeFormatter.ofPattern("dd/MM"));

        String mensaje = String.format(
                "Tu tarjeta terminada en %s está programada para cerrar el %s. Verifica si esta fecha es correcta o actualízala.",
                numeroTarjeta, fechaCierreStr);

        eventPublisher.publishEvent(new NotificacionEvent(
                this,
                idUsuarioAdmin,
                TipoNotificacion.RECORDATORIO_PROXIMO_CIERRE,
                mensaje));

        logger.info("Recordatorio enviado al usuario {} por cierre próximo de tarjeta {}",
                idUsuarioAdmin, tarjeta.getId());
    }
}
