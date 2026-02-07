package com.campito.backend.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.campito.backend.model.SolicitudPendienteEspacioTrabajo;

@Repository
public interface SolicitudPendienteEspacioTrabajoRepository extends JpaRepository<SolicitudPendienteEspacioTrabajo, Long> {

    List<SolicitudPendienteEspacioTrabajo> findByUsuarioInvitado_Id(UUID idUsuario);
}
