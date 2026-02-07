package com.campito.backend.dto;

import java.time.LocalDateTime;

public record SolicitudPendienteEspacioTrabajoDTOResponse(
    Long id,
    String espacioTrabajoNombre,
    String usuarioAdminNombre,
    String fotoPerfilUsuarioAdmin,
    LocalDateTime fechaCreacion
) {

}
