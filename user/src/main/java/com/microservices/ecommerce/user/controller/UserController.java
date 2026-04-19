package com.microservices.ecommerce.user.controller;

import com.microservices.ecommerce.user.service.UserService;
import com.microservices.ecommerce.user.userDTO.UserAuthResponseDTO;
import com.microservices.ecommerce.user.userDTO.UserRequestDTO;
import com.microservices.ecommerce.user.userDTO.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO userRequestDTO){
        UserResponseDTO newUser = userService.createUser(userRequestDTO);
        return new ResponseEntity<>(newUser,HttpStatus.CREATED);
    }

    @GetMapping("/auth")
    public ResponseEntity<UserAuthResponseDTO> getUserAuthByEmail(@RequestParam String email) {
        UserAuthResponseDTO userAuth = userService.getUserAuthByEmail(email);
        return new ResponseEntity<>(userAuth, HttpStatus.OK);
    }

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        List<UserResponseDTO> allUsers = userService.getAllUsers();
        return new ResponseEntity<>(allUsers,HttpStatus.OK);
    }

    @GetMapping("/getUserById/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID userId){
        UserResponseDTO userById = userService.getUserById(userId);
        return new ResponseEntity<>(userById,HttpStatus.OK);
    }

    @PutMapping("/{userId}/make-admin")
    public ResponseEntity<UserResponseDTO> makeUserAdmin(@PathVariable UUID userId) {
        UserResponseDTO updatedUser = userService.makeUserAdmin(userId);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

}
