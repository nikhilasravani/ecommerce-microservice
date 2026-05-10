package com.microservices.ecommerce.order.dto;

import com.microservices.ecommerce.order.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private UUID orderId;
    private UUID userId;
    private OrderStatus orderStatus;
    private Double totalPrice;
    private LocalDateTime orderDate;
    private List<OrderItemDTO> orderItems;
}
