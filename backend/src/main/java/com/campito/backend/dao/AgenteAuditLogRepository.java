package com.campito.backend.dao;

import com.campito.backend.model.AgenteAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio para gestionar el audit log del agente IA.
 */
@Repository
public interface AgenteAuditLogRepository extends JpaRepository<AgenteAuditLog, Long> {
    
    /**
     * Obtiene las últimas 50 interacciones de un usuario
     */
    List<AgenteAuditLog> findTop50ByUserIdOrderByTimestampDesc(UUID userId);
    
    /**
     * Busca auditorías de un workspace en un rango de fechas
     */
    List<AgenteAuditLog> findByWorkspaceIdAndTimestampBetween(
        UUID workspaceId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * Cuenta requests de un usuario después de una fecha (para analytics)
     */
    Long countByUserIdAndTimestampAfter(UUID userId, LocalDateTime after);
}
