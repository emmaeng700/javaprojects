package com.example.ldapauth.service;

import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import com.example.ldapauth.model.User;

import java.util.stream.Collectors;

@Service
public class UserService {

    public User getUserFromAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));

        return new User(
            auth.getName(),
            getFullName(auth.getName()),
            auth.getName() + "@springframework.org",
            roles.isEmpty() ? "USER" : roles
        );
    }

    private String getFullName(String username) {
        // In production, fetch from LDAP
        return switch (username) {
            case "ben" -> "Ben Alex";
            case "bob" -> "Bob Hamilton";
            case "joe" -> "Joe Smeth";
            case "alice" -> "Alice Johnson";
            default -> username;
        };
    }
}