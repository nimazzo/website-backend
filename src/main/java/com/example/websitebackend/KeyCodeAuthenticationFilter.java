package com.example.websitebackend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

public class KeyCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public KeyCodeAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/authenticate"), authenticationManager);
        setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        setAuthenticationSuccessHandler((_, res, _) -> {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"status\": \"authenticated\"}");
        });
        setAuthenticationFailureHandler((_, res, exception) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"status\": \"unauthenticated\", \"error\": \"" + exception.getMessage() + "\"}");
        });
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        var keyCode = request.getParameter("code");

        if (keyCode == null || keyCode.isEmpty()) {
            throw new AuthenticationException("Key code must not be empty") {
            };
        }

        request.getSession(true).setMaxInactiveInterval(10);
        var token = KeyCodeAuthenticationToken.unauthenticated(keyCode);
        return getAuthenticationManager().authenticate(token);
    }
}
