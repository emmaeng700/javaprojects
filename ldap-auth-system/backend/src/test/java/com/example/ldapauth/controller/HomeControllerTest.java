package com.example.ldapauth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void publicEndpoint_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/public/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.message").value("LDAP Auth API is running"));
    }

    @Test
    public void protectedEndpoint_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ben")
    public void protectedEndpoint_withAuth_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("ben"))
            .andExpect(jsonPath("$.fullName").value("Ben Alex"));
    }

    @Test
    @WithMockUser(username = "bob")
    public void home_withAuth_shouldReturnWelcome() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user").value("bob"))
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.message").value("Welcome to LDAP Auth System!"));
    }

    @Test
    @WithMockUser(username = "alice")
    public void dashboard_withAuth_shouldReturnData() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.username").value("alice"))
            .andExpect(jsonPath("$.message", containsString("Alice Johnson")))
            .andExpect(jsonPath("$.stats.totalUsers").value(4));
    }

    @Test
    public void dashboard_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
            .andExpect(status().isUnauthorized());
    }
}