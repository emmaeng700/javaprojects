package com.example.scheduler.service;

import com.example.scheduler.model.TaskExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    private final Random random = new Random();

    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
        String time = dateFormat.format(new Date());
        String message = "⏰ The time is now " + time;
        log.info(message);

        if (messagingTemplate != null) {
            TaskExecution execution = new TaskExecution(
                "Time Reporter",
                LocalDateTime.now(),
                message,
                "success"
            );
            messagingTemplate.convertAndSend("/topic/tasks", execution);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void checkSystemStatus() {
        String message = "✅ System status: All services running smoothly";
        log.info(message);

        if (messagingTemplate != null) {
            TaskExecution execution = new TaskExecution(
                "System Monitor",
                LocalDateTime.now(),
                message,
                "success"
            );
            messagingTemplate.convertAndSend("/topic/tasks", execution);
        }
    }

    @Scheduled(fixedRate = 15000)
    public void performDataBackup() {
        boolean success = random.nextInt(10) > 2;
        String status = success ? "success" : "warning";
        String message = success 
            ? "💾 Data backup completed successfully" 
            : "⚠️ Data backup: Warning - Retry scheduled";
        
        log.info(message);

        if (messagingTemplate != null) {
            TaskExecution execution = new TaskExecution(
                "Data Backup",
                LocalDateTime.now(),
                message,
                status
            );
            messagingTemplate.convertAndSend("/topic/tasks", execution);
        }
    }

    @Scheduled(fixedRate = 20000)
    public void cleanupTempFiles() {
        int filesDeleted = random.nextInt(50) + 10;
        String message = "🗑️ Cleanup complete: " + filesDeleted + " temp files removed";
        log.info(message);

        if (messagingTemplate != null) {
            TaskExecution execution = new TaskExecution(
                "Cleanup Service",
                LocalDateTime.now(),
                message,
                "success"
            );
            messagingTemplate.convertAndSend("/topic/tasks", execution);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void generateReport() {
        String message = "📊 Monthly report generated and sent to admins";
        log.info(message);

        if (messagingTemplate != null) {
            TaskExecution execution = new TaskExecution(
                "Report Generator",
                LocalDateTime.now(),
                message,
                "success"
            );
            messagingTemplate.convertAndSend("/topic/tasks", execution);
        }
    }
}