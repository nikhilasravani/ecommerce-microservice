package com.microservices.ecommerce.cart.externalClients;

import com.microservices.ecommerce.cart.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name="user")
public interface UserFeignClient {

    @GetMapping("/users/internal/{userId}")
    UserResponseDTO getUserById(@PathVariable("userId") UUID userId,
                                @RequestHeader("X-Internal-Token") String internalToken);
}
