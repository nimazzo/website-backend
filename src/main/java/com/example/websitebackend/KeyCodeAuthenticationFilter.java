package com.example.websitebackend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

public class KeyCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public KeyCodeAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/authenticate"), authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        var keyCode = request.getParameter("code");

        if (keyCode == null || keyCode.isEmpty()) {
            throw new AuthenticationException("Key code must not be empty") {
            };
        }

        var token = new KeyCodeAuthenticationToken(keyCode);
        return getAuthenticationManager().authenticate(token);
    }
}
