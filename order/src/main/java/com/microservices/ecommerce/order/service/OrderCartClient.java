package com.microservices.ecommerce.order.service;

import com.microservices.ecommerce.order.dto.CartResponseDTO;
import com.microservices.ecommerce.order.exception.CartServiceUnavailableException;
import com.microservices.ecommerce.order.externalClients.CartFeignClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderCartClient {

    private final CartFeignClient cartFeignClient;
    private final String cartInternalToken;

    public OrderCartClient(CartFeignClient cartFeignClient,
                           @Value("${services.cart.internal-token}") String cartInternalToken) {
        this.cartFeignClient = cartFeignClient;
        this.cartInternalToken = cartInternalToken;
    }

    @CircuitBreaker(name = "cart", fallbackMethod = "getCartFallback")
    public CartResponseDTO getCartByUserId(UUID userId) {
        return cartFeignClient.getCartByUserId(userId, cartInternalToken);
    }

    public CartResponseDTO getCartFallback(UUID userId, Throwable exception) {
        throw new CartServiceUnavailableException(
                "Cart service is temporarily unavailable, please try again later.",
                exception
        );
    }
}
