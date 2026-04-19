package com.microservices.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {

    private Long productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Integer productStock;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
