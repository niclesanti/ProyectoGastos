package com.campito.backend.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DescripcionValidatorTest {

    private DescripcionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DescripcionValidator();
    }

    @Test
    void isValid_deberiaAceptarNull_yVacio() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Descripción con café y ñandú"
    })
    void isValid_deberiaAceptarAcentosValidos(String descripcion) {
        assertTrue(validator.isValid(descripcion, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Descripción!"
    })
    void isValid_deberiaRechazarSimbolosNoPermitidos(String descripcion) {
        assertFalse(validator.isValid(descripcion, null));
    }
}
