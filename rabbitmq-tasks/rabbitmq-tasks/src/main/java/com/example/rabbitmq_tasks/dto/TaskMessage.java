package com.example.rabbitmq_tasks.dto;

import java.io.Serializable;

public class TaskMessage implements Serializable {
    
    private Long taskId;
    private String taskName;
    private String taskType;
    private String description;

    public TaskMessage() {}

    public TaskMessage(Long taskId, String taskName, String taskType, String description) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.description = description;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}