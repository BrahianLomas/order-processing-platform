package org.example.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.shared.response.GenericResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentResponse extends GenericResponse {

    private Long id;
    private Long saleId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String transactionId;
    private LocalDateTime createdAt;

    public PaymentResponse(Long id, Long saleId, BigDecimal amount, String currency,
                            String status, String transactionId, LocalDateTime createdAt) {
        this.id = id;
        this.saleId = saleId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
    }
}
