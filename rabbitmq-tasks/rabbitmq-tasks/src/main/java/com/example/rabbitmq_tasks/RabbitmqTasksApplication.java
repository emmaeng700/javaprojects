package com.example.rabbitmq_tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RabbitmqTasksApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqTasksApplication.class, args);
	}

}