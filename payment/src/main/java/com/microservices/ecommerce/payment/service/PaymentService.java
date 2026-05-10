package com.microservices.ecommerce.payment.service;

import com.microservices.ecommerce.payment.dto.PaymentRequestDTO;
import com.microservices.ecommerce.payment.dto.PaymentResponseDTO;
import jakarta.validation.Valid;

import java.util.UUID;
import java.util.List;

public interface PaymentService {
    PaymentResponseDTO intiatePayment(@Valid PaymentRequestDTO paymentRequestDTO);

    PaymentResponseDTO getPaymentByOrderId(UUID orderId);

    List<PaymentResponseDTO> getPaymentByUserId(UUID userId);

    List<PaymentResponseDTO> getAllPayments();
}
