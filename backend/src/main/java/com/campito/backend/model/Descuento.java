package com.campito.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "descuentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dia", nullable = false, length = 10)
    private String dia;

    @Column(name = "localidad", length = 100)
    private String localidad;

    @Column(name = "banco", nullable = false, length = 50)
    private String banco;

    @Column(name = "modo", nullable = false)
    private Boolean modo;

    @Column(name = "porcentaje", nullable = false, length = 4)
    private String porcentaje;

    @Column(name = "comercio", nullable = false, length = 50)
    private String comercio;

    @Column(name = "modo_pago", nullable = false, length = 20)
    private String modoPago;

    @Column(name = "tope_reintegro", length = 13)
    private String topeReintegro;

    @Column(name = "es_semanal", nullable = false)
    private Boolean esSemanal;

    @Column(name = "comentario", length = 100)
    private String comentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espacio_trabajo_id", nullable = false)
    private EspacioTrabajo espacioTrabajo;
}
