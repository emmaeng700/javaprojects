package com.example.demo.repository;

import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    
    @Test
    void shouldSaveUser() {
        // Given
        User user = new User("Alice", "alice@test.com");
        
        // When
        User saved = userRepository.save(user);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getEmail()).isEqualTo("alice@test.com");
    }
    
    @Test
    void shouldFindAllUsers() {
        // Given
        userRepository.save(new User("Alice", "alice@test.com"));
        userRepository.save(new User("Bob", "bob@test.com"));
        
        // When
        List<User> users = userRepository.findAll();
        
        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                         .containsExactlyInAnyOrder("Alice", "Bob");
    }
    
    @Test
    void shouldFindUserById() {
        // Given
        User user = userRepository.save(new User("Charlie", "charlie@test.com"));
        
        // When
        Optional<User> found = userRepository.findById(user.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Charlie");
    }
    
    @Test
    void shouldDeleteUser() {
        // Given
        User user = userRepository.save(new User("Delete Me", "delete@test.com"));
        Long userId = user.getId();
        
        // When
        userRepository.deleteById(userId);
        
        // Then
        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }
}
