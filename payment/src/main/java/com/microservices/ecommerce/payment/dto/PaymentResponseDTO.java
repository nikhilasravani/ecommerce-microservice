package com.microservices.ecommerce.payment.dto;

import com.microservices.ecommerce.payment.enums.PaymentMethod;
import com.microservices.ecommerce.payment.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponseDTO {

    private UUID paymentId;
    private UUID orderId;
    private UUID userId;
    private Double amount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private LocalDateTime paymentTime;
    private LocalDateTime createdAt;
}
