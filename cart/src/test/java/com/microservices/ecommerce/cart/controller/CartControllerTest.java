package com.microservices.ecommerce.cart.controller;

import com.microservices.ecommerce.cart.dto.CartItemRequestDTO;
import com.microservices.ecommerce.cart.dto.CartResponseDTO;
import com.microservices.ecommerce.cart.jwt.JwtAuthenticatedUser;
import com.microservices.ecommerce.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    void getCart_UsesUserIdFromAuthenticatedPrincipal() {
        UUID userId = UUID.randomUUID();
        CartResponseDTO responseDTO = new CartResponseDTO();
        responseDTO.setUserId(userId);

        when(cartService.getCartByUserId(userId)).thenReturn(responseDTO);

        Authentication authentication = authentication(userId, "ROLE_USER");

        CartResponseDTO response = cartController.getCart(authentication).getBody();

        assertEquals(userId, response.getUserId());
        verify(cartService).getCartByUserId(userId);
    }

    @Test
    void addItemToCart_UsesUserIdFromAuthenticatedPrincipal() {
        UUID userId = UUID.randomUUID();
        CartItemRequestDTO request = new CartItemRequestDTO(UUID.randomUUID(), 2);
        CartResponseDTO responseDTO = new CartResponseDTO();
        responseDTO.setUserId(userId);

        when(cartService.addItemToCart(userId, request)).thenReturn(responseDTO);

        CartResponseDTO response = cartController.addItemToCart(request, authentication(userId, "ROLE_USER")).getBody();

        assertEquals(userId, response.getUserId());
        verify(cartService).addItemToCart(userId, request);
    }

    private Authentication authentication(UUID userId, String role) {
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(userId, "user@example.com");
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
