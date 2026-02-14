package com.example.validatingforminput.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
    String message() default "Invalid phone number format (e.g. +1-555-123-4567)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
