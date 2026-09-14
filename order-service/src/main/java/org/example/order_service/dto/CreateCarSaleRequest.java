package org.example.order_service.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCarSaleRequest {
    private String customerEmail;
    private String vehicleVin;
    private String vehicleMake;
    private String vehicleModel;
    private Integer vehicleYear;
    private String vehicleColor;
    private Long vehicleMileage;
    private BigDecimal salePrice;
    private BigDecimal discount;
}
