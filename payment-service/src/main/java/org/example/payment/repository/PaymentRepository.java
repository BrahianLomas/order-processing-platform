package org.example.payment.repository;

import org.example.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySaleId(Long saleId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
