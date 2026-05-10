package com.microservices.ecommerce.payment.controller;

import com.microservices.ecommerce.payment.dto.PaymentRequestDTO;
import com.microservices.ecommerce.payment.dto.PaymentResponseDTO;
import com.microservices.ecommerce.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDTO> initiatePayment(@Valid @RequestBody PaymentRequestDTO paymentRequestDTO)
    {
        PaymentResponseDTO paymentResponseDTO = paymentService.intiatePayment(paymentRequestDTO);
        return new ResponseEntity<>(paymentResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable UUID orderId)
    {
        PaymentResponseDTO paymentResponseDTO = paymentService.getPaymentByOrderId(orderId);
        return new ResponseEntity<>(paymentResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentByUserId(@PathVariable UUID userId)
    {
        List<PaymentResponseDTO> paymentResponseDTO = paymentService.getPaymentByUserId(userId);
        return new ResponseEntity<>(paymentResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments(){
        List<PaymentResponseDTO> paymentResponseDTO = paymentService.getAllPayments();
        return new ResponseEntity<>(paymentResponseDTO, HttpStatus.OK);
    }
}
