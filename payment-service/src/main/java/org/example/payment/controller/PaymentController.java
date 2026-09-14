package org.example.payment.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

import org.example.payment.dto.PaymentResponse;
import org.example.payment.service.PaymentService;
import org.example.shared.response.GenericResponse;
import org.example.shared.response.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/sales/{saleId}")
    public ResponseEntity<GenericResponse> getPaymentBySaleId(@PathVariable @Positive(message = "saleId must be a positive number") Long saleId) {
        try {
            PaymentResponse response = paymentService.getPaymentBySaleId(saleId);
            response.markSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(GenericResponse.error(ResponseCode.NOT_FOUND, e.getMessage()));
        }
    }
}
