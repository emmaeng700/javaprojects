package com.example.scheduler.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WebSocketConfigTest {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    void webSocketConfig_shouldBeCreated() {
        assertThat(webSocketConfig).isNotNull();
    }

    @Test
    void webSocketConfig_shouldBeInstanceOfConfigurer() {
        assertThat(webSocketConfig)
            .isInstanceOf(org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer.class);
    }
}