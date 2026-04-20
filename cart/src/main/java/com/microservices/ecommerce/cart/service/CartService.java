package com.microservices.ecommerce.cart.service;

import com.microservices.ecommerce.cart.dto.CartItemRequestDTO;
import com.microservices.ecommerce.cart.dto.CartResponseDTO;
import com.microservices.ecommerce.cart.exception.UserNotFoundException;

import java.util.UUID;

public interface CartService {

    CartResponseDTO getCartByUserId(UUID userId) throws UserNotFoundException;
    CartResponseDTO addItemToCart(UUID userId, CartItemRequestDTO request) throws UserNotFoundException;
    CartResponseDTO updateQuantityOfCartItem(UUID userId, UUID cartItemId, Integer quantity);
    CartResponseDTO removeItemFromCart(UUID userId, UUID cartItemId);
    CartResponseDTO clearCart(UUID userId) throws UserNotFoundException;

}
