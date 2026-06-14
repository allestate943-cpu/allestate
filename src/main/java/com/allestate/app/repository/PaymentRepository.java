package com.allestate.app.repository;

import com.allestate.app.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
    Optional<Payment> findByGatewayEventId(String gatewayEventId);
}

