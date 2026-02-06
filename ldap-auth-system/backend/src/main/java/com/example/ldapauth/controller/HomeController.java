package com.example.ldapauth.controller;

import com.example.ldapauth.model.User;
import com.example.ldapauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public Map<String, Object> home(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to LDAP Auth System!");
        response.put("authenticated", auth != null && auth.isAuthenticated());
        response.put("user", auth != null ? auth.getName() : null);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/api/user")
    public User getUser(Authentication auth) {
        return userService.getUserFromAuthentication(auth);
    }

    @GetMapping("/api/dashboard")
    public Map<String, Object> dashboard(Authentication auth) {
        User user = userService.getUserFromAuthentication(auth);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", 4);
        stats.put("activeGroups", 2);
        stats.put("loginTime", System.currentTimeMillis());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to your dashboard, " + user.fullName() + "!");
        response.put("user", user);
        response.put("stats", stats);
        
        return response;
    }

    @GetMapping("/api/public/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "LDAP Auth API is running");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}