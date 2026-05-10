package com.microservices.ecommerce.order.config;

import com.microservices.ecommerce.order.dto.OrderResponseDTO;
import com.microservices.ecommerce.order.model.Order;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Order.class, OrderResponseDTO.class).addMappings(mapper -> {
            mapper.map(Order::getStatus, OrderResponseDTO::setOrderStatus);
            mapper.map(Order::getTotalAmount, OrderResponseDTO::setTotalPrice);
            mapper.map(Order::getCreatedAt, OrderResponseDTO::setOrderDate);
        });
        return modelMapper;
    }
}
