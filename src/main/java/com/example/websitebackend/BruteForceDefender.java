package com.example.websitebackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceDefender {

    private static final Logger log = LoggerFactory.getLogger(BruteForceDefender.class);

    private final static int MAX_FAILED_ATTEMPTS = 3;
    private final static long BLOCK_TIME_MS = 10 * 1000; // 24 * 60 * 60 * 1000;

    private record AttemptInfo(
            int attempts,
            long lastFailedTime
    ) {
    }

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String ip) {
        var prev = attempts.getOrDefault(ip, new AttemptInfo(0, 0));
        var now = System.currentTimeMillis();
        var newAttempts = prev.attempts + 1;
        attempts.put(ip, new AttemptInfo(newAttempts, now));
        log.warn("Login failed for IP: {}, attempts: {}, last failed time: {}", ip, newAttempts, now);
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
        log.info("Login succeeded for IP: {}", ip);
    }

    public boolean isBlocked(String ip) {
        log.info("Checking if IP is blocked: {}", ip);
        var attemptInfo = attempts.get(ip);

        if (attemptInfo == null || attemptInfo.attempts < MAX_FAILED_ATTEMPTS) {
            log.info("IP {} is not blocked (attempts: {}, last failed time: {})", ip,
                    attemptInfo != null ? attemptInfo.attempts : 0,
                    attemptInfo != null ? attemptInfo.lastFailedTime : 0);
            return false;
        }

        long now = System.currentTimeMillis();

        if (now - attemptInfo.lastFailedTime >= BLOCK_TIME_MS) {
            attempts.remove(ip);
            log.info("IP {} is not blocked anymore (attempts: {}, last failed time: {})", ip,
                    attemptInfo.attempts, attemptInfo.lastFailedTime);
            return false;
        }

        log.warn("IP {} is blocked (attempts: {}, last failed time: {})", ip,
                attemptInfo.attempts, attemptInfo.lastFailedTime);
        return true;
    }
}
