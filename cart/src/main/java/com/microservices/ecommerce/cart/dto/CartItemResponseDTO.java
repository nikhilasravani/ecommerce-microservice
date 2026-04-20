package com.microservices.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {

    private UUID id;
    private UUID productId;
    private String productName;
    private Double productPrice;
    private Integer quantity;
}
