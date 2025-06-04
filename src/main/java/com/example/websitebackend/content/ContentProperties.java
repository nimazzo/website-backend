package com.example.websitebackend.content;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("content")
public record ContentProperties(
        String path,
        @DefaultValue("content.json")
        String jsonFileName
) {
}
