package com.universidad.biblioteca.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valida que el valor sea un ISBN-10 o ISBN-13 sintacticamente correcto,
 * incluyendo la verificacion del digito de control (checksum).
 */
@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIsbn {
    String message() default "El ISBN no es valido (debe ser ISBN-10 o ISBN-13 con digito de control correcto)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
