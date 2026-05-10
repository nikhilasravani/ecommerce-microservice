package com.microservices.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    private UUID productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
