package com.microservices.ecommerce.authentication.dto;

import com.microservices.ecommerce.authentication.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
        private UUID userId;
        private String email;
        private String password;
        private Role role;
}
