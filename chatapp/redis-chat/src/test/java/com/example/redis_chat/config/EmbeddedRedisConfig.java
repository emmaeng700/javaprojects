package com.example.redis_chat.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
        } catch (Exception e) {
            // Redis might already be running
            System.out.println("Could not start embedded Redis: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}
