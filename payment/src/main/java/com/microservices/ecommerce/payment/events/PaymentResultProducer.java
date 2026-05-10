package com.microservices.ecommerce.payment.events;

import com.microservices.ecommerce.payment.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentResultProducer {

    private final KafkaTemplate<String, PaymentResultEvent> kafkaTemplate;

    @Value("${kafka.topic.payment-result}")
    private String paymentResultTopic;

    public void publish(PaymentResultEvent event){
        //Use orderId as kafka key so all events for the same order go to same partition
        kafkaTemplate.send(paymentResultTopic, event.getOrderId().toString(), event);
    }
}
