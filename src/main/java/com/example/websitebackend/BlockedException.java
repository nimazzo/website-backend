package com.example.websitebackend;

import org.springframework.security.core.AuthenticationException;

public class BlockedException extends AuthenticationException {
    public BlockedException(String msg) {
        super(msg);
    }
}
