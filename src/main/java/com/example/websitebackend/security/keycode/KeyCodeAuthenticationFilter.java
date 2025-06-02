package com.example.websitebackend.security.keycode;

import com.example.websitebackend.security.bruteforce.BlockedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

public class KeyCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String MESSAGE_TEMPLATE = """
            {
                "status": "%s",
                "reason": "%s",
                "message": "%s"
            }
            """;

    public KeyCodeAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/authenticate"), authenticationManager);
        setSecurityContextRepository(new HttpSessionSecurityContextRepository());
        setAuthenticationSuccessHandler(onAuthenticationSuccess());
        setAuthenticationFailureHandler(onAuthenticationFailure());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        var keyCode = request.getParameter("code");

        if (keyCode == null || keyCode.isEmpty()) {
            throw new AuthenticationException("Key code must not be empty") {
            };
        }

        var token = KeyCodeAuthenticationToken.unauthenticated(keyCode);
        return getAuthenticationManager().authenticate(token);
    }

    private AuthenticationFailureHandler onAuthenticationFailure() {
        return (_, res, exception) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");

            String message;
            if (exception instanceof BlockedException) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                message = String.format(MESSAGE_TEMPLATE, "error", "blocked", exception.getLocalizedMessage());
            } else {
                message = String.format(MESSAGE_TEMPLATE, "error", "UNAUTHORIZED", "Full authentication is required to access this resource");
            }

            res.getWriter().write(message);
        };
    }

    private static AuthenticationSuccessHandler onAuthenticationSuccess() {
        return (_, res, _) -> {
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write(String.format(MESSAGE_TEMPLATE, "success", "authenticated", "Successfully authenticated with key code"));
        };
    }
}
