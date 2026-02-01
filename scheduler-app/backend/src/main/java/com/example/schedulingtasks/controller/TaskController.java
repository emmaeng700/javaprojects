package com.example.schedulingtasks.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskController {

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "running");
        status.put("message", "Scheduler is active");
        status.put("tasksCount", 5);
        return status;
    }

    @GetMapping("/tasks")
    public Map<String, Object> getTasks() {
        Map<String, Object> response = new HashMap<>();
        response.put("tasks", new String[]{
            "Time Reporter (Every 5s)",
            "System Monitor (Every 10s)",
            "Data Backup (Every 15s)",
            "Cleanup Service (Every 20s)",
            "Report Generator (Every 30s)"
        });
        return response;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        return health;
    }
}
