package org.example.payment.service;


import org.example.payment.dto.PaymentResponse;
import org.example.shared.dto.CarSaleCreatedEvent;

public interface PaymentService {
    void processPayment(CarSaleCreatedEvent event);
    PaymentResponse getPaymentBySaleId(Long saleId);
}
