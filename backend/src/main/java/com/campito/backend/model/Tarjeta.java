package com.campito.backend.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "tarjetas")
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Almacenaremos los ultimos 4 dígitos de la tarjeta para mayor seguridad
    @Column(name = "numero_tarjeta", nullable = false, unique = true, length = 4)
    private String numeroTarjeta;

    @Column(name = "entidad_financiera", nullable = false, length = 50)
    private String entidadFinanciera;

    @Column(name = "red_de_pago", nullable = false, length = 50)
    private String redDePago;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @Column(name = "fecha_vencimiento_pago", nullable = false)
    private LocalDate fechaVencimientoPago;

    public Tarjeta() {
    }

    public Tarjeta(
        String numeroTarjeta,  
        String entidadFinanciera, 
        String redDePago, 
        LocalDate fechaCierre, 
        LocalDate fechaVencimientoPago) {
        this.numeroTarjeta = numeroTarjeta;
        this.entidadFinanciera = entidadFinanciera;
        this.redDePago = redDePago;
        this.fechaCierre = fechaCierre;
        this.fechaVencimientoPago = fechaVencimientoPago;
    }

}
