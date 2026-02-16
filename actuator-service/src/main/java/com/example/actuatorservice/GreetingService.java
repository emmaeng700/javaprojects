package com.example.actuatorservice;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private final ConcurrentLinkedDeque<Map<String, Object>> history = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<String, AtomicLong> nameCounts = new ConcurrentHashMap<>();

    public Greeting greet(String name) {
        long id = counter.incrementAndGet();
        Greeting greeting = new Greeting(id, String.format(template, name));

        // Track history (keep last 50)
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("name", name);
        entry.put("content", greeting.getContent());
        entry.put("timestamp", Instant.now().toString());
        history.addFirst(entry);
        while (history.size() > 50) {
            history.removeLast();
        }

        // Track name frequency
        nameCounts.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();

        return greeting;
    }

    public long getTotalCount() {
        return counter.get();
    }

    public List<Map<String, Object>> getHistory() {
        return new ArrayList<>(history);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalGreetings", counter.get());
        stats.put("uniqueNames", nameCounts.size());

        // Top 5 most greeted names
        List<Map<String, Object>> topNames = new ArrayList<>();
        nameCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(5)
                .forEach(e -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", e.getKey());
                    entry.put("count", e.getValue().get());
                    topNames.add(entry);
                });
        stats.put("topNames", topNames);

        return stats;
    }
}
