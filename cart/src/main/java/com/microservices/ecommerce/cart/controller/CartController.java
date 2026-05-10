package com.microservices.ecommerce.cart.controller;

import com.microservices.ecommerce.cart.dto.CartItemRequestDTO;
import com.microservices.ecommerce.cart.dto.CartResponseDTO;
import com.microservices.ecommerce.cart.jwt.JwtAuthenticatedUser;
import com.microservices.ecommerce.cart.service.CartService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @Value("${services.cart.internal-token}")
    private String cartInternalToken;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCart(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        CartResponseDTO response = cartService.getCartByUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/internal/{userId}")
    public ResponseEntity<CartResponseDTO> getCartByUserIdInternal(@PathVariable UUID userId,
                                                                   @RequestHeader("X-Internal-Token")String internalToken) {
        if(!internalToken.equals(cartInternalToken)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Invalid internal token");
        }
        CartResponseDTO response = cartService.getCartByUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDTO> addItemToCart(@RequestBody CartItemRequestDTO request,
                                                         Authentication authentication) {
        UUID userId = currentUserId(authentication);
        CartResponseDTO response = cartService.addItemToCart(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponseDTO> updateQuantity(@PathVariable UUID cartItemId,
                                                          @RequestBody CartItemRequestDTO request,
                                                          Authentication authentication){
        UUID userId = currentUserId(authentication);
        CartResponseDTO response = cartService.updateQuantityOfCartItem(userId,
                cartItemId, request.getQuantity());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?>  deleteItemFromCart(@PathVariable UUID cartItemId,
                                                 Authentication authentication){
        UUID userId = currentUserId(authentication);
        cartService.removeItemFromCart(userId, cartItemId);
        return new ResponseEntity<>("Item removed from the cart successfully!",HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<CartResponseDTO> clearCart(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        CartResponseDTO response = cartService.clearCart(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/internal/{userId}")
    public ResponseEntity<CartResponseDTO> clearCartInternal(@PathVariable UUID userId,
                                                             @RequestHeader("X-Internal-Token")String internalToken) {
        if(!internalToken.equals(cartInternalToken)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Invalid internal token");
        }
        CartResponseDTO response = cartService.clearCart(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private UUID currentUserId(Authentication authentication) {
        return currentUser(authentication).userId();
    }

    private JwtAuthenticatedUser currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser jwtUser)) {
            throw new AccessDeniedException("Authentication is required to access the cart.");
        }
        return jwtUser;
    }

}
