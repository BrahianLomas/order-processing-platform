package org.example.payment.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.service.PaymentService;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarSaleEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = AppConstants.TOPIC_CAR_SALE_CREATED, groupId = "payment-service")
    public void consumeCarSaleCreated(CarSaleCreatedEvent event) {
        try {
            log.info("Evento recibido: CarSaleCreated para venta: {}", event.getSaleId());
            paymentService.processPayment(event);
        } catch (Exception e) {
            log.error("Error procesando evento CarSaleCreated: {}", e.getMessage(), e);
        }
    }
}
