package com.example.schedulingtasks;

import org.springframework.boot.SpringApplication;

public class TestSchedulingTasksApplication {

	public static void main(String[] args) {
		SpringApplication.from(SchedulingTasksApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
