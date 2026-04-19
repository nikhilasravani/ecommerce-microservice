package com.microservices.ecommerce.user.userDTO;

import com.microservices.ecommerce.user.appConfig.Role;
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
    private String username;
    private String email;
    private Role role;
}
