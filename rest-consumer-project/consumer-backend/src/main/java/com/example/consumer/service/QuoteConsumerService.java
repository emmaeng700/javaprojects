package com.example.consumer.service;

import com.example.consumer.model.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class QuoteConsumerService {

    private static final Logger log = LoggerFactory.getLogger(QuoteConsumerService.class);
    private final RestClient restClient;

    public QuoteConsumerService(RestClient restClient) {
        this.restClient = restClient;
    }

    public Quote getRandomQuote() {
        try {
            log.debug("Fetching random quote from quoters service...");
            Quote quote = restClient
                    .get()
                    .uri("/api/random")
                    .retrieve()
                    .body(Quote.class);
            
            log.info("Successfully fetched random quote with id: {}", quote.value().id());
            return quote;
        } catch (RestClientException e) {
            log.error("Error fetching random quote: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch random quote", e);
        }
    }

    public List<Quote> getAllQuotes() {
        try {
            log.debug("Fetching all quotes from quoters service...");
            List<Quote> quotes = restClient
                    .get()
                    .uri("/api/")
                    .retrieve()
                    .body(List.class);
            
            log.info("Successfully fetched {} quotes", quotes != null ? quotes.size() : 0);
            return quotes;
        } catch (RestClientException e) {
            log.error("Error fetching all quotes: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch all quotes", e);
        }
    }

    public Quote getQuoteById(Long id) {
        try {
            log.debug("Fetching quote with id: {}", id);
            Quote quote = restClient
                    .get()
                    .uri("/api/{id}", id)
                    .retrieve()
                    .body(Quote.class);
            
            log.info("Successfully fetched quote with id: {}", id);
            return quote;
        } catch (RestClientException e) {
            log.error("Error fetching quote with id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch quote with id: " + id, e);
        }
    }
}