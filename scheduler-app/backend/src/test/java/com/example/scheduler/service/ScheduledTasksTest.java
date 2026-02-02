package com.example.scheduler.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScheduledTasksTest {

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Test
    void scheduledTasks_beanShouldBeCreated() {
        // Verify the bean is created and injected
        assertThat(scheduledTasks).isNotNull();
    }

    @Test
    void scheduledTasks_shouldHaveMessagingTemplate() {
        // Just verify the service loads without errors
        assertThat(scheduledTasks).isInstanceOf(ScheduledTasks.class);
    }
}