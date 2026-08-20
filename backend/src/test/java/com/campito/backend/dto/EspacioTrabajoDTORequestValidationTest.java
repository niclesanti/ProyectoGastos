package com.campito.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EspacioTrabajoDTORequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void nombreConAcentos_deberiaSerValido() {
        EspacioTrabajoDTORequest dto = new EspacioTrabajoDTORequest("Área Ñuñez", UUID.randomUUID());
        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void nombreConCaracterNoPermitido_deberiaViolarElCampoNombre() {
        EspacioTrabajoDTORequest dto = new EspacioTrabajoDTORequest("Área!", UUID.randomUUID());
        Set<ConstraintViolation<EspacioTrabajoDTORequest>> violaciones = validator.validate(dto);
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
    }

    @Test
    void emailAcentuado_deberiaViolarElCampoEmail() {
        CompartirRequest dto = new CompartirRequest("café@test.com");
        Set<ConstraintViolation<CompartirRequest>> violaciones = validator.validate(dto);
        assertTrue(violaciones.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}