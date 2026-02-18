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

import java.math.BigDecimal;

/*
* Entidad creada para optimizar el cálculo de gastos e ingresos mensuales
* de cada espacio de trabajo, evitando la necesidad de recorrer todas las transacciones
* cada vez que se requiere esta información.
*/
@Entity
@Table(name = "gastos_ingresos_mensuales")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class GastosIngresosMensuales {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "gastos", nullable = false, columnDefinition = "NUMERIC(15,2)")
    private BigDecimal gastos;

    @Column(name = "ingresos", nullable = false, columnDefinition = "NUMERIC(15,2)")
    private BigDecimal ingresos;

    @ManyToOne
    @JoinColumn(name = "espacio_trabajo_id", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    public void actualizarGastos(BigDecimal nuevoGasto) {
        this.gastos = this.gastos.add(nuevoGasto);
    }

    public void actualizarIngresos(BigDecimal nuevoIngreso) {
        this.ingresos = this.ingresos.add(nuevoIngreso);
    }

    public void eliminarGastos(BigDecimal nuevoGasto) {
        this.gastos = this.gastos.subtract(nuevoGasto);
    }

    public void eliminarIngresos(BigDecimal nuevoIngreso) {
        this.ingresos = this.ingresos.subtract(nuevoIngreso);
    }
}
