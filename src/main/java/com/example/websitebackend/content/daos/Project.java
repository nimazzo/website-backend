package com.example.websitebackend.content.daos;

import java.util.List;

public record Project(
        String title,
        String description,
        List<String> techStack,
        String screenshotUrl,
        String githubUrl
) {
}