package com.example.scheduler.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class TaskExecutionTest {

    @Test
    void taskExecution_shouldBeCreatedWithConstructor() {
        // Given
        String taskName = "Test Task";
        LocalDateTime now = LocalDateTime.now();
        String message = "Test message";
        String status = "success";

        // When
        TaskExecution execution = new TaskExecution(taskName, now, message, status);

        // Then
        assertThat(execution.getTaskName()).isEqualTo(taskName);
        assertThat(execution.getExecutionTime()).isEqualTo(now);
        assertThat(execution.getMessage()).isEqualTo(message);
        assertThat(execution.getStatus()).isEqualTo(status);
    }

    @Test
    void taskExecution_shouldSupportSetters() {
        // Given
        TaskExecution execution = new TaskExecution();

        // When
        execution.setTaskName("Updated Task");
        execution.setExecutionTime(LocalDateTime.now());
        execution.setMessage("Updated message");
        execution.setStatus("warning");

        // Then
        assertThat(execution.getTaskName()).isEqualTo("Updated Task");
        assertThat(execution.getMessage()).isEqualTo("Updated message");
        assertThat(execution.getStatus()).isEqualTo("warning");
    }

    @Test
    void taskExecution_shouldCreateWithNoArgsConstructor() {
        // When
        TaskExecution execution = new TaskExecution();

        // Then
        assertThat(execution).isNotNull();
        assertThat(execution.getTaskName()).isNull();
        assertThat(execution.getExecutionTime()).isNull();
        assertThat(execution.getMessage()).isNull();
        assertThat(execution.getStatus()).isNull();
    }
}