package com.example.messagingrabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class Receiver {

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);

    // Keep messages in memory so frontend can fetch them
    private final List<ReceivedMessage> messages = new CopyOnWriteArrayList<>();

    public void receiveMessage(String message) {
        logger.info("Received <{}>", message);
        messages.add(new ReceivedMessage(message, Instant.now().toString()));
    }

    public List<ReceivedMessage> getMessages() {
        return messages;
    }

    public record ReceivedMessage(String body, String receivedAt) {}
}