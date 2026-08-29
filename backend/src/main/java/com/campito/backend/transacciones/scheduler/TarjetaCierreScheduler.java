package com.campito.backend.transacciones.scheduler;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Dependencias a revisar para reducir acoplamiento y mejorar la modularidad
import com.campito.backend.common.event.NotificacionEvent;
import com.campito.backend.common.event.TipoNotificacion;
import com.campito.backend.usuarios.api.EspacioTrabajoApi;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.campito.backend.transacciones.repository.TarjetaRepository;
import com.campito.backend.transacciones.domain.entity.Tarjeta;


import lombok.RequiredArgsConstructor;

/**
 * Scheduler que ejecuta recordatorios de cierre de tarjetas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TarjetaCierreScheduler {

    private final TarjetaRepository tarjetaRepository;
    private final EspacioTrabajoApi espacioTrabajoApi;
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

        log.info("Iniciando envío de recordatorios para tarjetas que cierran el día: {}", diaACerrar);

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

        log.info("Encontradas {} tarjetas para enviar recordatorio", tarjetasPorCerrar.size());

        int recordatoriosEnviados = 0;
        int errores = 0;

        for (Tarjeta tarjeta : tarjetasPorCerrar) {
            try {
                enviarRecordatorio(tarjeta, fechaObjetivo);
                recordatoriosEnviados++;
            } catch (Exception e) {
                log.error("Error al enviar recordatorio de cierre para tarjeta ID: {}", tarjeta.getId(), e);
                errores++;
            }
        }

        log.info("Envío de recordatorios finalizado - Enviados: {} - Errores: {}", recordatoriosEnviados, errores);
    }

    private void enviarRecordatorio(Tarjeta tarjeta, LocalDate fechaCierre) {
        UUID idUsuarioAdmin = espacioTrabajoApi.obtenerIdUsuarioAdmin(tarjeta.getIdEspacioTrabajo());
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

        log.info("Recordatorio enviado al usuario {} por cierre próximo de tarjeta {}",
                idUsuarioAdmin, tarjeta.getId());
    }
}
