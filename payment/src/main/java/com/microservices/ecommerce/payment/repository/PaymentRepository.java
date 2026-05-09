package com.microservices.ecommerce.payment.repository;

import com.microservices.ecommerce.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {


    Optional<Payment> findByOrderId(UUID orderId);

    List<Payment> findByUserId(UUID userId);
}
