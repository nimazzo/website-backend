package com.example.websitebackend;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class KeyCodeAuthenticationToken extends AbstractAuthenticationToken {

    private final String keyCode;

    public KeyCodeAuthenticationToken(String keyCode) {
        this(keyCode, null);
    }

    public KeyCodeAuthenticationToken(String keyCode, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.keyCode = keyCode;
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
