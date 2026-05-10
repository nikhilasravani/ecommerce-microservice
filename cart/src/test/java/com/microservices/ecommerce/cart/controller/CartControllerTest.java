package com.microservices.ecommerce.cart.controller;

import com.microservices.ecommerce.cart.dto.CartResponseDTO;
import com.microservices.ecommerce.cart.jwt.JwtAuthenticatedUser;
import com.microservices.ecommerce.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    void getCart_WhenAuthenticatedUserOwnsCart_ShouldReturnCart() throws Exception {
        UUID userId = UUID.randomUUID();
        CartResponseDTO responseDTO = new CartResponseDTO();
        responseDTO.setUserId(userId);

        when(cartService.getCartByUserId(userId)).thenReturn(responseDTO);

        Authentication authentication = authentication(userId, "ROLE_USER");

        CartResponseDTO response = cartController.getCart(userId, authentication).getBody();

        assertEquals(userId, response.getUserId());
        verify(cartService).getCartByUserId(userId);
    }

    @Test
    void getCart_WhenAuthenticatedUserTargetsDifferentCart_ShouldThrowAccessDenied() throws Exception {
        UUID tokenUserId = UUID.randomUUID();
        UUID requestedUserId = UUID.randomUUID();

        Authentication authentication = authentication(tokenUserId, "ROLE_USER");

        assertThrows(AccessDeniedException.class,
                () -> cartController.getCart(requestedUserId, authentication));

        verify(cartService, never()).getCartByUserId(requestedUserId);
    }

    @Test
    void getCart_WhenAdminTargetsDifferentCart_ShouldAllowAccess() throws Exception {
        UUID adminUserId = UUID.randomUUID();
        UUID requestedUserId = UUID.randomUUID();
        CartResponseDTO responseDTO = new CartResponseDTO();
        responseDTO.setUserId(requestedUserId);

        when(cartService.getCartByUserId(requestedUserId)).thenReturn(responseDTO);

        Authentication authentication = authentication(adminUserId, "ROLE_ADMIN");

        CartResponseDTO response = cartController.getCart(requestedUserId, authentication).getBody();

        assertEquals(requestedUserId, response.getUserId());
        verify(cartService).getCartByUserId(requestedUserId);
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
