package com.campito.backend.dashboard.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-model desnormalizado que consolida KPIs financieros del dashboard
 * (saldo y deuda total de tarjetas) por espacio de trabajo.
 *
 * Se mantiene de forma SÍNCRONA mediante eventos de los módulos productores
 * (usuarios y transacciones), evitando que el dashboard dependa de sus
 * repositorios o fachadas para estos datos.
 */
@Entity
@Table(name = "resumen_financiero", schema = "dashboard")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class ResumenFinanciero {

    @Id
    @Column(name = "espacio_trabajo_id", nullable = false, columnDefinition = "uuid")
    private UUID idEspacioTrabajo;

    @Column(name = "saldo", nullable = false, columnDefinition = "NUMERIC(15,2)")
    private BigDecimal saldo;

    @Column(name = "deuda_total", nullable = false, columnDefinition = "NUMERIC(15,2)")
    private BigDecimal deudaTotal;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    /**
     * Suma el monto recibido a la deuda total (uso interno del listener).
     */
    public void incrementarDeuda(BigDecimal monto) {
        this.deudaTotal = this.deudaTotal.add(monto);
    }

    /**
     * Resta el monto recibido a la deuda total, sin permitir valores negativos.
     */
    public void decrementarDeuda(BigDecimal monto) {
        BigDecimal nuevo = this.deudaTotal.subtract(monto);
        this.deudaTotal = nuevo.max(BigDecimal.ZERO);
    }
}
