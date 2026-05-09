package com.microservices.ecommerce.order.dto;

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
    private String paymentStatus;
    private String transactionId;
}
