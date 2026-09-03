package com.campito.backend.usuarios.domain.dto;

import java.time.LocalDateTime;

public record SolicitudPendienteEspacioTrabajoDTOResponse(
    Long id,
    String espacioTrabajoNombre,
    String usuarioAdminNombre,
    String fotoPerfilUsuarioAdmin,
    LocalDateTime fechaCreacion
) {

}
