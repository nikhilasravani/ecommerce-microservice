package com.microservices.ecommerce.order.service;

import com.microservices.ecommerce.order.dto.OrderResponseDTO;

import java.util.UUID;
import java.util.List;

public interface OrderService {

    public OrderResponseDTO placeOrder(UUID userId) throws Exception;
    public List<OrderResponseDTO> getOrdersByUserId(UUID userId);
}
