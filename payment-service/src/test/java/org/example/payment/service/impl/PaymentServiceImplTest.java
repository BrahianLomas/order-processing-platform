package org.example.payment.service.impl;

import com.stripe.model.PaymentIntent;
import org.example.payment.dto.PaymentResponse;
import org.example.payment.entity.Payment;
import org.example.payment.kafka.PaymentEventProducer;
import org.example.payment.repository.PaymentRepository;
import org.example.payment.stripe.StripePaymentService;
import org.example.shared.constant.AppConstants;
import org.example.shared.dto.CarSaleCreatedEvent;
import org.example.shared.dto.PaymentProcessedEvent;
import org.example.shared.exception.PaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private StripePaymentService stripePaymentService;
    private PaymentEventProducer eventProducer;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = Mockito.mock(PaymentRepository.class);
        stripePaymentService = Mockito.mock(StripePaymentService.class);
        eventProducer = Mockito.mock(PaymentEventProducer.class);
        paymentService = new PaymentServiceImpl(paymentRepository, stripePaymentService, eventProducer);
    }

    private CarSaleCreatedEvent event() {
        return CarSaleCreatedEvent.builder()
                .saleId(10L)
                .customerId(3L)
                .vehicleVin("1HGCM82633A004352")
                .vehicleMake("Toyota")
                .vehicleModel("Corolla")
                .vehicleYear(2023)
                .salePrice(new BigDecimal("26460.00"))
                .createdAt(LocalDateTime.now())
                .status("PENDING")
                .build();
    }

    /** A tiny in-memory fake backing paymentRepository, so repeated calls to processPayment see evolving state. */
    private AtomicReference<Payment> fakeStore(Payment initial) {
        AtomicReference<Payment> store = new AtomicReference<>(initial);
        when(paymentRepository.findBySaleId(anyLong())).thenAnswer(inv -> Optional.ofNullable(store.get()));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment saved = inv.getArgument(0);
            store.set(saved);
            return saved;
        });
        return store;
    }

    @Test
    void processPayment_stripeSucceeds_savesPaidPaymentAndPublishesEvent() throws Exception {
        fakeStore(null);
        PaymentIntent intent = Mockito.mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_test_123");
        when(stripePaymentService.createPaymentIntent(any(Long.class), any(BigDecimal.class), anyString())).thenReturn(intent);

        paymentService.processPayment(event());

        Mockito.verify(paymentRepository).save(Mockito.argThat(p ->
                p.getStatus().equals(AppConstants.STATUS_PAID) && p.getStripePaymentIntentId().equals("pi_test_123")));

        org.mockito.ArgumentCaptor<PaymentProcessedEvent> captor = org.mockito.ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(eventProducer).publishPaymentProcessed(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AppConstants.STATUS_PAID);
        assertThat(captor.getValue().getTransactionId()).isEqualTo("pi_test_123");
    }

    @Test
    void processPayment_paymentAlreadyExists_skipsStripeAndDoesNotSaveAgain() throws Exception {
        Payment existing = Payment.builder().saleId(10L).customerId(3L).amount(BigDecimal.TEN)
                .currency("USD").status(AppConstants.STATUS_PAID).retryCount(0).build();
        fakeStore(existing);

        paymentService.processPayment(event());

        verify(stripePaymentService, never()).createPaymentIntent(any(), any(), any());
        verify(paymentRepository, never()).save(any());
        verify(eventProducer, never()).publishPaymentProcessed(any());
    }

    @Test
    void processPayment_stripeFailsOnce_createsPendingPaymentWithRetryCountOne() throws Exception {
        fakeStore(null);
        when(stripePaymentService.createPaymentIntent(any(), any(), anyString()))
                .thenThrow(new RuntimeException("Stripe unavailable"));

        paymentService.processPayment(event());

        Mockito.verify(paymentRepository).save(Mockito.argThat(p ->
                p.getStatus().equals(AppConstants.STATUS_PENDING) && p.getRetryCount() == 1));
        verify(eventProducer, never()).publishPaymentProcessed(any());
    }

    /**
     * Pins down how {@code processPayment} actually behaves on a reprocessed event: the "already
     * exists" check matches the PENDING row that {@code handlePaymentFailure} creates after the
     * first failure, so a redelivered event doesn't reach Stripe again and retryCount stays at 1.
     * See Design Decisions in the docs for the full write-up.
     */
    @Test
    void processPayment_calledAgainAfterFirstFailure_getsSkippedAsAlreadyExisting_soRetryNeverAdvances() throws Exception {
        AtomicReference<Payment> store = fakeStore(null);
        when(stripePaymentService.createPaymentIntent(any(), any(), anyString()))
                .thenThrow(new RuntimeException("Stripe unavailable"));

        paymentService.processPayment(event());
        paymentService.processPayment(event());
        paymentService.processPayment(event());

        assertThat(store.get().getRetryCount()).isEqualTo(1);
        assertThat(store.get().getStatus()).isEqualTo(AppConstants.STATUS_PENDING);
        verify(eventProducer, never()).publishPaymentProcessed(any());
        // Only the first call ever reaches Stripe — the other two short-circuit on "already exists".
        verify(stripePaymentService, times(1)).createPaymentIntent(any(), any(), anyString());
    }

    @Test
    void getPaymentBySaleId_found_returnsResponse() {
        Payment payment = Payment.builder().id(7L).saleId(10L).customerId(3L)
                .amount(new BigDecimal("26460.00")).currency("USD")
                .stripePaymentIntentId("pi_test_123").status(AppConstants.STATUS_PAID).retryCount(0).build();
        when(paymentRepository.findBySaleId(10L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentBySaleId(10L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getTransactionId()).isEqualTo("pi_test_123");
        assertThat(response.getStatus()).isEqualTo(AppConstants.STATUS_PAID);
    }

    @Test
    void getPaymentBySaleId_notFound_throwsPaymentException() {
        when(paymentRepository.findBySaleId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentBySaleId(999L))
                .isInstanceOf(PaymentException.class)
                .satisfies(e -> assertThat(((PaymentException) e).getErrorCode()).isEqualTo("PAYMENT_NOT_FOUND"));
    }
}
