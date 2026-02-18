package com.example.batchprocessing.service;

import com.example.batchprocessing.model.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchJobService {

    private final JobLauncher asyncJobLauncher;
    private final Job importUserJob;
    private final JobExplorer jobExplorer;
    private final JdbcTemplate jdbcTemplate;

    public BatchJobService(@Qualifier("asyncJobLauncher") JobLauncher asyncJobLauncher,
                           Job importUserJob,
                           JobExplorer jobExplorer,
                           JdbcTemplate jdbcTemplate) {
        this.asyncJobLauncher = asyncJobLauncher;
        this.importUserJob = importUserJob;
        this.jobExplorer = jobExplorer;
        this.jdbcTemplate = jdbcTemplate;
    }

    public JobExecution launchJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        return asyncJobLauncher.run(importUserJob, params);
    }

    public List<Person> getAllPeople() {
        return jdbcTemplate.query(
                "SELECT person_id, first_name, last_name FROM people",
                (rs, rowNum) -> {
                    Person p = new Person(rs.getString("first_name"), rs.getString("last_name"));
                    p.setPersonId(rs.getLong("person_id"));
                    return p;
                });
    }

    public long getPeopleCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM people", Long.class);
        return count != null ? count : 0;
    }

    public List<JobExecution> getRecentJobExecutions() {
        return jobExplorer.getJobInstances("importUserJob", 0, 10).stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .limit(10)
                .toList();
    }
}
