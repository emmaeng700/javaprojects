package com.example.actuatorservice;

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsHistoryController {

    private final MetricsCollectorService metricsCollector;

    public MetricsHistoryController(MetricsCollectorService metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @GetMapping("/history")
    public List<MetricsCollectorService.MetricSnapshot> getHistory(
            @RequestParam(defaultValue = "150") int count) {
        return metricsCollector.getRecentSnapshots(count);
    }

    @GetMapping("/latest")
    public Object getLatest() {
        MetricsCollectorService.MetricSnapshot latest = metricsCollector.getLatest();
        if (latest == null) {
            return Collections.singletonMap("status", "collecting");
        }
        return latest;
    }
}
