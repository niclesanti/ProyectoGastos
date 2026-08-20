package com.campito.backend.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NombreValidatorTest {

    private NombreValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NombreValidator();
    }

    @Test
    void isValid_deberiaAceptarNull_yVacio() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Cafetería Ángel Ñuñez",
        "Úrsula Ü",
        "México"
    })
    void isValid_deberiaAceptarAcentosValidos(String nombre) {
        assertTrue(validator.isValid(nombre, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Cafe, SA (2024) - Norte/Sur"
    })
    void isValid_deberiaAceptarSimbolosPermitidos(String nombre) {
        assertTrue(validator.isValid(nombre, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Café!",
        "hola@",
        "café&co"
    })
    void isValid_deberiaRechazarSimbolosNoPermitidos(String nombre) {
        assertFalse(validator.isValid(nombre, null));
    }
}