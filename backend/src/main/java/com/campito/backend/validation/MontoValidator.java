package com.campito.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MontoValidator implements ConstraintValidator<ValidMonto, BigDecimal> {

    @Override
    public void initialize(ValidMonto constraintAnnotation) {
    }

    @Override
    public boolean isValid(BigDecimal monto, ConstraintValidatorContext context) {
        if (monto == null) {
            return true; // Usar @NotNull para validar nulidad
        }

        // Obtener la parte entera y decimal
        BigDecimal integerPart = monto.setScale(0, BigDecimal.ROUND_DOWN);
        int integerDigits = integerPart.abs().toPlainString().replace(".", "").length();
        
        // Validar parte entera (máximo 12 dígitos para alinearse con NUMERIC(15,2))
        if (integerDigits > 13) {
            return false;
        }
        
        // Obtener escala (dígitos decimales)
        int decimalDigits = monto.scale();
        
        // Validar parte decimal (máximo 2 dígitos)
        if (decimalDigits > 2) {
            return false;
        }
        
        return true;
    }
}
