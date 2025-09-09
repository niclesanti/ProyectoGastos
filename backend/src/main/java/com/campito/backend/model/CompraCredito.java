package com.campito.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "compras_credito")
public class CompraCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDate fechaCompra;

    @Column(name = "monto_total", nullable = false)
    private Float montoTotal;

    @Column(name = "cantidad_cuotas", nullable = false)
    private int cantidadCuotas;

    @Column(name = "tasa_interes", nullable = false)
    private int tasaInteres;

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
    @JoinColumn(name = "comercio_id")
    private ContactoTransferencia comercio;

    @ManyToOne
    @JoinColumn(name = "tarjeta_id")
    private Tarjeta tarjeta;

    public CompraCredito() {
    }

    public CompraCredito(
        LocalDate fechaCompra, 
        Float montoTotal, 
        int cantidadCuotas, 
        int tasaInteres, 
        String descripcion, 
        String nombreCompletoAuditoria, 
        LocalDateTime fechaCreacion, 
        EspacioTrabajo espacioTrabajo, 
        MotivoTransaccion motivo, 
        ContactoTransferencia comercio) {
        this.fechaCompra = fechaCompra;
        this.montoTotal = montoTotal;
        this.cantidadCuotas = cantidadCuotas;
        this.tasaInteres = tasaInteres;
        this.descripcion = descripcion;
        this.nombreCompletoAuditoria = nombreCompletoAuditoria;
        this.fechaCreacion = fechaCreacion;
        this.espacioTrabajo = espacioTrabajo;
        this.motivo = motivo;
        this.comercio = comercio;
    }
}
