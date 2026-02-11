package com.campito.backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.SolicitudPendienteEspacioTrabajo;

@Repository
public interface SolicitudPendienteEspacioTrabajoRepository extends JpaRepository<SolicitudPendienteEspacioTrabajo, Long> {

    List<SolicitudPendienteEspacioTrabajo> findByUsuarioInvitado_Id(UUID idUsuario);
    
    /**
     * Verifica si ya existe una solicitud pendiente para un usuario y espacio específicos.
     * 
     * @param idEspacioTrabajo ID del espacio de trabajo
     * @param idUsuarioInvitado ID del usuario invitado
     * @return true si ya existe una solicitud pendiente, false en caso contrario
     */
    boolean existsByEspacioTrabajo_IdAndUsuarioInvitado_Id(UUID idEspacioTrabajo, UUID idUsuarioInvitado);
}
