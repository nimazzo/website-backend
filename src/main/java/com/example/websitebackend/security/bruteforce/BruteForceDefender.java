package com.example.websitebackend.security.bruteforce;

import com.example.websitebackend.security.SecurityProperties;
import com.example.websitebackend.security.bruteforce.db.FailedAttemptsRepository;
import com.example.websitebackend.security.bruteforce.db.LoginAttempt;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class BruteForceDefender {

    private static final Logger log = LoggerFactory.getLogger(BruteForceDefender.class);

    private final static int MAX_FAILED_ATTEMPTS = 3;

    private final FailedAttemptsRepository failedAttemptsRepository;

    private final Duration timeout;

    public BruteForceDefender(FailedAttemptsRepository failedAttemptsRepository, SecurityProperties securityProperties) {
        this.failedAttemptsRepository = failedAttemptsRepository;
        this.timeout = securityProperties.bruteforceDefenderBlockTime();
        log.info("BruteForceDefender initialized with block time: {}", timeout);
    }

    public void unblockAllUsers() {
        failedAttemptsRepository.deleteAll();
    }

//    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    @Transactional(dontRollbackOn = BlockedException.class)
    public void loginFailed(String ip) {
        var now = System.currentTimeMillis();
        var info = failedAttemptsRepository.findById(ip).orElse(new LoginAttempt(ip, 0, now));
        var newAttempts = info.getAttempts() + 1;
        info.setAttempts(newAttempts);
        failedAttemptsRepository.save(info);

        log.warn("Login failed for IP: {}, attempts: {}, last failed time: {}", ip, newAttempts, now);
        if (info.getAttempts() >= MAX_FAILED_ATTEMPTS) {
            log.warn("IP {} is blocked for {} seconds due to too many failed attempts ({} attempts)",
                    ip, timeout.getSeconds(), newAttempts);
            throw new BlockedException("Too many failed attempts, please try again later.");
        }
    }

    public void loginSucceeded(String ip) {
        failedAttemptsRepository.deleteById(ip);
        log.info("Login succeeded for IP: {}", ip);
    }

    public boolean isBlocked(String ip) {
        log.info("Checking if IP is blocked: {}", ip);
        var attemptInfo = failedAttemptsRepository.findById(ip).orElse(null);

        if (attemptInfo == null || attemptInfo.getAttempts() < MAX_FAILED_ATTEMPTS) {
            log.info("IP {} is not blocked (attempts: {}, last failed time: {})", ip,
                    attemptInfo != null ? attemptInfo.getAttempts() : 0,
                    attemptInfo != null ? attemptInfo.getLastFailedTime() : 0);
            return false;
        }

        long now = System.currentTimeMillis();

        if (now - attemptInfo.getLastFailedTime() >= timeout.toMillis()) {
            failedAttemptsRepository.deleteById(ip);
            log.info("IP {} is not blocked anymore (attempts: {}, last failed time: {})", ip,
                    attemptInfo.getAttempts(), attemptInfo.getLastFailedTime());
            return false;
        }

        log.warn("IP {} is blocked (attempts: {}, last failed time: {})", ip,
                attemptInfo.getAttempts(), attemptInfo.getLastFailedTime());
        return true;
    }
}
