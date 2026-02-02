package com.example.consumer.controller;

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
class ConsumerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRandomQuote_shouldReturnQuote() throws Exception {
        mockMvc.perform(get("/api/quote/random"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.type", is("success")))
                .andExpect(jsonPath("$.value.id", notNullValue()))
                .andExpect(jsonPath("$.value.quote", notNullValue()));
    }

    @Test
    void getQuoteById_shouldReturnSpecificQuote() throws Exception {
        mockMvc.perform(get("/api/quote/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("success")))
                .andExpect(jsonPath("$.value.id", is(1)));
    }

    @Test
    void getStatus_shouldReturnConsumerStatus() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service", is("REST Consumer")))
                .andExpect(jsonPath("$.status", is("running")))
                .andExpect(jsonPath("$.quotersStatus", notNullValue()));
    }
}