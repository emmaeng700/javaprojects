package com.example.actuatorservice;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemInfoController {

    @GetMapping("/api/system")
    @Cacheable("systemInfo")
    public Map<String, Object> systemInfo() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

        Map<String, Object> info = new LinkedHashMap<>();

        // JVM info
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("name", runtime.getVmName());
        jvm.put("vendor", runtime.getVmVendor());
        jvm.put("version", runtime.getVmVersion());
        jvm.put("specVersion", runtime.getSpecVersion());
        info.put("jvm", jvm);

        // Memory
        Map<String, Object> mem = new LinkedHashMap<>();
        mem.put("heapUsed", formatBytes(memory.getHeapMemoryUsage().getUsed()));
        mem.put("heapMax", formatBytes(memory.getHeapMemoryUsage().getMax()));
        mem.put("heapCommitted", formatBytes(memory.getHeapMemoryUsage().getCommitted()));
        mem.put("nonHeapUsed", formatBytes(memory.getNonHeapMemoryUsage().getUsed()));
        info.put("memory", mem);

        // Runtime
        Map<String, Object> rt = new LinkedHashMap<>();
        rt.put("pid", ProcessHandle.current().pid());
        rt.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        rt.put("uptimeMs", runtime.getUptime());
        rt.put("startTime", Instant.ofEpochMilli(runtime.getStartTime()).toString());
        rt.put("javaVersion", System.getProperty("java.version"));
        rt.put("osName", System.getProperty("os.name"));
        rt.put("osArch", System.getProperty("os.arch"));
        info.put("runtime", rt);

        return info;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1048576) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / 1048576.0);
    }
}
