package com.lirium.nutrition.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @InjectMocks private UserDetailsServiceImpl service;

  @Mock private UserRepository userRepository;

  @Test
  void shouldReturnUserWhenEmailExists() {

    User user = new User();
    user.setEmail("test@mail.com");

    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

    UserDetails result = service.loadUserByUsername("test@mail.com");

    assertEquals(user, result);

    verify(userRepository).findByEmail("test@mail.com");
  }

  @Test
  void shouldThrowExceptionWhenEmailDoesNotExist() {

    when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

    UsernameNotFoundException exception =
        assertThrows(
            UsernameNotFoundException.class, () -> service.loadUserByUsername("test@mail.com"));

    assertEquals("User not found: test@mail.com", exception.getMessage());
  }
}
