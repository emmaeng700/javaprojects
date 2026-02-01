package com.example.schedulingtasks.model;

import java.time.LocalDateTime;

public class TaskExecution {
    private String taskName;
    private LocalDateTime executionTime;
    private String message;
    private String status;

    public TaskExecution() {}

    public TaskExecution(String taskName, LocalDateTime executionTime, String message, String status) {
        this.taskName = taskName;
        this.executionTime = executionTime;
        this.message = message;
        this.status = status;
    }

    // Getters and Setters
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
