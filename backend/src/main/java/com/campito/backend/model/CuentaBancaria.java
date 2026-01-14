package com.campito.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "cuentas_bancarias")
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

    @Column(name = "saldo_actual", nullable = false)
    private Float saldoActual;
    
    @ManyToOne
    @JoinColumn(name = "id_espacio_trabajo", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    public void actualizarSaldoNuevaTransaccion(Float monto, TipoTransaccion tipo) {
        if (tipo.equals(TipoTransaccion.INGRESO)) {
            this.saldoActual += monto;
        } else {
            this.saldoActual -= monto;
        }
    }

    public void actualizarSaldoEliminarTransaccion(Float monto, TipoTransaccion tipo) {
        if (tipo.equals(TipoTransaccion.INGRESO)) {
            this.saldoActual -= monto;
        } else {
            this.saldoActual += monto;
        }
    }
}