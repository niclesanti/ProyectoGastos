package com.campito.backend.usuarios.controller;

import com.campito.backend.usuarios.domain.dto.AuthStatusResponse;
import com.campito.backend.usuarios.mapper.UsuarioMapper;
import com.campito.backend.usuarios.domain.entity.CustomOAuth2User;
import com.campito.backend.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para gestión de autenticación")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioMapper usuarioMapper;

    @Operation(summary = "Verificar estado de autenticación",
               description = "Devuelve si el usuario está autenticado y sus datos.",
               responses = {
                   @ApiResponse(responseCode = "200", description = "Estado de autenticación"),
                   @ApiResponse(responseCode = "401", description = "No autenticado")
               })
    @GetMapping("/status")
    public ResponseEntity<AuthStatusResponse> getAuthStatus(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        if (principal != null) {
            String token = jwtTokenProvider.generateToken(
                principal.getUsuario().getId(),
                principal.getUsuario().getEmail()
            );
            AuthStatusResponse response = new AuthStatusResponse(
                true,
                usuarioMapper.toResponse(principal.getUsuario()),
                token
            );
            return ResponseEntity.ok(response);
        } else {
            AuthStatusResponse response = new AuthStatusResponse(false, null, null);
            return ResponseEntity.ok(response);
        }
    }
}
