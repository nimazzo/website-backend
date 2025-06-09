package com.example.websitebackend.security.tracking;

import com.example.websitebackend.security.db.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationTracker {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationTracker.class);

    private final AuthenticationRepository authenticationRepository;
    private final TokenRepository tokenRepository;

    public AuthenticationTracker(AuthenticationRepository authenticationRepository, TokenRepository tokenRepository) {
        this.authenticationRepository = authenticationRepository;
        this.tokenRepository = tokenRepository;
    }

    public void trackSuccessfulAuthentication(String keyCode) {
        log.info("Tracking successful authentication for key code: {}", keyCode);

        var token = tokenRepository.findById(keyCode).orElseThrow(() ->
                new IllegalArgumentException("Token not found for key code: " + keyCode));

        var authentication = new Authentication(token, LocalDateTime.now());
        authenticationRepository.save(authentication);
    }

    public List<Authentication> getAllAuthentications() {
        log.info("Retrieving all authentications");
        return authenticationRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}
