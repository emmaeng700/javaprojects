package com.example.rabbitmq_tasks.repository;

import com.example.rabbitmq_tasks.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByCreatedAtDesc();
    List<Task> findByStatus(Task.TaskStatus status);
}