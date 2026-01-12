package com.campito.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MontoValidator implements ConstraintValidator<ValidMonto, Float> {

    @Override
    public void initialize(ValidMonto constraintAnnotation) {
    }

    @Override
    public boolean isValid(Float monto, ConstraintValidatorContext context) {
        if (monto == null) {
            return true; // Usar @NotNull para validar nulidad
        }

        // Convertir a BigDecimal para analizar precisión
        BigDecimal bd = BigDecimal.valueOf(monto);
        String stringValue = bd.toPlainString();
        
        // Separar parte entera y decimal
        String[] parts = stringValue.split("\\.");
        
        // Validar parte entera (máximo 11 dígitos)
        if (parts[0].replace("-", "").length() > 11) {
            return false;
        }
        
        // Validar parte decimal (máximo 2 dígitos)
        if (parts.length > 1 && parts[1].length() > 2) {
            return false;
        }
        
        return true;
    }
}
