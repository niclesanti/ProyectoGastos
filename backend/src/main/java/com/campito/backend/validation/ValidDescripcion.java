package com.campito.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DescripcionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDescripcion {
    String message() default "La descripción solo puede contener letras, números, coma, paréntesis, guiones y barra";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
