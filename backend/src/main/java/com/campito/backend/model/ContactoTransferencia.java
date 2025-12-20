package com.campito.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contactos_transferencia")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class ContactoTransferencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "id_espacio_trabajo", nullable = false)
    private EspacioTrabajo espacioTrabajo;

    public ContactoTransferencia(String nombre) {
        this.nombre = nombre;
    }

}
