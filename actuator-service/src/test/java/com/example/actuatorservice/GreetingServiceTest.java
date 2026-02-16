package com.example.actuatorservice;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GreetingServiceTest {

    private GreetingService service;

    @BeforeEach
    void setUp() {
        service = new GreetingService();
    }

    @Test
    void greetShouldReturnFormattedMessage() {
        Greeting greeting = service.greet("Alice");
        assertThat(greeting.getContent()).isEqualTo("Hello, Alice!");
    }

    @Test
    void greetShouldIncrementId() {
        Greeting first = service.greet("A");
        Greeting second = service.greet("B");
        assertThat(second.getId()).isEqualTo(first.getId() + 1);
    }

    @Test
    void totalCountShouldTrack() {
        service.greet("A");
        service.greet("B");
        service.greet("C");
        assertThat(service.getTotalCount()).isEqualTo(3);
    }

    @Test
    void historyShouldTrackRecentGreetings() {
        service.greet("Alice");
        service.greet("Bob");

        List<Map<String, Object>> history = service.getHistory();
        assertThat(history).hasSize(2);
        // Most recent first
        assertThat(history.get(0).get("name")).isEqualTo("Bob");
        assertThat(history.get(1).get("name")).isEqualTo("Alice");
    }

    @Test
    void historyShouldCapAt50() {
        for (int i = 0; i < 60; i++) {
            service.greet("User" + i);
        }
        assertThat(service.getHistory()).hasSize(50);
    }

    @Test
    void statsShouldIncludeTopNames() {
        service.greet("Alice");
        service.greet("Alice");
        service.greet("Alice");
        service.greet("Bob");
        service.greet("Bob");
        service.greet("Charlie");

        Map<String, Object> stats = service.getStats();
        assertThat(stats.get("totalGreetings")).isEqualTo(6L);
        assertThat(stats.get("uniqueNames")).isEqualTo(3);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topNames = (List<Map<String, Object>>) stats.get("topNames");
        assertThat(topNames).hasSize(3);
        assertThat(topNames.get(0).get("name")).isEqualTo("Alice");
        assertThat(topNames.get(0).get("count")).isEqualTo(3L);
    }

    @Test
    void statsShouldLimitTopNamesToFive() {
        for (int i = 0; i < 10; i++) {
            service.greet("Name" + i);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topNames =
                (List<Map<String, Object>>) service.getStats().get("topNames");
        assertThat(topNames).hasSizeLessThanOrEqualTo(5);
    }

    @Test
    void historyEntryShouldContainTimestamp() {
        service.greet("Test");
        List<Map<String, Object>> history = service.getHistory();
        assertThat(history.get(0)).containsKey("timestamp");
        assertThat(history.get(0).get("timestamp").toString()).isNotEmpty();
    }
}
