package com.microservices.ecommerce.user.service;

import com.microservices.ecommerce.user.exception.UserNotFoundException;
import com.microservices.ecommerce.user.appConfig.Role;
import com.microservices.ecommerce.user.model.User;
import com.microservices.ecommerce.user.repository.UserRepository;
import com.microservices.ecommerce.user.userDTO.UserAuthResponseDTO;
import com.microservices.ecommerce.user.userDTO.UserRequestDTO;
import com.microservices.ecommerce.user.userDTO.UserResponseDTO;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImplementation(UserRepository userRepository, ModelMapper modelMapper,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new IllegalArgumentException("User already exists!");
        }

        //DTO -> Entity
        User userEntity = modelMapper.map(userRequestDTO, User.class);
        userEntity.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        userEntity.setRole(Role.ROLE_USER);

        //Save
        User saved = userRepository.save(userEntity);

        //Entity -> DTO
        UserResponseDTO savedDTO = modelMapper.map(saved, UserResponseDTO.class);

        return savedDTO;
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> allUsers = userRepository.findAll();

        List<UserResponseDTO> allUsersDto = allUsers.stream().map(users ->{
            return modelMapper.map(users, UserResponseDTO.class);
        }).toList();

        return allUsersDto;
    }

    @Override
    public UserResponseDTO getUserById(UUID userId) {
        User userStatus = userRepository.findById(userId).
                orElseThrow(()-> new UserNotFoundException("User not found with ID : " +userId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.ROLE_ADMIN.name()));

        if (!isAdmin && !userStatus.getEmail().equals(loggedInEmail)) {
            throw new AccessDeniedException("You are not allowed to access this user.");
        }

        UserResponseDTO userDTO = modelMapper.map(userStatus, UserResponseDTO.class);
        return userDTO;
    }

    @Override
    public UserResponseDTO getUserByIdInternal(UUID userId) {
        User userStatus = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID : " + userId));
        return modelMapper.map(userStatus, UserResponseDTO.class);
    }

    @Override
    public UserAuthResponseDTO getUserAuthByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email : " + email));
        return modelMapper.map(user, UserAuthResponseDTO.class);
    }

    @Override
    public UserResponseDTO makeUserAdmin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID : " + userId));
        user.setRole(Role.ROLE_ADMIN);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserResponseDTO.class);
    }
}
