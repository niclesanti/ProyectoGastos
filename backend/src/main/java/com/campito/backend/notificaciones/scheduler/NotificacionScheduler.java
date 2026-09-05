package com.campito.backend.notificaciones.scheduler;

import lombok.extern.slf4j.Slf4j;
import com.campito.backend.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler para tareas de mantenimiento de notificaciones.
 * 
 * Ejecuta limpieza automática de notificaciones antiguas para evitar
 * acumulación de datos obsoletos en la base de datos.
 * 
 * Estrategia de limpieza:
 * - Notificaciones LEÍDAS: Eliminadas diariamente si tienen > 3 días
 * - Notificaciones NO LEÍDAS: Eliminadas mensualmente si tienen > 15 días
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacionScheduler {

    private final NotificacionService notificacionService;

    /**
     * Limpia notificaciones LEÍDAS antiguas (>3 días) todos los días a las 3:00 AM.
     * 
     * Esto evita acumulación de notificaciones que el usuario ya vio y no necesita
     * más.
     * Se ejecuta a las 3:00 AM (hora de menor actividad del sistema).
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void limpiarNotificacionesLeidasAntiguas() {
        log.info("Ejecutando limpieza de notificaciones leídas antiguas");
        try {
            notificacionService.limpiarNotificacionesLeidasAntiguas();
        } catch (Exception e) {
            log.error("Error al limpiar notificaciones leídas antiguas: {}", e.getMessage(), e);
        }
    }

    /**
     * Limpia notificaciones NO LEÍDAS muy antiguas (>15 días) el primer día de cada
     * mes a las 4:00 AM.
     * 
     * Asume que si después de 15 días no fueron leídas, ya no son relevantes.
     * Se ejecuta mensualmente para no ser tan agresivo con notificaciones no
     * leídas.
     */
    @Scheduled(cron = "0 0 4 1 * *", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void limpiarNotificacionesNoLeidasAntiguas() {
        log.info("Ejecutando limpieza de notificaciones no leídas antiguas");
        try {
            notificacionService.limpiarNotificacionesNoLeidasAntiguas();
        } catch (Exception e) {
            log.error("Error al limpiar notificaciones no leídas antiguas: {}", e.getMessage(), e);
        }
    }
}
