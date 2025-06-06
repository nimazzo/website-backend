package com.example.websitebackend.security.bruteforce.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedAttemptsRepository extends JpaRepository<LoginAttempt, String> {
}
