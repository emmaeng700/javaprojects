package com.example.ldapauth.model;

public record AuthResponse(
    boolean authenticated,
    String username,
    String message
) {}