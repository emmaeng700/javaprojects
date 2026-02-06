package com.example.ldapauth.controller;

import com.example.ldapauth.model.RegisterRequest;
import com.example.ldapauth.model.UserEntity;
import com.example.ldapauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (request.username() == null || request.username().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.password() == null || request.password().length() < 6) {
                response.put("success", false);
                response.put("message", "Password must be at least 6 characters");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.role() == null || request.role().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Role is required");
                return ResponseEntity.badRequest().body(response);
            }

            UserEntity user = userService.registerUser(
                request.username(),
                request.password(),
                request.fullName(),
                request.email(),
                request.role()
            );

            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}