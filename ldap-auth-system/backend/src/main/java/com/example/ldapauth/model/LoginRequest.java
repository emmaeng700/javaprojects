package com.example.ldapauth.model;

public record LoginRequest(
    String username,
    String password
) {}