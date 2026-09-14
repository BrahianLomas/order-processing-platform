package org.example.notification.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notification.service.NotificationService;
import org.example.shared.dto.PaymentProcessedEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendPaymentConfirmation(PaymentProcessedEvent event) {
        log.info("📨 Enviando confirmación de pago...");

        // Mock: simular envío de email
        String emailContent = String.format(
                "Hola,\n\n" +
                        "Tu pago ha sido procesado exitosamente.\n" +
                        "Venta ID: %d\n" +
                        "Monto: $%.2f %s\n" +
                        "Transacción: %s\n" +
                        "Estado: %s\n\n" +
                        "Gracias por tu compra!",
                event.getSaleId(),
                event.getAmount(),
                event.getCurrency(),
                event.getTransactionId(),
                event.getStatus()
        );

        log.info("✉️ EMAIL ENVIADO:");
        log.info(emailContent);

        // Mock: simular envío de SMS
        log.info("📱 SMS ENVIADO: Pago confirmado para venta {}. Transacción: {}",
                event.getSaleId(), event.getTransactionId());
    }
}
