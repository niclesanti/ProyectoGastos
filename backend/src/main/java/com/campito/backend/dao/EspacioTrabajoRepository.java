package com.campito.backend.dao;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.EspacioTrabajo;

@Repository
public interface EspacioTrabajoRepository extends JpaRepository<EspacioTrabajo, UUID> {
    
    List<EspacioTrabajo> findByUsuariosParticipantes_Id(UUID idUsuario);
    
    // Método para obtener espacios de trabajo ordenados por última modificación (más recientes primero)
    List<EspacioTrabajo> findByUsuariosParticipantes_IdOrderByFechaModificacionDesc(UUID idUsuario);
    
    Optional<EspacioTrabajo> findFirstByNombreAndUsuarioAdmin_Id(String nombre, UUID idUsuarioAdmin);
    
    // Método para verificar si un usuario tiene acceso a un espacio de trabajo
    boolean existsByIdAndUsuariosParticipantes_Id(UUID espacioId, UUID userId);
}
