package com.microservices.ecommerce.payment.events;

import com.microservices.ecommerce.payment.dto.OrderPlacedEvent;
import com.microservices.ecommerce.payment.dto.PaymentRequestDTO;
import com.microservices.ecommerce.payment.enums.PaymentMethod;
import com.microservices.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final PaymentService paymentService;

    @KafkaListener(
            topics = "${kafka.topic.order-placed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        PaymentRequestDTO paymentRequestDTO = new PaymentRequestDTO();

        //Copy order id from kafka event into payment request
        paymentRequestDTO.setOrderId(event.getOrderId());

        //Copy user id so payment records remains queryable by user
        paymentRequestDTO.setUserId(event.getUserId());

        //Use the order total as payment amount
        paymentRequestDTO.setTotalPrice(event.getTotalPrice());

        // Use a default online method until the caller supplies a selected payment method in the event.
        paymentRequestDTO.setPaymentMethod(PaymentMethod.UPI);

        //Let existing payment service create the payment row.
        paymentService.intiatePayment(paymentRequestDTO);
    }
}
