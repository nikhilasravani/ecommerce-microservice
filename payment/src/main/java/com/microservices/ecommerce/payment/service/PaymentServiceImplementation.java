package com.microservices.ecommerce.payment.service;

import com.microservices.ecommerce.payment.dto.PaymentRequestDTO;
import com.microservices.ecommerce.payment.dto.PaymentResponseDTO;
import com.microservices.ecommerce.payment.enums.PaymentMethod;
import com.microservices.ecommerce.payment.enums.PaymentStatus;
import com.microservices.ecommerce.payment.events.PaymentResultProducer;
import com.microservices.ecommerce.payment.model.Payment;
import com.microservices.ecommerce.payment.repository.PaymentRepository;
import com.microservices.ecommerce.payment.dto.PaymentResultEvent;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImplementation implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;
    private final PaymentResultProducer paymentResultProducer;


    @Override
    @Transactional
    public PaymentResponseDTO intiatePayment(PaymentRequestDTO paymentRequestDTO) {

        Payment payment = new Payment();
        payment.setOrderId(paymentRequestDTO.getOrderId());
        payment.setUserId(paymentRequestDTO.getUserId());
        payment.setAmount(paymentRequestDTO.getTotalPrice());
        payment.setPaymentMethod(paymentRequestDTO.getPaymentMethod());
        payment.setCreatedAt(LocalDateTime.now());

        // COD — no transaction, stays PENDING until delivery
        if(paymentRequestDTO.getPaymentMethod() == PaymentMethod.COD){
            payment.setTransactionId(null);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentTime(null);
        }
        else{

            processRazorpayPayment(paymentRequestDTO, payment);
        }

        Payment savedPayment = paymentRepository.save(payment);
        publishPaymentResultAfterCommit(new PaymentResultEvent(
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getPaymentStatus(),
                savedPayment.getTransactionId()
        ));
        return modelMapper.map(savedPayment,PaymentResponseDTO.class);
    }



    public void processRazorpayPayment(PaymentRequestDTO paymentRequestDTO, Payment payment){
        try{
            // Step 1 — Build Razorpay order request
            // Razorpay expects amount in PAISE (1 rupee = 100 paise)
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",(int)(paymentRequestDTO.getTotalPrice() * 100));
            orderRequest.put("currency","INR");
            orderRequest.put("receipt","txn_"+ paymentRequestDTO.getOrderId());

            // Step 2 — Create order via Razorpay API
            Order razorpayOrder = razorpayClient.orders.create(orderRequest);

            // Step 3 — Check Razorpay response status
            String razorpayStatus = razorpayOrder.get("status");

            if("created".equals(razorpayStatus)){
                // "created" means Razorpay accepted the order — payment link ready
                payment.setTransactionId(razorpayOrder.get("id").toString());
                payment.setPaymentStatus(PaymentStatus.PENDING);
                payment.setPaymentTime(null);
            }
            else{
                // Razorpay returned unexpected status
                payment.setTransactionId(null);
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setPaymentTime(LocalDateTime.now());
            }
        }
        catch(Exception e){
            // Razorpay threw exception — network issue, invalid keys, etc.
            payment.setTransactionId(null);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setPaymentTime(LocalDateTime.now());
        }
    }

    @Override
    public PaymentResponseDTO getPaymentByOrderId(UUID orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId).
                orElseThrow(()-> new RuntimeException("Payment not found for Order Id : "+orderId));

        return modelMapper.map(payment,PaymentResponseDTO.class);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentByUserId(UUID userId) {

        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(payment-> modelMapper.map(payment, PaymentResponseDTO.class))
                .toList();
    }

    @Override
    public List<PaymentResponseDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream()
                .map(payment-> modelMapper.map(payment, PaymentResponseDTO.class))
                .toList();
    }

    private void publishPaymentResultAfterCommit(PaymentResultEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            paymentResultProducer.publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                paymentResultProducer.publish(event);
            }
        });
    }
}
