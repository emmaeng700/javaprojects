package com.example.redis_chat.service;

import com.example.redis_chat.model.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MessageHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(MessageHistoryService.class);
    private static final int MAX_MESSAGES_PER_ROOM = 100; // Keep last 100 messages
    private static final int MESSAGE_EXPIRY_HOURS = 24; // Messages expire after 24 hours

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Save message to Redis list for history
     */
    public void saveMessage(ChatMessage message) {
        try {
            String key = "chat:history:" + message.getRoom();
            String messageJson = objectMapper.writeValueAsString(message);
            
            // Add message to the end of the list
            stringRedisTemplate.opsForList().rightPush(key, messageJson);
            
            // Trim list to keep only last MAX_MESSAGES_PER_ROOM messages
            stringRedisTemplate.opsForList().trim(key, -MAX_MESSAGES_PER_ROOM, -1);
            
            // Set expiry on the key (24 hours)
            stringRedisTemplate.expire(key, MESSAGE_EXPIRY_HOURS, TimeUnit.HOURS);
            
            logger.debug("Saved message to history: room={}, sender={}", message.getRoom(), message.getSender());
            
        } catch (JsonProcessingException e) {
            logger.error("Error saving message to history", e);
        }
    }

    /**
     * Get message history for a room
     */
    public List<ChatMessage> getMessageHistory(String room, int limit) {
        List<ChatMessage> messages = new ArrayList<>();
        String key = "chat:history:" + room;
        
        try {
            // Get last 'limit' messages (negative index gets from end)
            List<String> messageJsonList = stringRedisTemplate.opsForList().range(key, -limit, -1);
            
            if (messageJsonList != null) {
                for (String messageJson : messageJsonList) {
                    try {
                        ChatMessage message = objectMapper.readValue(messageJson, ChatMessage.class);
                        messages.add(message);
                    } catch (JsonProcessingException e) {
                        logger.error("Error parsing message from history", e);
                    }
                }
            }
            
            logger.info("Retrieved {} messages from history for room: {}", messages.size(), room);
            
        } catch (Exception e) {
            logger.error("Error retrieving message history", e);
        }
        
        return messages;
    }

    /**
     * Clear message history for a room
     */
    public void clearHistory(String room) {
        String key = "chat:history:" + room;
        stringRedisTemplate.delete(key);
        logger.info("Cleared message history for room: {}", room);
    }
}