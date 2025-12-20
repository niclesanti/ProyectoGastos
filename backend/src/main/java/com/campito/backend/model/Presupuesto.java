package com.campito.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "presupuestos")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class Presupuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "monto", nullable = false)
    private Float monto;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDateTime periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDateTime periodoFin;

    @Column(name = "umbral_alerta", nullable = false)
    private Float umbralAlerta;

    @ManyToOne
    @JoinColumn(name = "espacio_trabajo_id", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    @ManyToOne
    @JoinColumn(name = "motivo_id")
    private MotivoTransaccion motivo;

}
