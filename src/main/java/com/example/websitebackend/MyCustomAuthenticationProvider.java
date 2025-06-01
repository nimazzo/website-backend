package com.example.websitebackend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class MyCustomAuthenticationProvider implements AuthenticationProvider {

    private static final Logger log = LoggerFactory.getLogger(MyCustomAuthenticationProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("Authenticating with key code: {}", authentication.getCredentials());
        var keyCode = authentication.getCredentials().toString();

        if (keyCode.equals("00000000")) {
            return KeyCodeAuthenticationToken.authenticated(keyCode,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }

        throw new BadCredentialsException("Invalid key code");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        log.info("Checking if authentication is supported: {}", authentication.getName());
        return KeyCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
