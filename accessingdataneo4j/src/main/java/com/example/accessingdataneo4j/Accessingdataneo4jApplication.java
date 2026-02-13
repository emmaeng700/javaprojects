package com.example.accessingdataneo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveRepositoriesAutoConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * Main Spring Boot Application Class
 * 
 * This application demonstrates:
 * - Spring Data Neo4j integration
 * - Graph database operations
 * - Entity relationships in Neo4j
 * - Web interface with Thymeleaf
 */
@SpringBootApplication(exclude = {
    Neo4jReactiveDataAutoConfiguration.class,
    Neo4jReactiveRepositoriesAutoConfiguration.class
})
@EnableNeo4jRepositories
public class Accessingdataneo4jApplication {

    private static final Logger log = LoggerFactory.getLogger(Accessingdataneo4jApplication.class);

    public static void main(String[] args) {
        log.info("Starting Neo4j Team Manager Application...");
        SpringApplication.run(Accessingdataneo4jApplication.class, args);
        log.info("Application started successfully!");
        log.info("Access the application at: http://localhost:8080");
    }
}