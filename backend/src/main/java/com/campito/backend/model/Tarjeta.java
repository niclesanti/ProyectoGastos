package com.campito.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tarjetas")
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Almacenaremos los ultimos 4 dígitos de la tarjeta para mayor seguridad
    @Column(name = "numero_tarjeta", nullable = false, unique = true)
    private String numeroTarjeta;

    @Column(name = "nombre_titular", nullable = false)
    private String nombreTitular;

    @Column(name = "fecha_expiracion")
    private String fechaExpiracion;

    @Column(name = "entidad_financiera", nullable = false)
    private String entidadFinanciera;

    @Column(name = "red_de_pago", nullable = false)
    private String redDePago;

}
