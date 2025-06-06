package com.example.websitebackend.security.keycode;

import com.example.websitebackend.security.db.Token;

import java.time.LocalDateTime;

public record TokenDetails(String token, String owner, LocalDateTime createdAt) {
    public static TokenDetails fromToken(Token token) {
        return new TokenDetails(
                token.getToken(),
                token.getOwner(),
                token.getCreatedAt()
        );
    }
}