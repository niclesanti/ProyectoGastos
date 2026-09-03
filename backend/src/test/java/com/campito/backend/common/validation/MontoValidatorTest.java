package com.campito.backend.common.validation;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class MontoValidatorTest {

    private MontoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MontoValidator();
    }

    @Test
    void isValid_null_retornaTrue() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void isValid_montoValido_retornaTrue() {
        assertTrue(validator.isValid(new BigDecimal("123.45"), null));
    }

    @Test
    void isValid_enterosGrandes_retornaTrue() {
        assertTrue(validator.isValid(new BigDecimal("9999999999999.99"), null));
    }

    @Test
    void isValid_masDe13Enteros_retornaFalse() {
        assertFalse(validator.isValid(new BigDecimal("10000000000000.00"), null));
    }

    @Test
    void isValid_masDe2Decimales_retornaFalse() {
        assertFalse(validator.isValid(new BigDecimal("123.456"), null));
    }

    @Test
    void isValid_cero_retornaTrue() {
        assertTrue(validator.isValid(BigDecimal.ZERO, null));
    }

    @Test
    void isValid_montoNegativo_retornaTrue() {
        assertTrue(validator.isValid(new BigDecimal("-100.50"), null));
    }
}
