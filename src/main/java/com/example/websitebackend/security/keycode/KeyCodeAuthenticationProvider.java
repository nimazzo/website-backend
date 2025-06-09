package com.example.websitebackend.security.keycode;

import com.example.websitebackend.security.bruteforce.BlockedException;
import com.example.websitebackend.security.bruteforce.BruteForceDefender;
import com.example.websitebackend.security.tracking.AuthenticationTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class KeyCodeAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(KeyCodeAuthenticationProvider.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final BruteForceDefender bruteForceDefender;
    private final AuthenticationTracker authenticationTracker;

    public KeyCodeAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, BruteForceDefender bruteForceDefender, AuthenticationTracker authenticationTracker) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.bruteForceDefender = bruteForceDefender;
        this.authenticationTracker = authenticationTracker;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("Authenticating with key code: {}", authentication.getCredentials());
        var keyCode = authentication.getCredentials().toString();

        var clientIP = getClientIP();

        if (bruteForceDefender.isBlocked(clientIP)) {
            log.warn("Authentication blocked due to too many failed attempts for client ip: {} using key code: {}",
                    clientIP, keyCode);
            throw new BlockedException("Too many failed attempts, please try again later.");
        }

        try {
            var user = userDetailsService.loadUserByUsername(keyCode);

            if (passwordEncoder.matches(keyCode, user.getPassword())) {
                log.info("Key code authenticated successfully for user: {}", user.getUsername());
                bruteForceDefender.loginSucceeded(clientIP);
                authenticationTracker.trackSuccessfulAuthentication(keyCode);
                return KeyCodeAuthenticationToken.authenticated(keyCode, user.getAuthorities());
            }
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.error("Error during authentication for key code: {}", keyCode, e);
            bruteForceDefender.loginFailed(clientIP);
            throw e;
        }

        throw new IllegalStateException("Authentication failed, no user found for key code: " + keyCode);
    }

    private String getClientIP() {
        var request = RequestContextHolder.currentRequestAttributes();
        if (request instanceof ServletRequestAttributes servletRequestAttributes) {
            var servletRequest = servletRequestAttributes.getRequest();

            var cfIp = servletRequest.getHeader("CF-Connecting-IP");
            log.info("CF-Connecting-IP: {}", cfIp);
            if (cfIp != null && !cfIp.isBlank()) {
                return cfIp;
            }

            var xForwardedFor = servletRequest.getHeader("X-Forwarded-For");
            log.info("X-Forwarded-For: {}", xForwardedFor);
            if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                return xForwardedFor.split(",")[0].trim();
            }

            return servletRequest.getRemoteAddr();
        }
        throw new IllegalStateException("No request attributes found to determine client IP");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        log.info("Checking if authentication is supported: {}", authentication.getName());
        return KeyCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
