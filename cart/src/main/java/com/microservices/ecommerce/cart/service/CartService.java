package com.microservices.ecommerce.cart.service;

import com.microservices.ecommerce.cart.dto.CartItemRequestDTO;
import com.microservices.ecommerce.cart.dto.CartResponseDTO;

import java.util.UUID;

public interface CartService {

    CartResponseDTO getCartByUserId(UUID userId);
    CartResponseDTO addItemToCart(UUID userId, CartItemRequestDTO request);
    CartResponseDTO updateQuantityOfCartItem(UUID userId, UUID cartItemId, Integer quantity);
    CartResponseDTO removeItemFromCart(UUID userId, UUID cartItemId);
    CartResponseDTO clearCart(UUID userId);

}
