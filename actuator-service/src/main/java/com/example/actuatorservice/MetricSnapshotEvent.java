package com.example.actuatorservice;

public record MetricSnapshotEvent(MetricsCollectorService.MetricSnapshot snapshot) {}
