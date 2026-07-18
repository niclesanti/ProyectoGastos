package com.campito.backend.security;

import lombok.extern.slf4j.Slf4j;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Proveedor de tokens JWT para autenticación sin estado.
 * Genera y valida tokens JWT para usuarios autenticados.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT para un usuario identificado por su UUID
     * 
     * @param userId UUID del usuario
     * @param email Email del usuario
     * @return Token JWT firmado
     */
    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extrae el ID de usuario del token JWT
     * 
     * @param token Token JWT
     * @return UUID del usuario
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extrae el email del usuario del token JWT
     * 
     * @param token Token JWT
     * @return Email del usuario
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    /**
     * Valida un token JWT
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            log.error("Token JWT inválido: firma incorrecta");
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT expirado");
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT no soportado");
        } catch (IllegalArgumentException ex) {
            log.error("Token JWT vacío");
        } catch (Exception ex) {
            log.error("Error al validar token JWT", ex);
        }
        return false;
    }

    /**
     * Parsea y verifica un token JWT
     * 
     * @param token Token JWT
     * @return Claims del token
     * @throws JwtException si el token es inválido
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
