package com.microservices.ecommerce.payment.dto;

import com.microservices.ecommerce.payment.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
public class PaymentRequestDTO {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID userId;

    @NotNull
    @Positive
    private Double totalPrice;

    @NotNull
    private PaymentMethod paymentMethod;
}
