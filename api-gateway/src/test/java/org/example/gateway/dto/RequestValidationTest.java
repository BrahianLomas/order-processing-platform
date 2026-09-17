package org.example.gateway.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void registerRequest_allFieldsValid_hasNoViolations() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("demo@test.com");
        request.setPassword("secret123");
        request.setFirstName("Demo");
        request.setLastName("User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void registerRequest_invalidEmail_hasViolation() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("not-an-email");
        request.setPassword("secret123");
        request.setFirstName("Demo");
        request.setLastName("User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("email");
    }

    @Test
    void registerRequest_shortPassword_hasViolation() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("demo@test.com");
        request.setPassword("123");
        request.setFirstName("Demo");
        request.setLastName("User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("password");
    }

    @Test
    void registerRequest_blankFirstName_hasViolation() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("demo@test.com");
        request.setPassword("secret123");
        request.setFirstName("");
        request.setLastName("User");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("firstName");
    }

    @Test
    void loginRequest_blankCredentials_hasTwoViolations() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }

    @Test
    void loginRequest_validCredentials_hasNoViolations() {
        LoginRequest request = new LoginRequest();
        request.setEmail("demo@test.com");
        request.setPassword("secret123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
