package org.example.payment.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    public PaymentIntent createPaymentIntent(Long saleId, BigDecimal amount, String currency) throws StripeException {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount.multiply(new BigDecimal("100")).longValue()) // Stripe usa centavos
                    .setCurrency(currency.toLowerCase())
                    .putMetadata("saleId", saleId.toString())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("Stripe PaymentIntent creado: {} para venta: {}", intent.getId(), saleId);

            return intent;
        } catch (StripeException e) {
            log.error("Error creando PaymentIntent en Stripe: {}", e.getMessage());
            throw e;
        }
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
}
