package com.campito.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuentas_bancarias")
@EntityListeners(AuditingEntityListener.class)
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class CuentaBancaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "entidad_financiera", nullable = false, length = 50)
    private String entidadFinanciera;

    @Column(name = "saldo_actual", nullable = false, columnDefinition = "NUMERIC(15,2)")
    private BigDecimal saldoActual;
    
    @ManyToOne
    @JoinColumn(name = "id_espacio_trabajo", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    public void actualizarSaldoNuevaTransaccion(BigDecimal monto, TipoTransaccion tipo) {
        if (tipo.equals(TipoTransaccion.INGRESO)) {
            this.saldoActual = this.saldoActual.add(monto);
        } else {
            this.saldoActual = this.saldoActual.subtract(monto);
        }
    }

    public void actualizarSaldoEliminarTransaccion(BigDecimal monto, TipoTransaccion tipo) {
        if (tipo.equals(TipoTransaccion.INGRESO)) {
            this.saldoActual = this.saldoActual.subtract(monto);
        } else {
            this.saldoActual = this.saldoActual.add(monto);
        }
    }
}