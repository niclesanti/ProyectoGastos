package com.campito.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SaldoActualValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSaldoActual {
    String message() default "El saldo actual no puede exceder los 11 dígitos enteros y 2 decimales";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
