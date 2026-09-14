package org.example.notification.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.service.NotificationService;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.PaymentProcessedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessedEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = AppConstants.TOPIC_PAYMENT_PROCESSED, groupId = "notification-service")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        try {
            log.info("📧 Evento recibido: PaymentProcessed para venta: {}", event.getSaleId());
            notificationService.sendPaymentConfirmation(event);
        } catch (Exception e) {
            log.error("❌ Error enviando notificación: {}", e.getMessage(), e);
        }
    }
}
