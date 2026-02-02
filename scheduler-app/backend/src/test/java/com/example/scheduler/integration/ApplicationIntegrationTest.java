package com.example.scheduler.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Tests that the application context loads successfully
    }

    @Test
    void apiStatus_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("running")));
    }

    @Test
    void apiTasks_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray());
    }

    @Test
    void apiHealth_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void allEndpoints_shouldReturnJson() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
        
        mockMvc.perform(get("/api/tasks"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
        
        mockMvc.perform(get("/api/health"))
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}