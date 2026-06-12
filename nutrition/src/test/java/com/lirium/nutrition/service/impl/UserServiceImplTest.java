package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.CreatePatientRequestDTO;
import com.lirium.nutrition.dto.request.CreateUserRequestDTO;
import com.lirium.nutrition.dto.request.UserUpdateRequestDTO;
import com.lirium.nutrition.dto.response.UserResponseDTO;
import com.lirium.nutrition.exception.AccountDisabledException;
import com.lirium.nutrition.exception.EmailAlreadyExistsException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.UserMapper;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRegisterUser() {

        // Given
        CreateUserRequestDTO request = mock(CreateUserRequestDTO.class);
        User user = mock(User.class);
        User savedUser = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(request.email()).thenReturn("test@test.com");
        when(request.password()).thenReturn("123456");

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(passwordEncoder.encode("123456"))
                .thenReturn("hashed-password");

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.registerUser(request);

        // Then
        assertNotNull(result);

        verify(userRepository).existsByEmail("test@test.com");
        verify(userMapper).toEntity(request);
        verify(passwordEncoder).encode("123456");
        verify(user).setPasswordHash("hashed-password");
        verify(userRepository).save(user);
        verify(userMapper).toResponseDTO(savedUser);
    }

    @Test
    void shouldThrowWhenRegisterUserWithExistingEmail() {

        // Given
        CreateUserRequestDTO request = mock(CreateUserRequestDTO.class);

        when(request.email()).thenReturn("test@test.com");

        when(userRepository.existsByEmail("test@test.com"))
                .thenReturn(true);

        // When / Then
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        verify(userRepository).existsByEmail("test@test.com");

        verify(userMapper, never())
                .toEntity(any(CreateUserRequestDTO.class));

        verify(userRepository, never()).save(any());

        verifyNoInteractions(passwordEncoder);

    }

    @Test
    void shouldRegisterPatient() {

        // Given
        CreatePatientRequestDTO request = mock(CreatePatientRequestDTO.class);
        User user = mock(User.class);
        User savedUser = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(request.email()).thenReturn("patient@test.com");

        when(userRepository.existsByEmail("patient@test.com"))
                .thenReturn(false);

        when(userMapper.toEntity(request))
                .thenReturn(user);

        when(userRepository.save(user))
                .thenReturn(savedUser);

        when(userMapper.toResponseDTO(savedUser))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.registerPatient(request);

        // Then
        assertNotNull(result);

        verify(userRepository).existsByEmail("patient@test.com");
        verify(userMapper).toEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponseDTO(savedUser);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowWhenRegisterPatientWithExistingEmail() {

        // Given
        CreatePatientRequestDTO request = mock(CreatePatientRequestDTO.class);

        when(request.email()).thenReturn("patient@test.com");

        when(userRepository.existsByEmail("patient@test.com"))
                .thenReturn(true);

        // When / Then
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerPatient(request)
        );

        verify(userRepository).existsByEmail("patient@test.com");

        verify(userMapper, never())
                .toEntity(any(CreatePatientRequestDTO.class));

        verify(userRepository, never()).save(any());

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldFindUserById() {

        // Given
        User user = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.findById(1L);

        // Then
        assertNotNull(result);

        verify(userRepository).findById(1L);
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldThrowWhenUserNotFoundById() {

        // Given
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findById(1L)
        );

        verify(userRepository).findById(1L);

        verify(userMapper, never()).toResponseDTO(any(User.class));
    }

    @Test
    void shouldFindUserByEmail() {

        // Given
        User user = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.findByEmail("test@test.com");

        // Then
        assertNotNull(result);

        verify(userRepository).findByEmail("test@test.com");
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldThrowWhenUserNotFoundByEmail() {

        // Given
        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findByEmail("test@test.com")
        );

        verify(userRepository).findByEmail("test@test.com");

        verify(userMapper, never()).toResponseDTO(any(User.class));
    }

    @Test
    void shouldReturnAllUsers() {

        // Given
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        UserResponseDTO dto1 = mock(UserResponseDTO.class);
        UserResponseDTO dto2 = mock(UserResponseDTO.class);

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        when(userMapper.toResponseDTO(user1))
                .thenReturn(dto1);

        when(userMapper.toResponseDTO(user2))
                .thenReturn(dto2);

        // When
        List<UserResponseDTO> result = userService.findAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));

        verify(userRepository).findAll();
        verify(userMapper).toResponseDTO(user1);
        verify(userMapper).toResponseDTO(user2);
        verify(userMapper, times(2)).toResponseDTO(any(User.class));
    }

    @Test
    void shouldUpdateBasicInfo() {

        // Given
        User user = mock(User.class);
        UserUpdateRequestDTO request = mock(UserUpdateRequestDTO.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        // When
        UserResponseDTO result =
                userService.updateBasicInfo(1L, request);

        // Then
        assertNotNull(result);

        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromDTO(request, user);
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingUser() {

        // Given
        UserUpdateRequestDTO request = mock(UserUpdateRequestDTO.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateBasicInfo(1L, request)
        );

        verify(userRepository).findById(1L);

        verify(userMapper, never())
                .updateUserFromDTO(any(UserUpdateRequestDTO.class), any(User.class));

        verify(userMapper, never())
                .toResponseDTO(any(User.class));
    }

    @Test
    void shouldEnableUser() {

        // Given
        User user = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.setEnabled(1L, true);

        // Then
        assertNotNull(result);

        verify(userRepository).findById(1L);
        verify(user).setEnabled(true);
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldDisableUserWithSetEnabled() {

        // Given
        User user = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.setEnabled(1L, false);

        // Then
        assertNotNull(result);

        verify(userRepository).findById(1L);
        verify(user).setEnabled(false);
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldValidateEmail() {

        // Given
        User user = mock(User.class);
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponseDTO(user))
                .thenReturn(response);

        // When
        UserResponseDTO result = userService.validateEmail(1L);

        // Then
        assertNotNull(result);

        verify(userRepository).findById(1L);
        verify(user).setEmailValidated(true);
        verify(userMapper).toResponseDTO(user);
    }

    @Test
    void shouldDeleteUserByDisablingIt() {

        // Given
        User user = mock(User.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(user.isEnabled())
                .thenReturn(true);

        // When
        userService.deleteById(1L);

        // Then
        verify(userRepository).findById(1L);
        verify(user).isEnabled();
        verify(user).setEnabled(false);
    }

    @Test
    void shouldThrowWhenDeletingAlreadyDisabledUser() {

        // Given
        User user = mock(User.class);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(user.isEnabled())
                .thenReturn(false);

        // When / Then
        assertThrows(
                AccountDisabledException.class,
                () -> userService.deleteById(1L)
        );

        verify(userRepository).findById(1L);
        verify(user).isEnabled();

        verify(user, never()).setEnabled(false);
    }

}