package com.example.quoters.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void random_shouldReturnRandomQuote() throws Exception {
        mockMvc.perform(get("/api/random"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.type", is("success")))
                .andExpect(jsonPath("$.value.id", notNullValue()))
                .andExpect(jsonPath("$.value.quote", notNullValue()));
    }

    @Test
    void all_shouldReturnAllQuotes() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].type", is("success")));
    }

    @Test
    void getById_withValidId_shouldReturnSpecificQuote() throws Exception {
        mockMvc.perform(get("/api/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("success")))
                .andExpect(jsonPath("$.value.id", is(1)))
                .andExpect(jsonPath("$.value.quote", containsString("pair-programming")));
    }

    @Test
    void getById_withInvalidId_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void count_shouldReturnCorrectCount() throws Exception {
        mockMvc.perform(get("/api/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}