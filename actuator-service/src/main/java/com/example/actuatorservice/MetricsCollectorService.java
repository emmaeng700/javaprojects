package com.example.actuatorservice;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MetricsCollectorService {

    private static final int MAX_SNAPSHOTS = 300;

    private final ConcurrentLinkedDeque<MetricSnapshot> snapshots = new ConcurrentLinkedDeque<>();
    private final GreetingService greetingService;
    private final ApplicationEventPublisher eventPublisher;

    public MetricsCollectorService(GreetingService greetingService,
                                   ApplicationEventPublisher eventPublisher) {
        this.greetingService = greetingService;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 2000)
    public void collectMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long heapCommitted = memoryBean.getHeapMemoryUsage().getCommitted();
        int threadCount = threadBean.getThreadCount();
        int daemonThreadCount = threadBean.getDaemonThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();

        double cpuUsage = -1;
        double systemCpuUsage = -1;
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                    (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            cpuUsage = osBean.getProcessCpuLoad() * 100;
            systemCpuUsage = osBean.getCpuLoad() * 100;
            if (cpuUsage < 0) cpuUsage = 0;
            if (systemCpuUsage < 0) systemCpuUsage = 0;
        } catch (Exception ignored) {
        }

        MetricSnapshot snapshot = new MetricSnapshot(
                Instant.now(),
                heapUsed, heapMax, heapCommitted,
                threadCount, daemonThreadCount, peakThreadCount,
                cpuUsage, systemCpuUsage,
                greetingService.getTotalCount()
        );

        snapshots.addFirst(snapshot);
        while (snapshots.size() > MAX_SNAPSHOTS) {
            snapshots.removeLast();
        }

        eventPublisher.publishEvent(new MetricSnapshotEvent(snapshot));
    }

    public List<MetricSnapshot> getSnapshots() {
        return new ArrayList<>(snapshots);
    }

    public List<MetricSnapshot> getRecentSnapshots(int count) {
        List<MetricSnapshot> all = new ArrayList<>(snapshots);
        if (all.size() <= count) return all;
        return all.subList(0, count);
    }

    public MetricSnapshot getLatest() {
        return snapshots.peekFirst();
    }

    public record MetricSnapshot(
            Instant timestamp,
            long heapUsed,
            long heapMax,
            long heapCommitted,
            int threadCount,
            int daemonThreadCount,
            int peakThreadCount,
            double cpuUsage,
            double systemCpuUsage,
            long totalGreetings
    ) {}
}
