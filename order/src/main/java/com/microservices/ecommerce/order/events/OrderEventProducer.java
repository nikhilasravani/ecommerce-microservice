package com.microservices.ecommerce.order.events;

import com.microservices.ecommerce.order.dto.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    // Topic name loaded from application.yml
    @Value("${kafka.topic.order-placed}")
    private String orderPlacedTopic;

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void publishOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for orderId: {}", event.getOrderId());

        kafkaTemplate.send(orderPlacedTopic, event.getOrderId().toString(), event);

        log.info("OrderPlacedEvent published successfully for orderId: {}", event.getOrderId());
    }
}
