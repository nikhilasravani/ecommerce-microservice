package com.microservices.ecommerce.user.controller;

import com.microservices.ecommerce.user.service.UserService;
import com.microservices.ecommerce.user.userDTO.UserAuthResponseDTO;
import com.microservices.ecommerce.user.userDTO.UserRequestDTO;
import com.microservices.ecommerce.user.userDTO.UserResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final String authInternalToken;
    private final String cartInternalToken;

    public UserController(UserService userService,
                          @Value("${services.authentication.internal-token}") String authInternalToken,
                          @Value("${services.cart.internal-token}") String cartInternalToken) {
        this.userService = userService;
        this.authInternalToken = authInternalToken;
        this.cartInternalToken = cartInternalToken;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO userRequestDTO){
        UserResponseDTO newUser = userService.createUser(userRequestDTO);
        return new ResponseEntity<>(newUser,HttpStatus.CREATED);
    }

    @GetMapping("/auth")
    public ResponseEntity<UserAuthResponseDTO> getUserAuthByEmail(@RequestParam String email,
                                                                  @RequestHeader("X-Internal-Token") String requestToken) {
        if (!authInternalToken.equals(requestToken)) {
            throw new AccessDeniedException("Invalid internal authentication token.");
        }
        UserAuthResponseDTO userAuth = userService.getUserAuthByEmail(email);
        return new ResponseEntity<>(userAuth, HttpStatus.OK);
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> allUsers = userService.getAllUsers();
        return new ResponseEntity<>(allUsers,HttpStatus.OK);
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId,
                                                       @RequestHeader(value = "X-Internal-Token", required = false)
                                                       String requestToken){
        UserResponseDTO userById = cartInternalToken.equals(requestToken)
                ? userService.getUserByIdInternal(userId)
                : userService.getUserById(userId);
        return new ResponseEntity<>(userById,HttpStatus.OK);
    }

    @GetMapping("/internal/{userId}")
    public ResponseEntity<UserResponseDTO> getUserByIdInternal(@PathVariable UUID userId) {
        UserResponseDTO userById = userService.getUserByIdInternal(userId);
        return new ResponseEntity<>(userById, HttpStatus.OK);
    }

    @PutMapping("/{userId}/make-admin")
    public ResponseEntity<UserResponseDTO> makeUserAdmin(@PathVariable UUID userId) {
        UserResponseDTO updatedUser = userService.makeUserAdmin(userId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

}
