package com.example.ldapauth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.ldapauth.model.User;
import com.example.ldapauth.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public Map<String, Object> home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to LDAP Auth System!");
        response.put("user", auth.getName());
        response.put("authenticated", auth.isAuthenticated());
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    @GetMapping("/api/user")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserFromAuthentication(auth);
    }

    @GetMapping("/api/public/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "LDAP Auth API is running");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    @GetMapping("/api/dashboard")
    public Map<String, Object> dashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserFromAuthentication(auth);
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("message", "Welcome to your dashboard, " + user.fullName() + "!");
        response.put("stats", Map.of(
            "totalUsers", 4,
            "activeGroups", 2,
            "loginTime", System.currentTimeMillis()
        ));
        
        return response;
    }
}