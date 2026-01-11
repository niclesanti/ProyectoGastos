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

    @Column(name = "gastos", nullable = false)
    private Float gastos;

    @Column(name = "ingresos", nullable = false)
    private Float ingresos;

    @ManyToOne
    @JoinColumn(name = "espacio_trabajo_id", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    public void actualizarGastos(Float nuevoGasto) {
        this.gastos += nuevoGasto;
    }

    public void actualizarIngresos(Float nuevoIngreso) {
        this.ingresos += nuevoIngreso;
    }

    public void eliminarGastos(Float nuevoGasto) {
        this.gastos -= nuevoGasto;
    }

    public void eliminarIngresos(Float nuevoIngreso) {
        this.ingresos -= nuevoIngreso;
    }
}
