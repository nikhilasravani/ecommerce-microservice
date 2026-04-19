package com.microservices.ecommerce.authentication.controller;

import com.microservices.ecommerce.authentication.dto.AuthResponseDTO;
import com.microservices.ecommerce.authentication.dto.LoginRequestDTO;
import com.microservices.ecommerce.authentication.dto.RegisterRequestDTO;
import com.microservices.ecommerce.authentication.service.AuthUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUserService authUserService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registration(@Valid @RequestBody
                                                            RegisterRequestDTO registerRequestDTO
    , @RequestHeader(value="X-Gateway-Source", required=false) String gatewaySource) {
        AuthResponseDTO response = authUserService.registerUser(registerRequestDTO);
        log.info("X-Gateway-Source: {}", gatewaySource);
        return new  ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody
                                                     LoginRequestDTO loginRequestDTO,
                                                 @RequestHeader(value="X-Gateway-Source", required=false) String gatewaySource) {
        log.info("X-Gateway-Source: {}", gatewaySource);
        AuthResponseDTO response = authUserService.loginUser(loginRequestDTO);
        return new  ResponseEntity<>(response,HttpStatus.OK);
    }


}
