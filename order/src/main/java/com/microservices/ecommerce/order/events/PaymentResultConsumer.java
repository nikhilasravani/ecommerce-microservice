package com.microservices.ecommerce.order.events;

import com.microservices.ecommerce.order.dto.PaymentResultEvent;
import com.microservices.ecommerce.order.enums.OrderStatus;
import com.microservices.ecommerce.order.model.Order;
import com.microservices.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "${kafka.topic.payment-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumePaymentResult(PaymentResultEvent event) {
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found for id: " + event.getOrderId()));

        if ("SUCCESS".equals(event.getPaymentStatus())) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else if ("FAILED".equals(event.getPaymentStatus())) {
            order.setStatus(OrderStatus.CANCELED);
        } else {
            return;
        }

        orderRepository.save(order);
    }
}
