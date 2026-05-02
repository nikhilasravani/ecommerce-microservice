package com.microservices.ecommerce.order.externalClients;

import com.microservices.ecommerce.order.dto.StockUpdateRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "product")
public interface ProductFeignClient {

    @PutMapping("/products/internal/{productId}/stock/decrement")
    void reduceStock(@PathVariable UUID productId,
                     @RequestBody StockUpdateRequestDTO request,
                     @RequestHeader("X-Internal-Token") String internalToken);
}
