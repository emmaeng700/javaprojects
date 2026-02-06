package com.example.redis_chat.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    
    private String sender;
    private String content;
    private String room;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private MessageType type;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String sender, String content, String room, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.room = room;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "sender='" + sender + '\'' +
                ", content='" + content + '\'' +
                ", room='" + room + '\'' +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}