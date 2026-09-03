package com.campito.backend.transacciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campito.backend.transacciones.domain.entity.MotivoTransaccion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MotivoTransaccionRepository extends JpaRepository<MotivoTransaccion, Long> {

    List<MotivoTransaccion> findByIdEspacioTrabajo(UUID idEspacioTrabajo);
    
    // Método para obtener motivos ordenados por última modificación (más recientes primero)
    List<MotivoTransaccion> findByIdEspacioTrabajoOrderByFechaModificacionDesc(UUID idEspacioTrabajo);
    
    Optional<MotivoTransaccion> findFirstByMotivoAndIdEspacioTrabajo(String motivo, UUID idEspacioTrabajo);
}
