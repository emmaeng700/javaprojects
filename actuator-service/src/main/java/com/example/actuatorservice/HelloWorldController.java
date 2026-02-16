package com.example.actuatorservice;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController {

    private final GreetingService greetingService;

    public HelloWorldController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/hello-world")
    public Greeting sayHello(@RequestParam(name = "name", required = false,
            defaultValue = "Stranger") String name) {
        return greetingService.greet(name);
    }

    @GetMapping("/api/greetings/history")
    public List<Map<String, Object>> getHistory() {
        return greetingService.getHistory();
    }

    @GetMapping("/api/greetings/stats")
    public Map<String, Object> getStats() {
        return greetingService.getStats();
    }
}
