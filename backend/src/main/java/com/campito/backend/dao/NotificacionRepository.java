package com.campito.backend.dao;

import com.campito.backend.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    
    /**
     * Obtiene todas las notificaciones de un usuario ordenadas por fecha de creación descendente.
     * 
     * @param idUsuario ID del usuario
     * @return Lista de notificaciones ordenadas
     */
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(UUID idUsuario);
    
    /**
     * Obtiene las últimas 50 notificaciones de un usuario ordenadas por fecha de creación descendente.
     * Limita la cantidad de resultados para optimizar el rendimiento.
     * 
     * @param idUsuario ID del usuario
     * @return Lista de las últimas 50 notificaciones
     */
    List<Notificacion> findTop50ByUsuarioIdOrderByFechaCreacionDesc(UUID idUsuario);
    
    /**
     * Cuenta las notificaciones no leídas de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Cantidad de notificaciones no leídas
     */
    Long countByUsuarioIdAndLeidaFalse(UUID idUsuario);
    
    /**
     * Marca todas las notificaciones no leídas de un usuario como leídas.
     * 
     * @param idUsuario ID del usuario
     * @param fecha Fecha en la que se marcaron como leídas
     * @return Cantidad de notificaciones actualizadas
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true, n.fechaLeida = :fecha WHERE n.usuario.id = :idUsuario AND n.leida = false")
    int marcarTodasComoLeidas(UUID idUsuario, LocalDateTime fecha);
    
    /**
     * Elimina notificaciones leídas que superen el límite de antigüedad.
     * Se ejecuta diariamente para evitar acumulación de datos obsoletos.
     * 
     * @param fechaLimite Fecha límite (notificaciones anteriores serán eliminadas)
     * @return Cantidad de notificaciones eliminadas
     */
    @Modifying
    @Query("DELETE FROM Notificacion n WHERE n.leida = true AND n.fechaLeida < :fechaLimite")
    int eliminarNotificacionesLeidasAntiguas(LocalDateTime fechaLimite);
    
    /**
     * Elimina notificaciones no leídas muy antiguas.
     * Se ejecuta mensualmente asumiendo que si no fueron leídas en 30 días, ya no son relevantes.
     * 
     * @param fechaLimite Fecha límite (notificaciones anteriores serán eliminadas)
     * @return Cantidad de notificaciones eliminadas
     */
    @Modifying
    @Query("DELETE FROM Notificacion n WHERE n.leida = false AND n.fechaCreacion < :fechaLimite")
    int eliminarNotificacionesNoLeidasAntiguas(LocalDateTime fechaLimite);
}
