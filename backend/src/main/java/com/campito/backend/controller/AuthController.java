package com.campito.backend.controller;

import com.campito.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campito.backend.dto.UsuarioDTO;
import com.campito.backend.model.CustomOAuth2User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para gestión de autenticación")
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Verificar estado de autenticación", description = "Devuelve si el usuario está autenticado y sus datos.")
    @ApiResponse(responseCode = "200", description = "Estado de autenticación")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus(
            @AuthenticationPrincipal CustomOAuth2User principal,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser") && principal != null) {
            response.put("authenticated", true);
            response.put("user", UsuarioDTO.fromUsuario(principal.getUsuario()));
            
            // Generar y enviar token JWT actualizado
            String token = jwtTokenProvider.generateToken(
                principal.getUsuario().getId(),
                principal.getUsuario().getEmail()
            );
            response.put("token", token);
        } else {
            response.put("authenticated", false);
            response.put("user", null);
        }
        
        return ResponseEntity.ok(response);
    }
}
