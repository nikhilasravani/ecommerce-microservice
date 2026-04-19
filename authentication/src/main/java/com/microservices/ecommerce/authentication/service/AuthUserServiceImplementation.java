package com.microservices.ecommerce.authentication.service;

import com.microservices.ecommerce.authentication.dto.*;
import com.microservices.ecommerce.authentication.exception.UserAlreadyExistsException;
import com.microservices.ecommerce.authentication.externalClients.UserFeignClient;
import com.microservices.ecommerce.authentication.jwt.JwtUtil;
import com.microservices.ecommerce.authentication.model.Role;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;

@Service
public class AuthUserServiceImplementation implements AuthUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserFeignClient userFeignClient;
    private final String internalToken;

    public AuthUserServiceImplementation(PasswordEncoder passwordEncoder,
                                         JwtUtil jwtUtil,
                                         UserFeignClient userFeignClient,
                                         @Value("${services.user.internal-token}") String internalToken) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userFeignClient = userFeignClient;
        this.internalToken = internalToken;

    }
    @Override
    public AuthResponseDTO registerUser(RegisterRequestDTO registerRequestDTO) {
        UserResponseDTO createdUser;
        try {
            createdUser = userFeignClient.createUser(registerRequestDTO);
        } catch (FeignException.Conflict ex) {
            throw new UserAlreadyExistsException("User already exists!");
        } catch (FeignException ex) {
            throw new IllegalStateException("User service is unavailable. Registration failed.", ex);
        }

        String token = jwtUtil.generateToken(registerRequestDTO.getEmail(), Role.ROLE_USER, createdUser.getUserId());

        return new AuthResponseDTO(token, createdUser != null ? createdUser.getUserId() : null);
    }

    @Override
    public AuthResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {
        UserAuthResponseDTO authUser;
        try {
            authUser = userFeignClient.getUserAuthByEmail(loginRequestDTO.getEmail(), internalToken);
        } catch (FeignException.NotFound ex) {
            throw new BadCredentialsException("Invalid email or password.", ex);
        } catch (FeignException ex) {
            throw new IllegalStateException("User service is unavailable. Login failed.", ex);
        }

        if (authUser == null || !passwordEncoder.matches(loginRequestDTO.getPassword(), authUser.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        Role role = authUser.getRole() != null ? authUser.getRole() : Role.ROLE_USER;
        String token = jwtUtil.generateToken(authUser.getEmail(), role, authUser.getUserId());

        return new AuthResponseDTO(token, authUser.getUserId());
    }


}
