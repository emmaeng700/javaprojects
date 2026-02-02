package com.example.quoters.service;

import com.example.quoters.model.Quote;
import com.example.quoters.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);
    private final List<Value> quotes = new ArrayList<>();
    private final Random random = new Random();

    public QuoteService() {
        initializeQuotes();
    }

    private void initializeQuotes() {
        quotes.add(new Value(1L, "Working with Spring Boot is like pair-programming with the Spring developers."));
        quotes.add(new Value(2L, "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications."));
        quotes.add(new Value(3L, "Really loving Spring Boot, makes stand alone Spring apps easy."));
        quotes.add(new Value(4L, "Spring Boot is the best thing that happened to Java development."));
        quotes.add(new Value(5L, "With Spring Boot you can get started quickly and focus on your business logic."));
        quotes.add(new Value(6L, "Spring Boot: Simplicity is the ultimate sophistication."));
        quotes.add(new Value(7L, "Spring Boot eliminates boilerplate code and lets you focus on what matters."));
        quotes.add(new Value(8L, "Auto-configuration in Spring Boot is pure magic!"));
        quotes.add(new Value(9L, "Spring Boot + Spring Data = Developer Happiness"));
        quotes.add(new Value(10L, "Once you go Spring Boot, you never go back!"));
        log.info("Initialized {} quotes", quotes.size());
    }

    public Quote getRandomQuote() {
        Value value = quotes.get(random.nextInt(quotes.size()));
        log.debug("Returning random quote with id: {}", value.id());
        return new Quote("success", value);
    }

    public List<Quote> getAllQuotes() {
        log.debug("Returning all {} quotes", quotes.size());
        return quotes.stream()
                .map(value -> new Quote("success", value))
                .toList();
    }

    public Quote getQuoteById(Long id) {
        log.debug("Searching for quote with id: {}", id);
        return quotes.stream()
                .filter(value -> value.id().equals(id))
                .map(value -> new Quote("success", value))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("Quote with id {} not found", id);
                    return new Quote("error", new Value(0L, "Quote not found"));
                });
    }

    public int getQuotesCount() {
        return quotes.size();
    }
}