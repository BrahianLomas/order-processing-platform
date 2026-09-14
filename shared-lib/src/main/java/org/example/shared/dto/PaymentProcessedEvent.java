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
public class PaymentProcessedEvent {

    @JsonProperty("sale_id")
    private Long saleId;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("status")
    private String status;

    @JsonProperty("processed_at")
    private LocalDateTime processedAt;

    @JsonProperty("error_message")
    private String errorMessage;
}
