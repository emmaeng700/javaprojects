package com.example.consumer;

import com.example.consumer.model.Quote;
import com.example.consumer.service.QuoteConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class ConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(ConsumerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public ApplicationRunner run(QuoteConsumerService quoteConsumerService) {
        return args -> {
            log.info("=".repeat(80));
            log.info("🚀 Consumer Application Started - Fetching quote from Quoters API...");
            log.info("=".repeat(80));
            
            Quote quote = quoteConsumerService.getRandomQuote();
            
            log.info("📨 Received Quote:");
            log.info("   ID: {}", quote.value().id());
            log.info("   Quote: {}", quote.value().quote());
            log.info("   Status: {}", quote.type());
            log.info("=".repeat(80));
        };
    }
}