package com.example.validatingforminput.validation;

import com.example.validatingforminput.PersonForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, PersonForm> {

    @Override
    public boolean isValid(PersonForm form, ConstraintValidatorContext context) {
        if (form.getPassword() == null || form.getConfirmPassword() == null) {
            return true;
        }
        boolean matches = form.getPassword().equals(form.getConfirmPassword());
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return matches;
    }
}
