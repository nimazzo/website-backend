package com.example.websitebackend.security;

import com.example.websitebackend.security.db.Token;
import com.example.websitebackend.security.db.TokenRepository;
import com.example.websitebackend.security.keycode.TokenDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collection;

public class CustomUserDetailsManager extends JdbcUserDetailsManager {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsManager.class);

    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    public CustomUserDetailsManager(DataSource dataSource, PasswordEncoder passwordEncoder, TokenRepository tokenRepository) {
        super(dataSource);
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
    }

    public void createToken(TokenDetails tokenDetails) {
        Assert.isTrue(!tokenExists(tokenDetails.token()), "token should not exist");
        createUser(toUserDetails(tokenDetails));
        log.info("Creating token: {}", tokenDetails);
        tokenRepository.save(new Token(tokenDetails.token(), tokenDetails.owner(), tokenDetails.createdAt()));
    }

    public boolean tokenExists(String token) {
        return tokenRepository.existsById(token);
    }

    public Collection<TokenDetails> getAllTokens() {
        return tokenRepository.findAll().stream()
                .map(TokenDetails::fromToken)
                .toList();
    }

    private UserDetails toUserDetails(TokenDetails tokenDetails) {
        return User.withUsername(tokenDetails.token())
                .password(passwordEncoder.encode(tokenDetails.token()))
                .roles("USER")
                .build();
    }

    @Transactional
    public TokenDetails createTokenForOwner(String owner) {
        var token = generateRandomToken();
        var tokenDetails = new TokenDetails(token, owner, LocalDateTime.now());
        createToken(tokenDetails);
        return tokenDetails;
    }

    private String generateRandomToken() {
        var rng = new SecureRandom();
        String token;
        do {
            token = Integer.toString(rng.nextInt(100_000_000)).formatted("%08d");
        } while (tokenExists(token));

        return token;
    }

    public TokenDetails getTokenByOwner(String owner) {
        return tokenRepository.findByOwner(owner);
    }
}
