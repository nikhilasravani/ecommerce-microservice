package com.microservices.ecommerce.payment.dto;

import com.microservices.ecommerce.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultEvent {
    private UUID orderId;
    private UUID userId;
    private PaymentStatus paymentStatus;
    private String transactionId;

}
