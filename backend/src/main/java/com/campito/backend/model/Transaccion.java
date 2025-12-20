package com.campito.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoTransaccion tipo;

    @Column(name = "monto", nullable = false)
    private Float monto;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Column(name = "nombre_completo_auditoria", nullable = false, length = 100)
    private String nombreCompletoAuditoria;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne
    @JoinColumn(name = "espacio_trabajo_id", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    @ManyToOne
    @JoinColumn(name = "motivo_transaccion_id", nullable = false)
    private MotivoTransaccion motivo;

    @ManyToOne
    @JoinColumn(name = "contacto_transferencia_id")
    private ContactoTransferencia contacto;

    @ManyToOne
    @JoinColumn(name = "cuenta_bancaria_id")
    private CuentaBancaria cuentaBancaria;

}
