package com.campito.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.MotivoTransaccion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MotivoTransaccionRepository extends JpaRepository<MotivoTransaccion, Long> {

    List<MotivoTransaccion> findByEspacioTrabajo_Id(UUID idEspacioTrabajo);
    
    // Método para obtener motivos ordenados por última modificación (más recientes primero)
    List<MotivoTransaccion> findByEspacioTrabajo_IdOrderByFechaModificacionDesc(UUID idEspacioTrabajo);
    
    Optional<MotivoTransaccion> findFirstByMotivoAndEspacioTrabajo_Id(String motivo, UUID idEspacioTrabajo);
}
