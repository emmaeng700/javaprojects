package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "test@example.com");
        testUser.setId(1L);
    }
    
    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Given
        User user1 = new User("Alice", "alice@test.com");
        User user2 = new User("Bob", "bob@test.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        
        // When
        List<User> users = userService.getAllUsers();
        
        // Then
        assertThat(users).hasSize(2);
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    void createUser_withValidData_shouldSaveUser() {
        // Given
        User newUser = new User("New User", "new@test.com");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        
        // When
        User saved = userService.createUser(newUser);
        
        // Then
        assertThat(saved.getName()).isEqualTo("New User");
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void createUser_withEmptyName_shouldThrowException() {
        // Given
        User invalidUser = new User("", "test@test.com");
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(invalidUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User name cannot be empty");
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void createUser_withInvalidEmail_shouldThrowException() {
        // Given
        User invalidUser = new User("Test", "invalidemail");
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(invalidUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email address");
        
        verify(userRepository, never()).save(any(User.class));
    }
}
