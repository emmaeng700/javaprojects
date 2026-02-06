package com.example.ldapauth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void authStatus_withoutAuth_shouldReturnNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(false))
            .andExpect(jsonPath("$.user").isEmpty());
    }

    @Test
    @WithMockUser(username = "ben")
    public void authStatus_withAuth_shouldReturnAuthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.user.username").value("ben"));
    }

    @Test
    @WithMockUser(username = "joe")
    public void logout_shouldClearAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    @WithMockUser(username = "bob")
    public void getAllUsers_shouldReturnUserList() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(4))
            .andExpect(jsonPath("$.users").isArray());
    }
}