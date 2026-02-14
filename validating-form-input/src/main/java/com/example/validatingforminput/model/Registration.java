package com.example.validatingforminput.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class Registration {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Registration() {
        this.id = ID_GENERATOR.getAndIncrement();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Registration(String name, String email, Integer age, String phone) {
        this();
        this.name = name;
        this.email = email;
        this.age = age;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
