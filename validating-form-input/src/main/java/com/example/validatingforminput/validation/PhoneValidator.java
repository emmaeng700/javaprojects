package com.example.validatingforminput.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final String PHONE_PATTERN = "^\\+?[0-9\\-\\s\\(\\)]{7,20}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // let @NotNull handle nulls
        }
        return value.matches(PHONE_PATTERN);
    }
}
