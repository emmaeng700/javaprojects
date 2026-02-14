package com.example.validatingforminput;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PersonFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private PersonForm validForm() {
        PersonForm form = new PersonForm();
        form.setName("John Doe");
        form.setEmail("john@example.com");
        form.setAge(25);
        form.setPassword("SecurePass1");
        form.setConfirmPassword("SecurePass1");
        return form;
    }

    @Test
    void validFormShouldHaveNoViolations() {
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(validForm());
        assertTrue(violations.isEmpty(), "Valid form should have no violations");
    }

    @Test
    void nameTooShortShouldFail() {
        PersonForm form = validForm();
        form.setName("A");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void nameTooLongShouldFail() {
        PersonForm form = validForm();
        form.setName("A".repeat(31));
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullNameShouldFail() {
        PersonForm form = validForm();
        form.setName(null);
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidEmailShouldFail() {
        PersonForm form = validForm();
        form.setEmail("not-an-email");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void ageBelowMinimumShouldFail() {
        PersonForm form = validForm();
        form.setAge(17);
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("age")));
    }

    @Test
    void ageAboveMaximumShouldFail() {
        PersonForm form = validForm();
        form.setAge(200);
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("age")));
    }

    @Test
    void nullAgeShouldFail() {
        PersonForm form = validForm();
        form.setAge(null);
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordWithoutUppercaseShouldFail() {
        PersonForm form = validForm();
        form.setPassword("lowercase1");
        form.setConfirmPassword("lowercase1");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void passwordWithoutDigitShouldFail() {
        PersonForm form = validForm();
        form.setPassword("NoDigitHere");
        form.setConfirmPassword("NoDigitHere");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void passwordTooShortShouldFail() {
        PersonForm form = validForm();
        form.setPassword("Ab1");
        form.setConfirmPassword("Ab1");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void mismatchedPasswordsShouldFail() {
        PersonForm form = validForm();
        form.setPassword("SecurePass1");
        form.setConfirmPassword("DifferentPass1");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertFalse(violations.isEmpty());
    }

    @Test
    void validPhoneNumberShouldPass() {
        PersonForm form = validForm();
        form.setPhone("+1-555-123-4567");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidPhoneNumberShouldFail() {
        PersonForm form = validForm();
        form.setPhone("abc-not-a-phone");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));
    }

    @Test
    void emptyPhoneShouldPass() {
        PersonForm form = validForm();
        form.setPhone("");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty(), "Empty phone should be allowed (optional field)");
    }

    @Test
    void exactMinNameLengthShouldPass() {
        PersonForm form = validForm();
        form.setName("Jo");
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void exactMinAgeShouldPass() {
        PersonForm form = validForm();
        form.setAge(18);
        Set<ConstraintViolation<PersonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }
}
