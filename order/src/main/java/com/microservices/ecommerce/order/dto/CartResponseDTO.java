package com.microservices.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO
{
    private UUID cartId;
    private UUID userId;
    private List<CartItemDTO> items;
}
