package com.example.websitebackend.security.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@SuppressWarnings("unused")
@Entity
public class Token {
    @Id
    private String token;
    @Column(unique = true, nullable = false)
    private String owner;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Token() {
    }

    public Token(String token, String owner, LocalDateTime createdAt) {
        this.token = token;
        this.owner = owner;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
