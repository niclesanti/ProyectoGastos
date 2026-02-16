package com.campito.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "espacios_trabajo")
@EntityListeners(AuditingEntityListener.class)
@Data // Genera equals, hashCode, toString y getters/setters para todos los campos
@NoArgsConstructor  // Genera constructor sin argumentos (requerido por JPA)
@AllArgsConstructor  // Genera constructor con todos los argumentos
@Builder // Implementa el patrón Builder para construcción fluida de objetos
public class EspacioTrabajo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "saldo", nullable = false, columnDefinition = "NUMERIC(15,2)")
    private BigDecimal saldo;// Asignar saldo inicial en BigDecimal.ZERO

    @ManyToOne
    @JoinColumn(name = "usuario_admin_id", nullable = false)
    private Usuario usuarioAdmin;

    @ManyToMany
    @JoinTable(name = "espacios_trabajo_usuarios", joinColumns = @JoinColumn(name = "espacio_trabajo_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private List<Usuario> usuariosParticipantes;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDateTime fechaModificacion;

    public void actualizarSaldoNuevaTransaccion(BigDecimal monto, TipoTransaccion tipo) {
        if (tipo.equals(TipoTransaccion.INGRESO)) {
            this.saldo = this.saldo.add(monto);
        } else {
            this.saldo = this.saldo.subtract(monto);
        }
    }

    public void actualizarSaldoEliminarTransaccion(BigDecimal monto, TipoTransaccion tipo) {
        if (tipo.equals(TipoTransaccion.INGRESO)) {
            this.saldo = this.saldo.subtract(monto);
        } else {
            this.saldo = this.saldo.add(monto);
        }
    }
}
