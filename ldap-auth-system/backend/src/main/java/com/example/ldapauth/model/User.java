package com.example.ldapauth.model;

public record User(
    String username,
    String fullName,
    String email,
    String role
) {}