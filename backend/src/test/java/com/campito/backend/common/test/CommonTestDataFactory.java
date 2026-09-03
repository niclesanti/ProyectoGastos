package com.campito.backend.common.test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.campito.backend.usuarios.domain.entity.Usuario;
import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.usuarios.domain.dto.*;

public final class CommonTestDataFactory {

    private CommonTestDataFactory() {}

    public static UsuarioDTOResponse crearUsuarioResponse(UUID id) {
        return new UsuarioDTOResponse(id, "Usuario Test", "test@test.com", null);
    }

    public static EspacioTrabajoDTOResponse crearEspacioTrabajoResponse(UUID id) {
        return new EspacioTrabajoDTOResponse(id, "Espacio Test", BigDecimal.ZERO, TestIds.USUARIO_ADMIN_ID);
    }

    public static EspacioTrabajoDTORequest crearEspacioTrabajoRequest(UUID idUsuarioAdmin) {
        return new EspacioTrabajoDTORequest("Espacio Test", idUsuarioAdmin);
    }

    public static SolicitudPendienteEspacioTrabajoDTOResponse crearSolicitudResponse(Long id) {
        return new SolicitudPendienteEspacioTrabajoDTOResponse(
            id, "Espacio Test", "Admin Test", null, LocalDateTime.now()
        );
    }
}
