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
    @JoinColumn(name = "token", nullable = false)
    private Token token;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public Authentication() {
    }

    public Authentication(Token token, LocalDateTime timestamp) {
        this.token = token;
        this.timestamp = timestamp;
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
}
