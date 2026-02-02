package com.example.consumer.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConsumerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void allEndpoints_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/quote/random")).andExpect(status().isOk());
        mockMvc.perform(get("/api/quote/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/status")).andExpect(status().isOk());
    }
}