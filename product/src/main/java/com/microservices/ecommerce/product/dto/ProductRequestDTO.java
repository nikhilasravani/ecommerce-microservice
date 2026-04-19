package com.microservices.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String productName;

    @NotBlank
    @Size(max = 500)
    private String productDescription;

    @NotNull
    @Positive
    private Double productPrice;

    @NotNull
    @Min(0)
    private Integer productStock;
}
