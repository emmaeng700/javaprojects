package com.example.ldapauth.model;

public record RegisterRequest(
    String username,
    String password,
    String fullName,
    String email,
    String role
) {}