package org.example.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarSaleCreatedEvent {

    @JsonProperty("sale_id")
    private Long saleId;

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("vehicle_vin")
    private String vehicleVin;

    @JsonProperty("vehicle_make")
    private String vehicleMake;

    @JsonProperty("vehicle_model")
    private String vehicleModel;

    @JsonProperty("vehicle_year")
    private Integer vehicleYear;

    @JsonProperty("sale_price")
    private BigDecimal salePrice;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("status")
    private String status;
}
