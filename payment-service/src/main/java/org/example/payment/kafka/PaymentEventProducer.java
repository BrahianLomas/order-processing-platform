package org.example.payment.kafka;

import lombok.RequiredArgsConstructor;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.PaymentProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        kafkaTemplate.send(AppConstants.TOPIC_PAYMENT_PROCESSED, event);
    }
}
