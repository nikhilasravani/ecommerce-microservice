package com.microservices.ecommerce.cart.externalClients;

import com.microservices.ecommerce.cart.dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name ="product")
public interface ProductFeignClient {

    @GetMapping("/products/{productId}")
    ProductResponseDTO getProductById(@PathVariable("productId") UUID productId);

}
