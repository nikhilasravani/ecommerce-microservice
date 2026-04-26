package com.microservices.ecommerce.cart.controller;

import com.microservices.ecommerce.cart.dto.CartItemRequestDTO;
import com.microservices.ecommerce.cart.dto.CartResponseDTO;
import com.microservices.ecommerce.cart.jwt.JwtAuthenticatedUser;
import com.microservices.ecommerce.cart.service.CartService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable UUID userId,
                                                   Authentication authentication) {
        validateUserAccess(userId, authentication);
        CartResponseDTO response = cartService.getCartByUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(@PathVariable UUID userId,
                                                         @RequestBody CartItemRequestDTO request,
                                                         Authentication authentication) {
        validateUserAccess(userId, authentication);
        CartResponseDTO response = cartService.addItemToCart(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateQuantity(@PathVariable UUID userId,
                                                          @PathVariable UUID cartItemId,
                                                          @RequestBody CartItemRequestDTO request,
                                                          Authentication authentication){
        validateUserAccess(userId, authentication);
        CartResponseDTO response = cartService.updateQuantityOfCartItem(userId,
                cartItemId, request.getQuantity());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<?>  deleteItemFromCart(@PathVariable UUID userId,
                                                 @PathVariable UUID cartItemId,
                                                 Authentication authentication){
        validateUserAccess(userId, authentication);
        cartService.removeItemFromCart(userId, cartItemId);
        return new ResponseEntity<>("Item removed from the cart successfully!",HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<CartResponseDTO> clearCart(@PathVariable UUID userId,
                                                     Authentication authentication) {
        validateUserAccess(userId, authentication);
        CartResponseDTO response = cartService.clearCart(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void validateUserAccess(UUID userId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser jwtUser)) {
            throw new AccessDeniedException("Authentication is required to access the cart.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin && !userId.equals(jwtUser.userId())) {
            throw new AccessDeniedException("You are not allowed to access another user's cart.");
        }
    }

}
