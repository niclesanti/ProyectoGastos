package com.campito.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notificaciones")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mensaje", nullable = false, length = 255)
    private String mensaje;
    
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}
