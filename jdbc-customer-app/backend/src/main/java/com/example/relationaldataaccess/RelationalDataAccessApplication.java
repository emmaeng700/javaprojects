package com.example.relationaldataaccess;

import com.example.relationaldataaccess.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class RelationalDataAccessApplication {

    private static final Logger log = LoggerFactory.getLogger(RelationalDataAccessApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RelationalDataAccessApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(CustomerRepository repository) {
        return (args) -> {
            // Initialize database
            repository.initializeDatabase();

            // Split up the array of whole names into an array of first/last names
            List<Object[]> splitUpNames = Stream.of("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
                    .map(name -> name.split(" "))
                    .collect(Collectors.toList());

            // Log each insertion
            splitUpNames.forEach(name ->
                    log.info("Inserting customer record for {} {}", name[0], name[1]));

            // Use JdbcTemplate's batchUpdate operation to bulk load data
            repository.batchInsert(splitUpNames);

            log.info("Querying for customer records where first_name = 'Josh':");
            repository.findByFirstName("Josh")
                    .forEach(customer -> log.info(customer.toString()));
        };
    }
}