package com.microservices.ecommerce.user.service;

import com.microservices.ecommerce.user.constants.UserTestConstants;
import com.microservices.ecommerce.user.exception.UserNotFoundException;
import com.microservices.ecommerce.user.model.User;
import com.microservices.ecommerce.user.repository.UserRepository;
import com.microservices.ecommerce.user.userDTO.UserAuthResponseDTO;
import com.microservices.ecommerce.user.userDTO.UserRequestDTO;
import com.microservices.ecommerce.user.userDTO.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplementationTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImplementation userServiceImplementation;

    private UserRequestDTO userRequestDTO;
    private User userEntity;
    private UserResponseDTO userResponseDTO;
    private UserAuthResponseDTO userAuthResponseDTO;
    private User saved;

    @Mock
    private PasswordEncoder passwordEncoder;
    //Arrange
    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        UserTestConstants.EMAIL,
                        null,
                        List.of()
                )
        );

        userRequestDTO = UserRequestDTO.builder()
                .username(UserTestConstants.USERNAME)
                .email(UserTestConstants.EMAIL)
                .password(UserTestConstants.PASSWORD)
                .build();

        userEntity = User.builder()
                .username(UserTestConstants.USERNAME)
                .email(UserTestConstants.EMAIL)
                .build();

        saved = User.builder()
                .userId(UserTestConstants.USER_ID)
                .username(UserTestConstants.USERNAME)
                .email(UserTestConstants.EMAIL)
                .build();

        userResponseDTO = UserResponseDTO.builder()
                .userId(UserTestConstants.USER_ID)
                .username(UserTestConstants.USERNAME)
                .email(UserTestConstants.EMAIL)
                .build();

        userAuthResponseDTO = UserAuthResponseDTO.builder()
                .email(UserTestConstants.EMAIL)
                .password("encodedPassword")
                .build();
    }

    @Test
    public void createUserTest() {
        when(userRepository.existsByEmail(UserTestConstants.EMAIL)).thenReturn(false);
        when(modelMapper.map(userRequestDTO, User.class)).thenReturn(userEntity);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(saved);
        when(modelMapper.map(saved, UserResponseDTO.class)).thenReturn(userResponseDTO);

        //Act
        UserResponseDTO result = userServiceImplementation.createUser(userRequestDTO);

        //Assert
        assertNotNull(result);
        assertEquals(UserTestConstants.USER_ID,result.getUserId());
        assertEquals(UserTestConstants.USERNAME,result.getUsername());
        assertEquals(UserTestConstants.EMAIL,result.getEmail());

        //verify
        verify(userRepository, times(1)).existsByEmail(UserTestConstants.EMAIL);
        verify(modelMapper, times(1)).map(userRequestDTO, User.class);
        verify(passwordEncoder, times(1)).encode(UserTestConstants.PASSWORD);
        verify(userRepository,times(1)).save(userEntity);
        verify(modelMapper, times(1)).map(saved, UserResponseDTO.class);
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);
    }

    @Test
    public void createUserAlreadyExistsTest() {
        when(userRepository.existsByEmail(UserTestConstants.EMAIL)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userServiceImplementation.createUser(userRequestDTO));

        assertEquals("User already exists!", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(UserTestConstants.EMAIL);
        verify(userRepository, never()).save(any());
        verify(modelMapper, never()).map(userRequestDTO, User.class);
        verify(passwordEncoder, never()).encode(anyString());
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);
    }

    @Test
    public void getUserByIdValidTest() {
        when(userRepository.findById(UserTestConstants.USER_ID)).thenReturn(Optional.of(saved));
        when(modelMapper.map(saved, UserResponseDTO.class)).thenReturn(userResponseDTO);
        UserResponseDTO result = userServiceImplementation.getUserById(UserTestConstants.USER_ID);

        assertNotNull(result);
        assertEquals(UserTestConstants.USER_ID,result.getUserId());
    }

    @Test
    public void getUserByInvalidIdTest() {
        when(userRepository.findById(UserTestConstants.USER_ID)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userServiceImplementation.getUserById(UserTestConstants.USER_ID);
        });

        assertEquals("User not found with ID : "+UserTestConstants.USER_ID,
                exception.getMessage());
        verify(userRepository,times(1)).findById(UserTestConstants.USER_ID);
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getUserAuthByEmailValidTest() {
        when(userRepository.findByEmail(UserTestConstants.EMAIL)).thenReturn(Optional.of(saved));
        when(modelMapper.map(saved, UserAuthResponseDTO.class)).thenReturn(userAuthResponseDTO);

        UserAuthResponseDTO result = userServiceImplementation.getUserAuthByEmail(UserTestConstants.EMAIL);

        assertNotNull(result);
        assertEquals(UserTestConstants.EMAIL, result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository, times(1)).findByEmail(UserTestConstants.EMAIL);
        verify(modelMapper, times(1)).map(saved, UserAuthResponseDTO.class);
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);
    }

    @Test
    public void getUserAuthByEmailInvalidTest() {
        when(userRepository.findByEmail(UserTestConstants.EMAIL)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userServiceImplementation.getUserAuthByEmail(UserTestConstants.EMAIL));

        assertEquals("User not found with email : " + UserTestConstants.EMAIL, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(UserTestConstants.EMAIL);
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);
    }

    @Test
    public void getAllUsersValidListTest() {
        when(userRepository.findAll()).thenReturn(List.of(saved));
        when(modelMapper.map(saved, UserResponseDTO.class)).thenReturn(userResponseDTO);
        List<UserResponseDTO> result = userServiceImplementation.getAllUsers();
        assertNotNull(result);
        assertEquals(UserTestConstants.USER_ID,result.getFirst().getUserId());
        assertEquals(UserTestConstants.USERNAME,result.getFirst().getUsername());
        assertEquals(UserTestConstants.EMAIL,result.getFirst().getEmail());

        assertEquals(1, result.size());

        verify(userRepository,times(1)).findAll();
        verify(modelMapper, times(1)).map(saved, UserResponseDTO.class);
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);

    }

    @Test
    public void getAllUsersInvalidListTest() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> result = userServiceImplementation.getAllUsers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository,times(1)).findAll();
        verify(modelMapper, never()).map(any(), any());
        verifyNoMoreInteractions(userRepository,modelMapper,passwordEncoder);
    }
}
