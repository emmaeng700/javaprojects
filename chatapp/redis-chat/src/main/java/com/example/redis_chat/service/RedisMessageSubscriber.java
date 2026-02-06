package com.example.redis_chat.service;

import com.example.redis_chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageSubscriber implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RedisMessageSubscriber.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());
            
            logger.info("Received Redis message from channel: {}", channel);
            logger.debug("Message body: {}", body);
            
            ChatMessage chatMessage = objectMapper.readValue(body, ChatMessage.class);
            
            // Forward to WebSocket subscribers
            String destination = "/topic/messages/" + chatMessage.getRoom();
            messagingTemplate.convertAndSend(destination, chatMessage);
            
            logger.info("Forwarded message to WebSocket destination: {}", destination);
            
        } catch (Exception e) {
            logger.error("Error processing Redis message", e);
        }
    }
}