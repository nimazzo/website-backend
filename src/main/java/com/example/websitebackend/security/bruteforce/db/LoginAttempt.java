package com.example.websitebackend.security.bruteforce.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class LoginAttempt {

    @Id
    private String ip;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Long lastFailedTime;

    public LoginAttempt() {
    }

    public LoginAttempt(String ip, Integer attempts, Long lastFailedTime) {
        this.ip = ip;
        this.attempts = attempts;
        this.lastFailedTime = lastFailedTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String token) {
        this.ip = token;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Long getLastFailedTime() {
        return lastFailedTime;
    }

    public void setLastFailedTime(Long lastFailedTime) {
        this.lastFailedTime = lastFailedTime;
    }
}
