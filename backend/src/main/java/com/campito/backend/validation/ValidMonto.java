package com.campito.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MontoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMonto {
    String message() default "El monto no puede exceder los 8 dígitos enteros y 2 decimales";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
