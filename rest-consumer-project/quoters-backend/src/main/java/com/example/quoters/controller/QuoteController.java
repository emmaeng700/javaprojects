package com.example.quoters.controller;

import com.example.quoters.model.Quote;
import com.example.quoters.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"})
public class QuoteController {

    private static final Logger log = LoggerFactory.getLogger(QuoteController.class);
    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping("/random")
    public ResponseEntity<Quote> random() {
        Quote quote = quoteService.getRandomQuote();
        log.info("GET /api/random - Returned quote id: {}", quote.value().id());
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/")
    public ResponseEntity<List<Quote>> all() {
        List<Quote> quotes = quoteService.getAllQuotes();
        log.info("GET /api/ - Returned {} quotes", quotes.size());
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quote> getById(@PathVariable Long id) {
        Quote quote = quoteService.getQuoteById(id);
        log.info("GET /api/{} - Returned quote with status: {}", id, quote.type());
        
        if ("error".equals(quote.type())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(quote);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> count() {
        int count = quoteService.getQuotesCount();
        log.info("GET /api/count - Returned count: {}", count);
        return ResponseEntity.ok(count);
    }
}