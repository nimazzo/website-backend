package com.example.websitebackend;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SessionListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(SessionListener.class);

    @Value("${server.servlet.session.timeout:30m}")
    private Duration maxInactiveInterval;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        var session = se.getSession();

        log.info("Session created: {}. Set timeout to {} seconds", session.getId(), maxInactiveInterval.toSeconds());
        session.setMaxInactiveInterval((int) maxInactiveInterval.toSeconds());
    }
}
