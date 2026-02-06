package com.campito.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación del sistema para un usuario.
 * 
 * Las notificaciones se generan automáticamente por eventos del sistema
 * (cierre de tarjetas, invitaciones, etc.) y se almacenan para que el
 * usuario pueda consultarlas posteriormente.
 */
@Entity
@Table(name = "notificaciones")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoNotificacion tipo;
    
    @Column(nullable = false, length = 200)
    private String mensaje;
    
    @Column(nullable = false)
    private Boolean leida = false;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column
    private LocalDateTime fechaLeida;
}
