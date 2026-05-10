package com.microservices.ecommerce.cart.jwt;

import java.util.UUID;

public record JwtAuthenticatedUser(UUID userId, String email) {
}
