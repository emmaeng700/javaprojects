package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // ⭐ ADDS SAMPLE DATA ON STARTUP
    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
            repository.save(new User("Alice Johnson", "alice@example.com"));
            repository.save(new User("Bob Smith", "bob@example.com"));
            repository.save(new User("Charlie Brown", "charlie@example.com"));
        };
    }
}