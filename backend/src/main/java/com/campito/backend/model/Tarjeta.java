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
    private Integer diaCierre;

    @Column(name = "fecha_vencimiento_pago", nullable = false)
    private Integer diaVencimientoPago;

    public Tarjeta() {
    }

    public Tarjeta(
        String numeroTarjeta,  
        String entidadFinanciera, 
        String redDePago, 
        Integer diaCierre, 
        Integer diaVencimientoPago) {
        this.numeroTarjeta = numeroTarjeta;
        this.entidadFinanciera = entidadFinanciera;
        this.redDePago = redDePago;
        this.diaCierre = diaCierre;
        this.diaVencimientoPago = diaVencimientoPago;
    }

    public Long getId() {
        return id;
    }

    public Integer getDiaCierre() {
        return diaCierre;
    }

    public Integer getDiaVencimientoPago() {
        return diaVencimientoPago;
    }
}
