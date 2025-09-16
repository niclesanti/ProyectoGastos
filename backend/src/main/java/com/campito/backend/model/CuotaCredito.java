package com.campito.backend.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "cuotas_credito")
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
    @JoinColumn(name = "transaccion_id")
    private Transaccion transaccionAsociada;

    public CuotaCredito() {
    }

    public CuotaCredito(int numeroCuota, LocalDate fechaVencimiento, Float montoCuota) {
        this.numeroCuota = numeroCuota;
        this.fechaVencimiento = fechaVencimiento;
        this.montoCuota = montoCuota;
        this.pagada = false;
    }

    public void pagarCuota() {
        this.pagada = true;
    }

    public void asociarTransaccion(Transaccion transaccion) {
        this.transaccionAsociada = transaccion;
    }

    public void setCompraCredito(CompraCredito compraCredito) {
        this.compraCredito = compraCredito;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public void setMontoCuota(Float montoCuota) {
        this.montoCuota = montoCuota;
    }
    
    public void setPagada(boolean pagada) {
        this.pagada = pagada;
    }

    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

}
