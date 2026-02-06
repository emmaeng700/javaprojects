package com.example.ldapauth.controller;

import com.example.ldapauth.model.User;
import com.example.ldapauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/status")
    public Map<String, Object> status(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        
        if (auth != null && auth.isAuthenticated()) {
            User user = userService.getUserFromAuthentication(auth);
            response.put("authenticated", true);
            response.put("user", user);
            response.put("message", "User is authenticated");
        } else {
            response.put("authenticated", false);
            response.put("message", "User is not authenticated");
        }
        
        return response;
    }

    @PostMapping("/logout")
    public Map<String, String> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Logged out successfully");
        return response;
    }
}