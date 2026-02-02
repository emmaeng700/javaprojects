package com.example.scheduler.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStatus_shouldReturnRunningStatus() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("running")))
                .andExpect(jsonPath("$.message", is("Scheduler is active")))
                .andExpect(jsonPath("$.tasksCount", is(5)));
    }

    @Test
    void getTasks_shouldReturnTasksList() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks", hasSize(5)))
                .andExpect(jsonPath("$.tasks[0]", containsString("Time Reporter")))
                .andExpect(jsonPath("$.tasks[1]", containsString("System Monitor")))
                .andExpect(jsonPath("$.tasks[2]", containsString("Data Backup")))
                .andExpect(jsonPath("$.tasks[3]", containsString("Cleanup Service")))
                .andExpect(jsonPath("$.tasks[4]", containsString("Report Generator")));
    }

    @Test
    void health_shouldReturnUpStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")));
    }

    @Test
    void getStatus_shouldReturnJsonContentType() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}