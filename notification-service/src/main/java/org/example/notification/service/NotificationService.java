package org.example.notification.service;

import org.example.shared.dto.PaymentProcessedEvent;

public interface NotificationService {
    void sendPaymentConfirmation(PaymentProcessedEvent event);
}
