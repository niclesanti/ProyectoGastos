package com.campito.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class SaldoActualValidator implements ConstraintValidator<ValidSaldoActual, BigDecimal> {

    @Override
    public void initialize(ValidSaldoActual constraintAnnotation) {
    }

    @Override
    public boolean isValid(BigDecimal saldo, ConstraintValidatorContext context) {
        if (saldo == null) {
            return true; // Usar @NotNull para validar nulidad
        }

        // Validar que no sea negativo
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        // Obtener la parte entera y decimal
        BigDecimal integerPart = saldo.setScale(0, BigDecimal.ROUND_DOWN);
        int integerDigits = integerPart.abs().toPlainString().replace(".", "").length();
        
        // Validar parte entera (máximo 12 dígitos para alinearse con NUMERIC(15,2))
        if (integerDigits > 13) {
            return false;
        }
        
        // Obtener escala (dígitos decimales)
        int decimalDigits = saldo.scale();
        
        // Validar parte decimal (máximo 2 dígitos)
        if (decimalDigits > 2) {
            return false;
        }
        
        return true;
    }
}
