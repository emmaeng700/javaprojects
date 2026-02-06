package com.example.redis_chat.service;

import com.example.redis_chat.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessagePublisher.class);

    @Autowired
    private RedisTemplate<String, ChatMessage> redisTemplate;

    @Autowired
    private MessageHistoryService messageHistoryService;

    public void publish(ChatMessage message) {
        String channel = "chat." + message.getRoom();
        logger.info("Publishing message to Redis channel: {}", channel);
        
        // Publish to Redis Pub/Sub for real-time delivery
        redisTemplate.convertAndSend(channel, message);
        
        // Save to history (only for CHAT messages, not JOIN/LEAVE)
        if (message.getType() == ChatMessage.MessageType.CHAT) {
            messageHistoryService.saveMessage(message);
        }
    }
}