package com.campito.backend.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "espacios_trabajo")
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class EspacioTrabajo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "saldo", nullable = false)
    private Float saldo;// Asignar saldo inicial a 0.0f

    @ManyToOne
    @JoinColumn(name = "usuario_admin_id", nullable = false)
    private Usuario usuarioAdmin;

    @ManyToMany
    @JoinTable(name = "espacios_trabajo_usuarios", joinColumns = @JoinColumn(name = "espacio_trabajo_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private List<Usuario> usuariosParticipantes;

}
