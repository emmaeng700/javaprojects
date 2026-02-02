package com.example.quoters.integration;

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
class QuotersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void allEndpoints_shouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/random")).andExpect(status().isOk());
        mockMvc.perform(get("/api/")).andExpect(status().isOk());
        mockMvc.perform(get("/api/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/count")).andExpect(status().isOk());
    }

    @Test
    void allQuotes_shouldHaveSequentialIds() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].value.id", is(1)))
                .andExpect(jsonPath("$[9].value.id", is(10)));
    }
}