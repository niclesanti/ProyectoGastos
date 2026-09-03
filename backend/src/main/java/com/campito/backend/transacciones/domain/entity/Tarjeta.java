package com.campito.backend.transacciones.domain.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tarjetas", schema = "transacciones")
@EntityListeners(AuditingEntityListener.class)
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Almacenaremos los ultimos 4 dígitos de la tarjeta para mayor seguridad
    @Column(name = "numero_tarjeta", nullable = false, length = 4)
    private String numeroTarjeta;

    @Column(name = "entidad_financiera", nullable = false, length = 50)
    private String entidadFinanciera;

    @Column(name = "red_de_pago", nullable = false, length = 50)
    private String redDePago;

    @Column(name = "dia_cierre", nullable = false)
    private Integer diaCierre;

    @Column(name = "dia_vencimiento_pago", nullable = false)
    private Integer diaVencimientoPago;

    @Column(name = "espacio_trabajo_id", nullable = false)
    private UUID idEspacioTrabajo;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

}
