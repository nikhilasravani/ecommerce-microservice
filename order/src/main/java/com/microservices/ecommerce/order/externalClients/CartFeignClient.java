package com.microservices.ecommerce.order.externalClients;

import com.microservices.ecommerce.order.dto.CartResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name="cart")
public interface CartFeignClient {

    @GetMapping("/cart/internal/{userId}")
    CartResponseDTO getCartByUserId(@PathVariable UUID userId,
                                    @RequestHeader("X-Internal-Token") String internalToken);

    @DeleteMapping("/cart/internal/{userId}")
    CartResponseDTO clearCart(@PathVariable UUID userId,
                              @RequestHeader("X-Internal-Token")String internalToken);


}
