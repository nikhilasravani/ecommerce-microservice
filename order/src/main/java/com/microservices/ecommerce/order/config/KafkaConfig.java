package com.microservices.ecommerce.order.config;

import com.microservices.ecommerce.order.dto.OrderPlacedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig{

    // Spring Boot auto-creates ProducerFactory from application.yml
    // We just need to declare a typed KafkaTemplate bean
    @Bean
    public KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate(
            ProducerFactory<String, OrderPlacedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
