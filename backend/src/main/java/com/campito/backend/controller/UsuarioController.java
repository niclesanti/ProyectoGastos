package com.campito.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.UsuarioDTO;
import com.campito.backend.model.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/usuario")
@Tag(name = "Usuario", description = "Operaciones para la gestión de usuarios")
public class UsuarioController {

    @Operation(summary = "Obtener datos del usuario autenticado", description = "Devuelve el id, nombre y email del usuario que ha iniciado sesión.")
    @ApiResponse(responseCode = "200", description = "Datos del usuario")
    @ApiResponse(responseCode = "401", description = "No autorizado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> getUsuarioAutenticado(@AuthenticationPrincipal CustomOAuth2User principal) {
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UsuarioDTO usuarioAut = UsuarioDTO.fromUsuario(principal.getUsuario());

        return new ResponseEntity<>(usuarioAut, HttpStatus.OK);
    }
}
