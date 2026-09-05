package com.campito.backend.common.test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.campito.backend.usuarios.domain.entity.EspacioTrabajo;
import com.campito.backend.usuarios.domain.entity.Usuario;
import com.campito.backend.usuarios.domain.entity.ProveedorAutenticacion;
import com.campito.backend.usuarios.domain.dto.UsuarioDTOResponse;

public final class UsuariosTestDataFactory {

    private UsuariosTestDataFactory() {}

    public static Usuario crearUsuario(UUID id) {
        return Usuario.builder()
            .id(id)
            .nombre("Usuario Test")
            .email("test@test.com")
            .proveedor(ProveedorAutenticacion.GOOGLE)
            .idProveedor("google-123")
            .rol("USER")
            .activo(true)
            .fechaRegistro(LocalDateTime.now())
            .build();
    }

    public static Usuario crearUsuarioAdmin() {
        return crearUsuario(TestIds.USUARIO_ADMIN_ID);
    }

    public static Usuario crearUsuarioParticipante() {
        Usuario u = crearUsuario(TestIds.USUARIO_PARTICIPANTE_ID);
        u.setNombre("Participante Test");
        u.setEmail("participante@test.com");
        return u;
    }

    public static EspacioTrabajo crearEspacioTrabajo(UUID id) {
        Usuario admin = crearUsuarioAdmin();
        return EspacioTrabajo.builder()
            .id(id)
            .nombre("Espacio Test")
            .saldo(BigDecimal.ZERO)
            .usuarioAdmin(admin)
            .usuariosParticipantes(List.of(admin))
            .fechaCreacion(LocalDateTime.now())
            .fechaModificacion(LocalDateTime.now())
            .build();
    }

    public static EspacioTrabajo crearEspacioTrabajo() {
        return crearEspacioTrabajo(TestIds.ESPACIO_TRABAJO_ID);
    }

    public static UsuarioDTOResponse crearUsuarioDTOResponse(UUID id) {
        return new UsuarioDTOResponse(id, "Usuario Test", "test@test.com", null);
    }
}
