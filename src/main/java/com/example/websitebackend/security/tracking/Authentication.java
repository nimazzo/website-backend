package com.example.websitebackend.security.tracking;

import com.example.websitebackend.security.db.Token;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@SuppressWarnings({"unused", "JpaDataSourceORMInspection"})
@Entity
@Table(name = "authentications")
public class Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "token")
    private Token token;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean successful;

    @Column
    private String attemptedToken;

    public Authentication() {
    }

    public Authentication(Token token, LocalDateTime timestamp, Boolean successful, String attemptedToken) {
        this.token = token;
        this.timestamp = timestamp;
        this.successful = successful;
        this.attemptedToken = attemptedToken;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public String getAttemptedToken() {
        return attemptedToken;
    }

    public void setAttemptedToken(String attemptedToken) {
        this.attemptedToken = attemptedToken;
    }
}
