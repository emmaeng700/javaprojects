package com.example.rabbitmq_tasks.service;

import com.example.rabbitmq_tasks.config.RabbitMQConfig;
import com.example.rabbitmq_tasks.dto.TaskMessage;
import com.example.rabbitmq_tasks.model.Task;
import com.example.rabbitmq_tasks.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskProducer {

    private static final Logger logger = LoggerFactory.getLogger(TaskProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TaskRepository taskRepository;

    public Task submitTask(String taskName, String taskType, String description) {
        Task task = new Task(taskName, taskType, description);
        task = taskRepository.save(task);

        TaskMessage message = new TaskMessage(
            task.getId(),
            task.getTaskName(),
            task.getTaskType(),
            task.getDescription()
        );

        logger.info("Sending task to queue: {}", message.getTaskId());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.TASK_EXCHANGE,
            RabbitMQConfig.TASK_ROUTING_KEY,
            message
        );

        return task;
    }
}