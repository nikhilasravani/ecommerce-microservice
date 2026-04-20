package com.microservices.ecommerce.user.service;

import com.microservices.ecommerce.user.userDTO.UserRequestDTO;
import com.microservices.ecommerce.user.userDTO.UserAuthResponseDTO;
import com.microservices.ecommerce.user.userDTO.UserResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(UUID userId);

    UserResponseDTO getUserByIdInternal(UUID userId);

    UserAuthResponseDTO getUserAuthByEmail(String email);

    UserResponseDTO makeUserAdmin(UUID userId);
}
