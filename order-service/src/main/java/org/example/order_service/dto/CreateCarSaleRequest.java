package org.example.order_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCarSaleRequest {

    @NotBlank(message = "customerEmail is required")
    @Email(message = "customerEmail must be a valid address")
    private String customerEmail;

    @NotBlank(message = "vehicleVin is required")
    @Size(min = 17, max = 17, message = "vehicleVin must be exactly 17 characters")
    private String vehicleVin;

    @NotBlank(message = "vehicleMake is required")
    private String vehicleMake;

    @NotBlank(message = "vehicleModel is required")
    private String vehicleModel;

    @NotNull(message = "vehicleYear is required")
    @Min(value = 1900, message = "vehicleYear must be 1900 or later")
    private Integer vehicleYear;

    @NotBlank(message = "vehicleColor is required")
    private String vehicleColor;

    @NotNull(message = "vehicleMileage is required")
    @PositiveOrZero(message = "vehicleMileage cannot be negative")
    private Long vehicleMileage;

    @NotNull(message = "salePrice is required")
    @Positive(message = "salePrice must be greater than zero")
    private BigDecimal salePrice;

    @PositiveOrZero(message = "discount cannot be negative")
    private BigDecimal discount;
}
