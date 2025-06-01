package com.example.websitebackend;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class KeyCodeAuthenticationToken extends AbstractAuthenticationToken {

    private final String keyCode;

    private KeyCodeAuthenticationToken(String keyCode) {
        super(null);
        this.keyCode = keyCode;
        setAuthenticated(false);
    }

    private KeyCodeAuthenticationToken(String keyCode, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.keyCode = keyCode;
        setAuthenticated(true);
    }

    public static KeyCodeAuthenticationToken authenticated(String keyCode, Collection<? extends GrantedAuthority> authorities) {
        return new KeyCodeAuthenticationToken(keyCode, authorities);
    }

    public static KeyCodeAuthenticationToken unauthenticated(String keyCode) {
        return new KeyCodeAuthenticationToken(keyCode);
    }

    @Override
    public Object getCredentials() {
        return keyCode;
    }

    @Override
    public Object getPrincipal() {
        return keyCode;
    }
}
