package com.campito.backend.dao;


import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.EspacioTrabajo;

@Repository
public interface EspacioTrabajoRepository extends JpaRepository<EspacioTrabajo, Long> {
    
    List<EspacioTrabajo> findByUsuariosParticipantes_Id(Long idUsuario);
    
    Optional<EspacioTrabajo> findFirstByNombreAndUsuarioAdmin_Id(String nombre, Long idUsuarioAdmin);
}
