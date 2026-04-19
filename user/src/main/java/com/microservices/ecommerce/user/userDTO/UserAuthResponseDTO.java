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
public class UserAuthResponseDTO {
    private UUID userId;
    private String email;
    private String password;
    private Role role;
}
