package com.campito.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class SaldoActualValidator implements ConstraintValidator<ValidSaldoActual, Float> {

    @Override
    public void initialize(ValidSaldoActual constraintAnnotation) {
    }

    @Override
    public boolean isValid(Float saldo, ConstraintValidatorContext context) {
        if (saldo == null) {
            return true; // Usar @NotNull para validar nulidad
        }

        // Validar que no sea negativo
        if (saldo < 0) {
            return false;
        }

        // Usar BigDecimal para manejar la precisión correctamente
        BigDecimal bd = new BigDecimal(Float.toString(saldo));
        
        // Obtener la parte entera y decimal
        BigDecimal integerPart = bd.setScale(0, BigDecimal.ROUND_DOWN);
        int integerDigits = integerPart.abs().toPlainString().replace(".", "").length();
        
        // Validar parte entera (máximo 11 dígitos)
        if (integerDigits > 11) {
            return false;
        }
        
        // Obtener escala (dígitos decimales)
        int decimalDigits = bd.scale();
        
        // Validar parte decimal (máximo 2 dígitos)
        if (decimalDigits > 2) {
            return false;
        }
        
        return true;
    }
}
