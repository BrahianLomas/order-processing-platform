package org.example.payment.service.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.payment.dto.PaymentResponse;
import org.example.payment.entity.Payment;
import org.example.payment.kafka.PaymentEventProducer;
import org.example.payment.repository.PaymentRepository;
import org.example.payment.service.PaymentService;
import org.example.payment.stripe.StripePaymentService;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.example.shared.dto.PaymentProcessedEvent;
import org.example.shared.exception.PaymentException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripePaymentService stripePaymentService;
    private final PaymentEventProducer eventProducer;

    @Override
    @Retry(name = "stripe-api")
    @CircuitBreaker(name = "stripe-api", fallbackMethod = "processPaymentFallback")
    public void processPayment(CarSaleCreatedEvent event) {
        try {
            // Verificar si ya existe pago
            Optional<Payment> existingPayment = paymentRepository.findBySaleId(event.getSaleId());
            if (existingPayment.isPresent()) {
                log.warn("Pago ya existe para venta: {}", event.getSaleId());
                return;
            }

            // Crear PaymentIntent en Stripe
            PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                    event.getSaleId(),
                    event.getSalePrice(),
                    "USD");

            // Guardar Payment
            Payment payment = Payment.builder()
                    .saleId(event.getSaleId())
                    .customerId(event.getCustomerId())
                    .amount(event.getSalePrice())
                    .currency("USD")
                    .stripePaymentIntentId(paymentIntent.getId())
                    .status(AppConstants.STATUS_PAID) // ← EN TEST MODE, SIMULAR PAGADO
                    .retryCount(0)
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            // Publicar evento de éxito
            PaymentProcessedEvent successEvent = PaymentProcessedEvent.builder()
                    .saleId(event.getSaleId())
                    .transactionId(paymentIntent.getId())
                    .amount(event.getSalePrice())
                    .currency("USD")
                    .status(AppConstants.STATUS_PAID)
                    .processedAt(LocalDateTime.now())
                    .build();

            eventProducer.publishPaymentProcessed(successEvent);
            log.info("✅ Pago procesado exitosamente: {}", event.getSaleId());

        } catch (StripeException e) {
            log.error("Error en Stripe: {}", e.getMessage());
            handlePaymentFailure(event, e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando pago: {}", e.getMessage());
            handlePaymentFailure(event, e.getMessage());
        }
    }

    @Override
    public PaymentResponse getPaymentBySaleId(Long saleId) {
        Payment payment = paymentRepository.findBySaleId(saleId)
                .orElseThrow(() -> new PaymentException("PAYMENT_NOT_FOUND", "Pago no encontrado"));

        return new PaymentResponse(
                payment.getId(),
                payment.getSaleId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getStripePaymentIntentId(),
                payment.getCreatedAt());
    }

    public void processPaymentFallback(CarSaleCreatedEvent event, Exception e) {
        log.error("Fallback: Circuit breaker abierto para venta: {}, error: {}", event.getSaleId(), e.getMessage());
        handlePaymentFailure(event, "Stripe no disponible - reintentando más tarde");
    }

    private void handlePaymentFailure(CarSaleCreatedEvent event, String errorMessage) {
        Optional<Payment> paymentOpt = paymentRepository.findBySaleId(event.getSaleId());

        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setRetryCount(payment.getRetryCount() + 1);
            payment.setErrorMessage(errorMessage);

            if (payment.getRetryCount() >= 3) {
                payment.setStatus(AppConstants.STATUS_FAILED);
                log.error("Pago fallido después de 3 reintentos: {}", event.getSaleId());

                // Publicar evento de fallo
                PaymentProcessedEvent failEvent = PaymentProcessedEvent.builder()
                        .saleId(event.getSaleId())
                        .amount(event.getSalePrice())
                        .currency("USD")
                        .status(AppConstants.STATUS_FAILED)
                        .errorMessage(errorMessage)
                        .processedAt(LocalDateTime.now())
                        .build();

                eventProducer.publishPaymentProcessed(failEvent);
            }

            paymentRepository.save(payment);
        } else {
            Payment payment = Payment.builder()
                    .saleId(event.getSaleId())
                    .customerId(event.getCustomerId())
                    .amount(event.getSalePrice())
                    .currency("USD")
                    .status(AppConstants.STATUS_PENDING)
                    .errorMessage(errorMessage)
                    .retryCount(1)
                    .build();

            paymentRepository.save(payment);
        }
    }
}
