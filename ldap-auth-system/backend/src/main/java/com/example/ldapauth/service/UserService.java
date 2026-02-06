package com.example.ldapauth.service;

import com.example.ldapauth.model.User;
import com.example.ldapauth.model.UserEntity;
import com.example.ldapauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserEntity registerUser(String username, String password, String fullName, String email, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);
        UserEntity user = new UserEntity(username, encodedPassword, fullName, email, role);
        return userRepository.save(user);
    }

    public User getUserFromAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String username = auth.getName();
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);

        if (userEntity != null) {
            return new User(
                userEntity.getUsername(),
                userEntity.getFullName(),
                userEntity.getEmail(),
                "ROLE_" + userEntity.getRole()
            );
        }

        return null;
    }
}