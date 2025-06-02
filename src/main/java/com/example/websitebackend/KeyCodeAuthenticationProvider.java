package com.example.websitebackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class KeyCodeAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(KeyCodeAuthenticationProvider.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public KeyCodeAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("Authenticating with key code: {}", authentication.getCredentials());
        var keyCode = authentication.getCredentials().toString();

        var user = userDetailsService.loadUserByUsername(keyCode);

        if (passwordEncoder.matches(keyCode, user.getPassword())) {
            log.info("Key code authenticated successfully for user: {}", user.getUsername());
            return KeyCodeAuthenticationToken.authenticated(keyCode, user.getAuthorities());
        }

        log.warn("Invalid key code provided: {}", keyCode);
        throw new BadCredentialsException("Invalid key code");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        log.info("Checking if authentication is supported: {}", authentication.getName());
        return KeyCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
