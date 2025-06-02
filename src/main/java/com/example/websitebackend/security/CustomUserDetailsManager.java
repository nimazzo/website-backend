package com.example.websitebackend.security;

import com.example.websitebackend.security.keycode.TokenDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomUserDetailsManager extends InMemoryUserDetailsManager {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, TokenDetails> tokens = new HashMap<>();

    public CustomUserDetailsManager(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void createToken(TokenDetails tokenDetails) {
        Assert.isTrue(!tokenExists(tokenDetails.token()), "token should not exist");
        createUser(toUserDetails(tokenDetails));
        this.tokens.put(tokenDetails.token().toLowerCase(Locale.ROOT), tokenDetails);
    }

    public Collection<TokenDetails> getAllTokens() {
        return tokens.values();
    }

    private UserDetails toUserDetails(TokenDetails tokenDetails) {
        return User.withUsername(tokenDetails.token())
                .password(passwordEncoder.encode(tokenDetails.token()))
                .roles("USER")
                .build();
    }

    private boolean tokenExists(String token) {
        return this.tokens.containsKey(token.toLowerCase(Locale.ROOT));
    }

}
