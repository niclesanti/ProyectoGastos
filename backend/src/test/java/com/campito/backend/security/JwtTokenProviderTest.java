package com.campito.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "test_jwt_secret_para_unit_tests_minimo_256_bits_1234567890123456789012";
    private static final long EXPIRATION_MS = 604800000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", EXPIRATION_MS);
        jwtTokenProvider.init();
    }

    @Test
    void generateToken_retornaTokenValido() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@test.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUserIdFromToken_extraeUuidCorrectamente() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@test.com");

        UUID extractedId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(userId, extractedId);
    }

    @Test
    void getEmailFromToken_extraeEmailCorrectamente() {
        UUID userId = UUID.randomUUID();
        String email = "usuario@test.com";
        String token = jwtTokenProvider.generateToken(userId, email);

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);
        assertEquals(email, extractedEmail);
    }

    @Test
    void validateToken_tokenValido_retornaTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@test.com");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_tokenVacio_retornaFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void validateToken_tokenNulo_retornaFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_tokenMalformado_retornaFalse() {
        assertFalse(jwtTokenProvider.validateToken("token.invalido.aqui"));
    }

    @Test
    void validateToken_tokenConFirmaInvalida_retornaFalse() {
        // Crear un provider con una clave diferente
        JwtTokenProvider otroProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(otroProvider, "jwtSecret", "otra_clave_completamente_diferente_para_tests_256_bits_!!!_12345");
        ReflectionTestUtils.setField(otroProvider, "jwtExpirationMs", EXPIRATION_MS);
        otroProvider.init();

        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateToken(userId, "test@test.com");

        assertFalse(otroProvider.validateToken(token));
    }

    @Test
    void validateToken_tokenExpirado_retornaFalse() {
        JwtTokenProvider expiradoProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiradoProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(expiradoProvider, "jwtExpirationMs", -1L); // Expiración en el pasado
        expiradoProvider.init();

        UUID userId = UUID.randomUUID();
        String token = expiradoProvider.generateToken(userId, "test@test.com");

        assertFalse(expiradoProvider.validateToken(token));
    }
}
