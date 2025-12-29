package com.campito.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DescripcionValidator implements ConstraintValidator<ValidDescripcion, String> {

    private static final String DESCRIPCION_PATTERN = "^[a-zA-Z0-9,()_\\-/\\s]*$";

    @Override
    public void initialize(ValidDescripcion constraintAnnotation) {
    }

    @Override
    public boolean isValid(String descripcion, ConstraintValidatorContext context) {
        // Null o vacío es válido (campo opcional)
        if (descripcion == null || descripcion.isEmpty()) {
            return true;
        }
        
        // Validar contra el patrón regex
        return descripcion.matches(DESCRIPCION_PATTERN);
    }
}
