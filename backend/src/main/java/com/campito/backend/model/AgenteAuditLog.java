package com.campito.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad para auditar todas las interacciones con el agente IA.
 * Almacena conversaciones, funciones llamadas y consumo de tokens
 * para compliance y análisis de uso.
 */
@Entity
@Table(name = "agente_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgenteAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private UUID userId;
    
    @Column(nullable = false)
    private UUID workspaceId;
    
    @Column(nullable = false, length = 500)
    private String userMessage;
    
    @Column(columnDefinition = "TEXT")
    private String agentResponse;
    
    @Column(length = 500)
    private String functionsCalled;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private Integer tokensUsed;
    
    @Column(nullable = false)
    private Boolean success;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
