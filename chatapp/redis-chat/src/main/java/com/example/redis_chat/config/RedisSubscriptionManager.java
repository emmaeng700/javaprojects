package com.example.redis_chat.config;

import com.example.redis_chat.service.RedisMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisSubscriptionManager.class);

    @Autowired
    private RedisMessageListenerContainer redisContainer;

    @Autowired
    private RedisMessageSubscriber messageSubscriber;

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToChannels() {
        logger.info("Subscribing to Redis channels...");
        redisContainer.addMessageListener(messageSubscriber, new PatternTopic("chat.*"));
        logger.info("Successfully subscribed to chat.* channels");
    }
}