package com.example.actuatorservice;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class GreetingHealthIndicator implements HealthIndicator {

    private final GreetingService greetingService;

    public GreetingHealthIndicator(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @Override
    public Health health() {
        long total = greetingService.getTotalCount();
        Health.Builder builder = Health.up()
                .withDetail("totalGreetings", total)
                .withDetail("uniqueNames", greetingService.getStats().get("uniqueNames"));

        if (total > 1000) {
            builder.withDetail("warning", "High greeting volume detected");
        }

        return builder.build();
    }
}
