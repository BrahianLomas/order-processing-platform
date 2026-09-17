package org.example.order_service.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCarSaleRequestValidationTest {

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

    private CreateCarSaleRequest validRequest() {
        CreateCarSaleRequest request = new CreateCarSaleRequest();
        request.setCustomerEmail("cliente@test.com");
        request.setVehicleVin("1HGCM82633A004352"); // 17 chars
        request.setVehicleMake("Toyota");
        request.setVehicleModel("Corolla");
        request.setVehicleYear(2023);
        request.setVehicleColor("Blanco");
        request.setVehicleMileage(0L);
        request.setSalePrice(new BigDecimal("25000"));
        request.setDiscount(BigDecimal.ZERO);
        return request;
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void shortVin_hasViolation() {
        CreateCarSaleRequest request = validRequest();
        request.setVehicleVin("TOO-SHORT");

        Set<ConstraintViolation<CreateCarSaleRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("vehicleVin");
    }

    @Test
    void negativeSalePrice_hasViolation() {
        CreateCarSaleRequest request = validRequest();
        request.setSalePrice(new BigDecimal("-100"));

        Set<ConstraintViolation<CreateCarSaleRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("salePrice");
    }

    @Test
    void yearBefore1900_hasViolation() {
        CreateCarSaleRequest request = validRequest();
        request.setVehicleYear(1899);

        Set<ConstraintViolation<CreateCarSaleRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("vehicleYear");
    }

    @Test
    void negativeMileage_hasViolation() {
        CreateCarSaleRequest request = validRequest();
        request.setVehicleMileage(-1L);

        Set<ConstraintViolation<CreateCarSaleRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("vehicleMileage");
    }
}
