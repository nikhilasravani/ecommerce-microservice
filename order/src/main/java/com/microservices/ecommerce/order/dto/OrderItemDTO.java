package com.microservices.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private UUID productId;
    private String productName;
    private Double  productPrice;
    private Integer quantity;
}
