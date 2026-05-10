package com.microservices.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {

    private UUID id;
    private UUID userId;
    private List<CartItemResponseDTO> items;
    private Double totalPrice;

}
