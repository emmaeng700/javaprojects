package com.example.consumer.service;

import com.example.consumer.model.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuoteConsumerServiceTest {

    @Autowired
    private QuoteConsumerService quoteConsumerService;

    @Test
    void quoteConsumerService_shouldBeInjected() {
        assertThat(quoteConsumerService).isNotNull();
    }

    @Test
    void getRandomQuote_shouldReturnValidQuote() {
        Quote quote = quoteConsumerService.getRandomQuote();
        
        assertThat(quote).isNotNull();
        assertThat(quote.type()).isEqualTo("success");
        assertThat(quote.value()).isNotNull();
        assertThat(quote.value().id()).isNotNull();
        assertThat(quote.value().quote()).isNotEmpty();
    }

    @Test
    void getQuoteById_shouldReturnSpecificQuote() {
        Quote quote = quoteConsumerService.getQuoteById(1L);
        
        assertThat(quote).isNotNull();
        assertThat(quote.type()).isEqualTo("success");
        assertThat(quote.value().id()).isEqualTo(1L);
    }
}