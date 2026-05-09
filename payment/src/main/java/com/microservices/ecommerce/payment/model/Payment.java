package com.microservices.ecommerce.payment.model;

import com.microservices.ecommerce.payment.enums.PaymentMethod;
import com.microservices.ecommerce.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="payment_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    @Column(nullable=false)
    private UUID orderId;

    @Column(nullable=false)
    private UUID userId;

    @Column(nullable=false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private PaymentMethod paymentMethod;

    @Column(unique=true)
    private String transactionId;

    private LocalDateTime paymentTime;

    @Column(updatable=false, nullable=false)
    private LocalDateTime createdAt;


}
