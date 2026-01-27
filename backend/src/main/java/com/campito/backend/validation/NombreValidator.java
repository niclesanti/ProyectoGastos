package com.campito.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NombreValidator implements ConstraintValidator<ValidNombre, String> {

    private static final String NOMBRE_PATTERN = "^[a-zA-Z0-9,()_\\-/\\s]*$";

    @Override
    public void initialize(ValidNombre constraintAnnotation) {
    }

    @Override
    public boolean isValid(String nombre, ConstraintValidatorContext context) {
        // Null o vacío es validado por @NotBlank/@NotNull
        if (nombre == null || nombre.isEmpty()) {
            return true;
        }
        
        // Validar contra el patrón regex
        return nombre.matches(NOMBRE_PATTERN);
    }
}
