package com.campito.backend.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuotas_credito")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class CuotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuota", nullable = false)
    private int numeroCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "monto_cuota", nullable = false)
    private Float montoCuota;

    @Column(name = "pagada", nullable = false)
    private boolean pagada;

    @ManyToOne
    @JoinColumn(name = "compra_credito_id", nullable = false)
    private CompraCredito compraCredito;

    @ManyToOne
    @JoinColumn(name = "resumen_id")
    private Resumen resumenAsociado;

    public void pagarCuota() {
        this.pagada = true;
    }

}
