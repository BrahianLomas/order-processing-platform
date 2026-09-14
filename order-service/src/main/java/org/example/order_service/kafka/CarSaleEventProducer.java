package org.example.order_service.kafka;

import lombok.RequiredArgsConstructor;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CarSaleEventProducer {

    private final KafkaTemplate<String, CarSaleCreatedEvent> kafkaTemplate;

    public void publishCarSaleCreated(CarSaleCreatedEvent event) {
        kafkaTemplate.send(AppConstants.TOPIC_CAR_SALE_CREATED, event);
    }
}
