package com.example.websitebackend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties("security")
public record SecurityProperties(
        @DefaultValue("admin")
        String adminUsername,

        @DefaultValue("admin")
        String adminPassword,

        @DefaultValue("false")
        boolean createPublicToken,

        @DefaultValue("24h")
        Duration bruteforceDefenderBlockTime
) {
}