package com.example.ldapauth;

import org.springframework.boot.SpringApplication;

public class TestLdapauthApplication {

	public static void main(String[] args) {
		SpringApplication.from(LdapauthApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
