package com.example.uploadingfiles;

import org.springframework.boot.SpringApplication;

public class TestUploadingfilesApplication {

	public static void main(String[] args) {
		SpringApplication.from(UploadingfilesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
