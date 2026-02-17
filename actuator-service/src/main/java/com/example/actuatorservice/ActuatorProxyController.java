package com.example.actuatorservice;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Proxies actuator requests from the main server port to the management port,
 * avoiding CORS issues when the browser fetches actuator data.
 */
@RestController
@RequestMapping("/api/actuator")
public class ActuatorProxyController {

    private final String managementBase;
    private final RestTemplate restTemplate;

    public ActuatorProxyController(@Value("${management.server.port}") int managementPort) {
        this.managementBase = "http://127.0.0.1:" + managementPort;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping
    public ResponseEntity<String> actuatorIndex() {
        return proxy("/actuator");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return proxy("/actuator/health");
    }

    @GetMapping("/info")
    public ResponseEntity<String> info() {
        return proxy("/actuator/info");
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics() {
        return proxy("/actuator/metrics");
    }

    @GetMapping("/metrics/{name}")
    public ResponseEntity<String> metric(@PathVariable String name) {
        return proxy("/actuator/metrics/" + name);
    }

    @GetMapping("/env")
    public ResponseEntity<String> env() {
        return proxy("/actuator/env");
    }

    @GetMapping("/beans")
    public ResponseEntity<String> beans() {
        return proxy("/actuator/beans");
    }

    @GetMapping("/mappings")
    public ResponseEntity<String> mappings() {
        return proxy("/actuator/mappings");
    }

    @GetMapping("/configprops")
    public ResponseEntity<String> configprops() {
        return proxy("/actuator/configprops");
    }

    @GetMapping("/loggers")
    public ResponseEntity<String> loggers() {
        return proxy("/actuator/loggers");
    }

    @GetMapping("/threaddump")
    public ResponseEntity<String> threaddump() {
        return proxy("/actuator/threaddump");
    }

    @GetMapping("/scheduledtasks")
    public ResponseEntity<String> scheduledtasks() {
        return proxy("/actuator/scheduledtasks");
    }

    @GetMapping("/conditions")
    public ResponseEntity<String> conditions() {
        return proxy("/actuator/conditions");
    }

    /** Catch-all: proxies any sub-path like /health/{*path}, /env/{toMatch}, /loggers/{name}, etc. */
    @GetMapping("/{endpoint}/**")
    public ResponseEntity<String> catchAll(HttpServletRequest request) {
        // Strip the /api/actuator prefix to get the actuator path
        String uri = request.getRequestURI();                       // e.g. /api/actuator/loggers/ROOT
        String actuatorPath = uri.replaceFirst("^/api/actuator", "/actuator");
        return proxy(actuatorPath);
    }

    private ResponseEntity<String> proxy(String path) {
        try {
            ResponseEntity<String> resp = restTemplate.exchange(
                    managementBase + path, HttpMethod.GET, null, String.class);
            return ResponseEntity.status(resp.getStatusCode())
                    .header("Content-Type", "application/json")
                    .body(resp.getBody());
        } catch (HttpClientErrorException e) {
            // Forward 4xx status codes faithfully (e.g. 404 for invalid param)
            return ResponseEntity.status(e.getStatusCode())
                    .header("Content-Type", "application/json")
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body("{\"error\":\"Management endpoint unavailable: " + e.getMessage() + "\"}");
        }
    }
}
