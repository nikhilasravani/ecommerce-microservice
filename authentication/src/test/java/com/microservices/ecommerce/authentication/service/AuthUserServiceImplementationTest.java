package com.microservices.ecommerce.authentication.service;

import com.microservices.ecommerce.authentication.dto.AuthResponseDTO;
import com.microservices.ecommerce.authentication.dto.LoginRequestDTO;
import com.microservices.ecommerce.authentication.dto.RegisterRequestDTO;
import com.microservices.ecommerce.authentication.dto.UserAuthResponseDTO;
import com.microservices.ecommerce.authentication.exception.UserAlreadyExistsException;
import com.microservices.ecommerce.authentication.jwt.JwtUtil;
import com.microservices.ecommerce.authentication.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthUserServiceImplementation authUserServiceImplementation;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private UserAuthResponseDTO userAuthResponseDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        registerRequestDTO = RegisterRequestDTO.builder()
                .username("nikhil")
                .email("nikhil@example.com")
                .password("password123")
                .role(Role.ROLE_USER)
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
    }

    @Test
    void registerUserSuccessTest() {
        when(restTemplate.postForObject(
                eq("http://localhost:8081/users"),
                eq(registerRequestDTO),
                eq(UserAuthResponseDTO.class)
        )).thenReturn(userAuthResponseDTO);
        when(jwtUtil.generateToken(registerRequestDTO.getEmail(), registerRequestDTO.getRole())).thenReturn("jwt-token");

        AuthResponseDTO result = authUserServiceImplementation.registerUser(registerRequestDTO);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(userId, result.getUserId());
        verify(restTemplate, times(1)).postForObject(
                "http://localhost:8081/users",
                registerRequestDTO,
                UserAuthResponseDTO.class
        );
        verify(jwtUtil, times(1)).generateToken(registerRequestDTO.getEmail(), registerRequestDTO.getRole());
        verifyNoMoreInteractions(restTemplate, jwtUtil, passwordEncoder);
    }

    @Test
    void registerUserAlreadyExistsTest() {
        when(restTemplate.postForObject(
                eq("http://localhost:8081/users"),
                eq(registerRequestDTO),
                eq(UserAuthResponseDTO.class)
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], null
        ));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authUserServiceImplementation.registerUser(registerRequestDTO)
        );

        assertEquals("User already exists!", exception.getMessage());
        verify(restTemplate, times(1)).postForObject(
                "http://localhost:8081/users",
                registerRequestDTO,
                UserAuthResponseDTO.class
        );
        verify(jwtUtil, never()).generateToken(anyString(), any());
        verifyNoMoreInteractions(restTemplate, jwtUtil, passwordEncoder);
    }

    @Test
    void registerUserServiceUnavailableTest() {
        when(restTemplate.postForObject(
                eq("http://localhost:8081/users"),
                eq(registerRequestDTO),
                eq(UserAuthResponseDTO.class)
        )).thenThrow(new RestClientException("connection refused"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authUserServiceImplementation.registerUser(registerRequestDTO)
        );

        assertEquals("User service is unavailable. Registration failed.", exception.getMessage());
        verify(restTemplate, times(1)).postForObject(
                "http://localhost:8081/users",
                registerRequestDTO,
                UserAuthResponseDTO.class
        );
        verify(jwtUtil, never()).generateToken(anyString(), any());
        verifyNoMoreInteractions(restTemplate, jwtUtil, passwordEncoder);
    }

    @Test
    void loginUserSuccessTest() {
        when(restTemplate.getForObject(
                eq("http://localhost:8081/users/auth?email={email}"),
                eq(UserAuthResponseDTO.class),
                eq(loginRequestDTO.getEmail())
        )).thenReturn(userAuthResponseDTO);
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(userAuthResponseDTO.getEmail(), userAuthResponseDTO.getRole())).thenReturn("jwt-token");

        AuthResponseDTO result = authUserServiceImplementation.loginUser(loginRequestDTO);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(userId, result.getUserId());
        verify(restTemplate, times(1)).getForObject(
                "http://localhost:8081/users/auth?email={email}",
                UserAuthResponseDTO.class,
                loginRequestDTO.getEmail()
        );
        verify(passwordEncoder, times(1)).matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword());
        verify(jwtUtil, times(1)).generateToken(userAuthResponseDTO.getEmail(), userAuthResponseDTO.getRole());
        verifyNoMoreInteractions(restTemplate, passwordEncoder, jwtUtil);
    }

    @Test
    void loginUserNotFoundTest() {
        when(restTemplate.getForObject(
                eq("http://localhost:8081/users/auth?email={email}"),
                eq(UserAuthResponseDTO.class),
                eq(loginRequestDTO.getEmail())
        )).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null
        ));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authUserServiceImplementation.loginUser(loginRequestDTO)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(restTemplate, times(1)).getForObject(
                "http://localhost:8081/users/auth?email={email}",
                UserAuthResponseDTO.class,
                loginRequestDTO.getEmail()
        );
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any());
        verifyNoMoreInteractions(restTemplate, passwordEncoder, jwtUtil);
    }

    @Test
    void loginUserInvalidPasswordTest() {
        when(restTemplate.getForObject(
                eq("http://localhost:8081/users/auth?email={email}"),
                eq(UserAuthResponseDTO.class),
                eq(loginRequestDTO.getEmail())
        )).thenReturn(userAuthResponseDTO);
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword())).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authUserServiceImplementation.loginUser(loginRequestDTO)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(restTemplate, times(1)).getForObject(
                "http://localhost:8081/users/auth?email={email}",
                UserAuthResponseDTO.class,
                loginRequestDTO.getEmail()
        );
        verify(passwordEncoder, times(1)).matches(loginRequestDTO.getPassword(), userAuthResponseDTO.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), any());
        verifyNoMoreInteractions(restTemplate, passwordEncoder, jwtUtil);
    }

    @Test
    void loginUserServiceUnavailableTest() {
        when(restTemplate.getForObject(
                eq("http://localhost:8081/users/auth?email={email}"),
                eq(UserAuthResponseDTO.class),
                eq(loginRequestDTO.getEmail())
        )).thenThrow(new RestClientException("connection refused"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> authUserServiceImplementation.loginUser(loginRequestDTO)
        );

        assertEquals("User service is unavailable. Login failed.", exception.getMessage());
        verify(restTemplate, times(1)).getForObject(
                "http://localhost:8081/users/auth?email={email}",
                UserAuthResponseDTO.class,
                loginRequestDTO.getEmail()
        );
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), any());
        verifyNoMoreInteractions(restTemplate, passwordEncoder, jwtUtil);
    }
}
