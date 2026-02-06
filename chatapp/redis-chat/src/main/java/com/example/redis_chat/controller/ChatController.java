package com.example.redis_chat.controller;

import com.example.redis_chat.model.ChatMessage;
import com.example.redis_chat.service.RedisMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private RedisMessagePublisher messagePublisher;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String username, 
                       @RequestParam String room,
                       Model model) {
        model.addAttribute("username", username);
        model.addAttribute("room", room);
        logger.info("User {} joining room {}", username, room);
        return "chat";
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message) {
        logger.info("Sending message: {}", message);
        message.setType(ChatMessage.MessageType.CHAT);
        messagePublisher.publish(message);
    }

    @MessageMapping("/chat.join")
    public void joinRoom(@Payload ChatMessage message) {
        logger.info("User joining: {}", message);
        message.setType(ChatMessage.MessageType.JOIN);
        message.setContent(message.getSender() + " joined the room!");
        messagePublisher.publish(message);
    }

    @MessageMapping("/chat.leave")
    public void leaveRoom(@Payload ChatMessage message) {
        logger.info("User leaving: {}", message);
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setContent(message.getSender() + " left the room!");
        messagePublisher.publish(message);
    }
}