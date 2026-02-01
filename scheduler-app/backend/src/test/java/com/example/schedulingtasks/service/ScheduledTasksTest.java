package com.example.schedulingtasks.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import java.time.Duration;

@SpringBootTest
@TestPropertySource(properties = "spring.task.scheduling.enabled=true")
class ScheduledTasksTest {

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Test
    void scheduledTasks_shouldExecuteAndIncrementCounter() {
        // Reset counter
        scheduledTasks.resetExecutionCount();
        
        // Wait longer for multiple tasks to execute (tasks run every 5s)
        await()
            .atMost(Duration.ofSeconds(15))
            .pollDelay(Duration.ofSeconds(6))
            .untilAsserted(() -> {
                int count = scheduledTasks.getExecutionCount();
                System.out.println("Current execution count: " + count);
                assertThat(count).isGreaterThan(1);
            });
        
        // Verify counter increased to at least 2
        int finalCount = scheduledTasks.getExecutionCount();
        System.out.println("Final execution count: " + finalCount);
        assertThat(finalCount).isGreaterThanOrEqualTo(2);
    }
}
