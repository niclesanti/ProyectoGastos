package com.campito.backend.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.UsuarioDTO;
import com.campito.backend.service.SecurityService;
import com.campito.backend.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuario", description = "Operaciones para la gestión de usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final SecurityService securityService;
    private final UsuarioService usuarioService;

    @Operation(summary = "Obtener datos del usuario autenticado", description = "Devuelve el id, nombre y email del usuario que ha iniciado sesión.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getUsuarioAutenticado() {
        UUID userId = securityService.getAuthenticatedUserId();
        UsuarioDTO usuarioAut = usuarioService.getUsuarioAutenticado(userId);
        return new ResponseEntity<>(usuarioAut, HttpStatus.OK);
    }
}
