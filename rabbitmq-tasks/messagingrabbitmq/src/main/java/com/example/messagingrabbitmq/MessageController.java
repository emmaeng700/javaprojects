package com.example.messagingrabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") // for dev; tighten in prod
public class MessageController {

    private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;

    public MessageController(RabbitTemplate rabbitTemplate, Receiver receiver) {
        this.rabbitTemplate = rabbitTemplate;
        this.receiver = receiver;
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody SendRequest request) {
        String body = request.message() == null ? "" : request.message().trim();
        if (body.isEmpty()) {
            return ResponseEntity.badRequest().body("Message must not be empty");
        }

        // routing key must match "foo.bar.#"
        rabbitTemplate.convertAndSend(
                MessagingRabbitmqApplication.TOPIC_EXCHANGE_NAME,
                "foo.bar.frontend",
                body + " (sent at " + Instant.now() + ")"
        );

        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public List<Receiver.ReceivedMessage> getMessages() {
        return receiver.getMessages();
    }

    public record SendRequest(String message) {}
}