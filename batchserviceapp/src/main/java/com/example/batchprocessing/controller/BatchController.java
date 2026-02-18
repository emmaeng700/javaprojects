package com.example.batchprocessing.controller;

import com.example.batchprocessing.model.Person;
import com.example.batchprocessing.service.BatchJobService;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class BatchController {

    private final BatchJobService batchJobService;

    public BatchController(BatchJobService batchJobService) {
        this.batchJobService = batchJobService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        long count = batchJobService.getPeopleCount();
        List<JobExecution> executions = batchJobService.getRecentJobExecutions();
        model.addAttribute("peopleCount", count);
        model.addAttribute("executions", executions);
        return "dashboard";
    }

    @PostMapping("/run-job")
    public String runJob(RedirectAttributes redirectAttributes) {
        try {
            JobExecution execution = batchJobService.launchJob();
            redirectAttributes.addFlashAttribute("successMessage",
                    "Batch job launched! Job ID: " + execution.getJobId()
                            + " | Status: " + execution.getStatus());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to launch job: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/people")
    public String listPeople(Model model) {
        List<Person> people = batchJobService.getAllPeople();
        model.addAttribute("people", people);
        return "people";
    }

    @GetMapping("/jobs")
    public String listJobs(Model model) {
        List<JobExecution> executions = batchJobService.getRecentJobExecutions();
        model.addAttribute("executions", executions);
        return "jobs";
    }
}
