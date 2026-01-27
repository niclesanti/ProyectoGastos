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

        // Usar BigDecimal para manejar la precisión correctamente
        BigDecimal bd = new BigDecimal(Float.toString(monto));
        
        // Obtener la parte entera y decimal
        BigDecimal integerPart = bd.setScale(0, BigDecimal.ROUND_DOWN);
        int integerDigits = integerPart.abs().toPlainString().replace(".", "").length();
        
        // Validar parte entera (máximo 8 dígitos)
        if (integerDigits > 9) {
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
