package com.example.rabbitmq_tasks.controller;

import com.example.rabbitmq_tasks.model.Task;
import com.example.rabbitmq_tasks.repository.TaskRepository;
import com.example.rabbitmq_tasks.service.TaskProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class TaskController {

    @Autowired
    private TaskProducer taskProducer;

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<Task> tasks = taskRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @PostMapping("/tasks/submit")
    @ResponseBody
    public Task submitTask(@RequestParam String taskName,
                           @RequestParam String taskType,
                           @RequestParam String description) {
        return taskProducer.submitTask(taskName, taskType, description);
    }

    @GetMapping("/api/tasks")
    @ResponseBody
    public List<Task> getAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/api/tasks/{id}")
    @ResponseBody
    public Task getTask(@PathVariable Long id) {
        return taskRepository.findById(id).orElse(null);
    }
}