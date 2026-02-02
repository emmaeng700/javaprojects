package com.example.quoters.service;

import com.example.quoters.model.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuoteServiceTest {

    @Autowired
    private QuoteService quoteService;

    @Test
    void quoteService_shouldBeInjected() {
        assertThat(quoteService).isNotNull();
    }

    @Test
    void getRandomQuote_shouldReturnValidQuote() {
        Quote quote = quoteService.getRandomQuote();
        
        assertThat(quote).isNotNull();
        assertThat(quote.type()).isEqualTo("success");
        assertThat(quote.value()).isNotNull();
        assertThat(quote.value().id()).isNotNull();
        assertThat(quote.value().id()).isBetween(1L, 10L);
        assertThat(quote.value().quote()).isNotEmpty();
    }

    @Test
    void getAllQuotes_shouldReturn10Quotes() {
        List<Quote> quotes = quoteService.getAllQuotes();
        
        assertThat(quotes).isNotNull();
        assertThat(quotes).hasSize(10);
        assertThat(quotes).allMatch(q -> q.type().equals("success"));
        assertThat(quotes).allMatch(q -> q.value() != null);
    }

    @Test
    void getQuoteById_withValidId_shouldReturnCorrectQuote() {
        Quote quote = quoteService.getQuoteById(1L);
        
        assertThat(quote).isNotNull();
        assertThat(quote.type()).isEqualTo("success");
        assertThat(quote.value().id()).isEqualTo(1L);
        assertThat(quote.value().quote()).contains("pair-programming");
    }

    @Test
    void getQuoteById_withInvalidId_shouldReturnError() {
        Quote quote = quoteService.getQuoteById(999L);
        
        assertThat(quote).isNotNull();
        assertThat(quote.type()).isEqualTo("error");
        assertThat(quote.value().id()).isEqualTo(0L);
        assertThat(quote.value().quote()).isEqualTo("Quote not found");
    }

    @Test
    void getQuotesCount_shouldReturn10() {
        int count = quoteService.getQuotesCount();
        assertThat(count).isEqualTo(10);
    }
}