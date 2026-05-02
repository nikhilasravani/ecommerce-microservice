package com.microservices.ecommerce.authentication.service;

import com.microservices.ecommerce.authentication.dto.AuthResponseDTO;
import com.microservices.ecommerce.authentication.dto.LoginRequestDTO;
import com.microservices.ecommerce.authentication.dto.RegisterRequestDTO;
import com.microservices.ecommerce.authentication.dto.UserAuthResponseDTO;
import com.microservices.ecommerce.authentication.dto.UserResponseDTO;
import com.microservices.ecommerce.authentication.exception.UserAlreadyExistsException;
import com.microservices.ecommerce.authentication.externalClients.UserFeignClient;
import com.microservices.ecommerce.authentication.jwt.JwtUtil;
import com.microservices.ecommerce.authentication.model.Role;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceImplementationTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserFeignClient userFeignClient;

    private AuthUserServiceImplementation authUserServiceImplementation;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private UserAuthResponseDTO userAuthResponseDTO;
    private UserResponseDTO userResponseDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        registerRequestDTO = RegisterRequestDTO.builder()
                .username("nikhil")
                .email("nikhil@example.com")
                .password("password123")
                .build();

        loginRequestDTO = LoginRequestDTO.builder()
                .email("nikhil@example.com")
                .password("password123")
                .build();

        userAuthResponseDTO = UserAuthResponseDTO.builder()
                .userId(userId)
                .email("nikhil@example.com")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .userId(userId)
                .email("nikhil@example.com")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();

        authUserServiceImplementation = new AuthUserServiceImplementation(
                passwordEncoder,
                jwtUtil,
                userFeignClient,
                "internal-token"
        );
    }

    @Test
    void registerUserSuccessTest() {
        when(userFeignClient.createUser(registerRequestDTO)).thenReturn(userResponseDTO);
        when(jwtUtil.generateToken(registerRequestDTO.getEmail(), Role.ROLE_USER, userId)).thenReturn("jwt-token");

        AuthResponseDTO result = authUserServiceImplementation.registerUser(registerRequestDTO);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(userId, result.getUserId());
        verify(userFeignClient, times(1)).createUser(registerRequestDTO);
        verify(jwtUtil, times(1)).generateToken(registerRequestDTO.getEmail(), Role.ROLE_USER, userId);
        verifyNoMoreInteractions(userFeignClient, jwtUtil, passwordEncoder);
    }

    @Test
    void registerUserAlreadyExistsTest() {
        when(userFeignClient.createUser(registerRequestDTO)).thenThrow(feignConflict());

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authUserServiceImplementation.registerUser(registerRequestDTO)
        );

        assertEquals("User already exists!", exception.getMessage());
        verify(userFeignClient, times(1)).createUser(registerRequestDTO);
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), any(UUID.class));
        verifyNoMoreInteractions(userFeignClient, jwtUtil, passwordEncoder);
    }

    @Test
    void registerUserServiceUnavailableTest() {
        when(userFeignClient.createUser(registerRequestDTO)).thenThrow(feignServiceUnavailable());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authUserServiceImplementation.registerUser(registerRequestDTO)
        );

        assertEquals("User service is unavailable. Registration failed.", exception.getMessage());
        verify(userFeignClient, times(1)).createUser(registerRequestDTO);
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), any(UUID.class));
        verifyNoMoreInteractions(userFeignClient, jwtUtil, passwordEncoder);
    }

    @Test
    void loginUserSuccessTest() {
        when(userFeignClient.getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token")).thenReturn(userAuthResponseDTO);
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(userAuthResponseDTO.getEmail(), userAuthResponseDTO.getRole(), userId))
                .thenReturn("jwt-token");

        AuthResponseDTO result = authUserServiceImplementation.loginUser(loginRequestDTO);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(userId, result.getUserId());
        verify(userFeignClient, times(1)).getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token");
        verify(passwordEncoder, times(1)).matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword());
        verify(jwtUtil, times(1)).generateToken(userAuthResponseDTO.getEmail(), userAuthResponseDTO.getRole(), userId);
        verifyNoMoreInteractions(userFeignClient, passwordEncoder, jwtUtil);
    }

    @Test
    void loginUserUsesDefaultRoleWhenMissing() {
        UserAuthResponseDTO authResponseWithoutRole = UserAuthResponseDTO.builder()
                .userId(userId)
                .email("nikhil@example.com")
                .password("encoded-password")
                .build();

        when(userFeignClient.getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token")).thenReturn(authResponseWithoutRole);
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), authResponseWithoutRole.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(authResponseWithoutRole.getEmail(), Role.ROLE_USER, userId)).thenReturn("jwt-token");

        AuthResponseDTO result = authUserServiceImplementation.loginUser(loginRequestDTO);

        assertEquals("jwt-token", result.getToken());
        assertEquals(userId, result.getUserId());
        verify(jwtUtil, times(1)).generateToken(authResponseWithoutRole.getEmail(), Role.ROLE_USER, userId);
    }

    @Test
    void loginUserNotFoundTest() {
        when(userFeignClient.getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token")).thenThrow(feignNotFound());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authUserServiceImplementation.loginUser(loginRequestDTO)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(userFeignClient, times(1)).getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), any(UUID.class));
        verifyNoMoreInteractions(userFeignClient, passwordEncoder, jwtUtil);
    }

    @Test
    void loginUserInvalidPasswordTest() {
        when(userFeignClient.getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token")).thenReturn(userAuthResponseDTO);
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword())).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authUserServiceImplementation.loginUser(loginRequestDTO)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(userFeignClient, times(1)).getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token");
        verify(passwordEncoder, times(1)).matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), any(UUID.class));
        verifyNoMoreInteractions(userFeignClient, passwordEncoder, jwtUtil);
    }

    @Test
    void loginUserServiceUnavailableTest() {
        when(userFeignClient.getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token")).thenThrow(feignServiceUnavailable());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authUserServiceImplementation.loginUser(loginRequestDTO)
        );

        assertEquals("User service is unavailable. Login failed.", exception.getMessage());
        verify(userFeignClient, times(1)).getUserAuthByEmail(loginRequestDTO.getEmail(), "internal-token");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any(Role.class), any(UUID.class));
        verifyNoMoreInteractions(userFeignClient, passwordEncoder, jwtUtil);
    }

    private FeignException.NotFound feignNotFound() {
        return new FeignException.NotFound(
                "not found",
                requestTemplate(),
                new byte[0],
                null
        );
    }

    private FeignException.Conflict feignConflict() {
        return new FeignException.Conflict(
                "conflict",
                requestTemplate(),
                new byte[0],
                null
        );
    }

    private FeignException feignServiceUnavailable() {
        return FeignException.errorStatus("user-service", feign.Response.builder()
                .status(503)
                .reason("Service Unavailable")
                .request(requestTemplate())
                .headers(java.util.Map.of())
                .body(new byte[0])
                .build());
    }

    private feign.Request requestTemplate() {
        return feign.Request.create(
                feign.Request.HttpMethod.GET,
                "/users",
                java.util.Map.of(),
                new byte[0],
                StandardCharsets.UTF_8,
                null
        );
    }
}
