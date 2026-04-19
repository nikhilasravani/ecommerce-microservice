package com.microservices.ecommerce.authentication.service;

import com.microservices.ecommerce.authentication.dto.AuthResponseDTO;
import com.microservices.ecommerce.authentication.dto.LoginRequestDTO;
import com.microservices.ecommerce.authentication.dto.RegisterRequestDTO;
import jakarta.validation.Valid;

public interface AuthUserService {

    public AuthResponseDTO registerUser(RegisterRequestDTO registerRequestDTO);

    AuthResponseDTO loginUser(@Valid LoginRequestDTO loginRequestDTO);
}
