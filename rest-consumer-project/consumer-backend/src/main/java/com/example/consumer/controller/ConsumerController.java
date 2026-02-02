package com.example.consumer.controller;

import com.example.consumer.model.Quote;
import com.example.consumer.service.QuoteConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ConsumerController {

    private static final Logger log = LoggerFactory.getLogger(ConsumerController.class);
    private final QuoteConsumerService quoteConsumerService;

    public ConsumerController(QuoteConsumerService quoteConsumerService) {
        this.quoteConsumerService = quoteConsumerService;
    }

    @GetMapping("/quote/random")
    public ResponseEntity<Quote> getRandomQuote() {
        log.info("GET /api/quote/random - Fetching random quote from quoters service");
        Quote quote = quoteConsumerService.getRandomQuote();
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/quote/all")
    public ResponseEntity<List<Quote>> getAllQuotes() {
        log.info("GET /api/quote/all - Fetching all quotes from quoters service");
        List<Quote> quotes = quoteConsumerService.getAllQuotes();
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/quote/{id}")
    public ResponseEntity<Quote> getQuoteById(@PathVariable Long id) {
        log.info("GET /api/quote/{} - Fetching quote by id from quoters service", id);
        try {
            Quote quote = quoteConsumerService.getQuoteById(id);
            return ResponseEntity.ok(quote);
        } catch (RuntimeException e) {
            log.error("Quote with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("GET /api/status - Checking consumer service status");
        Map<String, Object> status = new HashMap<>();
        status.put("service", "REST Consumer");
        status.put("status", "running");
        status.put("quotersUrl", "http://localhost:8080");
        
        try {
            Quote quote = quoteConsumerService.getRandomQuote();
            status.put("quotersStatus", "connected");
            status.put("lastFetchedQuoteId", quote.value().id());
        } catch (Exception e) {
            status.put("quotersStatus", "disconnected");
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }
}