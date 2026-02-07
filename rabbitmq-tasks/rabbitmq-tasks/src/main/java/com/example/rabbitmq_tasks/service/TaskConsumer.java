package com.example.rabbitmq_tasks.service;

import com.example.rabbitmq_tasks.config.RabbitMQConfig;
import com.example.rabbitmq_tasks.dto.TaskMessage;
import com.example.rabbitmq_tasks.model.Task;
import com.example.rabbitmq_tasks.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumer.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void receiveTask(TaskMessage message) {
        logger.info("Received task from queue: {}", message.getTaskId());

        Task task = taskRepository.findById(message.getTaskId()).orElse(null);
        if (task == null) {
            logger.error("Task not found: {}", message.getTaskId());
            return;
        }

        try {
            task.setStatus(Task.TaskStatus.PROCESSING);
            task.setStartedAt(LocalDateTime.now());
            taskRepository.save(task);
            notifyUpdate(task);

            processTask(task);

            task.setStatus(Task.TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
            task.setResult("Task completed successfully!");
            taskRepository.save(task);
            notifyUpdate(task);

            logger.info("Task completed: {}", task.getId());

        } catch (Exception e) {
            logger.error("Task failed: {}", task.getId(), e);
            task.setStatus(Task.TaskStatus.FAILED);
            task.setResult("Task failed: " + e.getMessage());
            taskRepository.save(task);
            notifyUpdate(task);
        }
    }

    private void processTask(Task task) throws InterruptedException {
        switch (task.getTaskType()) {
            case "email":
                Thread.sleep(2000);
                break;
            case "report":
                Thread.sleep(5000);
                break;
            case "image":
                Thread.sleep(3000);
                break;
            case "backup":
                Thread.sleep(4000);
                break;
            case "export":
                Thread.sleep(3500);
                break;
            default:
                Thread.sleep(1000);
        }
    }

    private void notifyUpdate(Task task) {
        messagingTemplate.convertAndSend("/topic/tasks", task);
    }
}