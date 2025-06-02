package com.example.websitebackend.security.keycode;

import java.time.LocalDateTime;

public record TokenDetails(String token, String owner, LocalDateTime createdAt) {
}