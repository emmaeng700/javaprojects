package com.example.quoters;

import org.springframework.boot.SpringApplication;

public class TestQuotersApplication {

	public static void main(String[] args) {
		SpringApplication.from(QuotersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
