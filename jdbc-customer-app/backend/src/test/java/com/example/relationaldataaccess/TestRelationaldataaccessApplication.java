package com.example.relationaldataaccess;

import org.springframework.boot.SpringApplication;

public class TestRelationaldataaccessApplication {

	public static void main(String[] args) {
		SpringApplication.from(RelationaldataaccessApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
