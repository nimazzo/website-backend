package com.example.websitebackend.content.daos;

import java.util.List;

public record Skill(
        String title,
        boolean withLevels,
        List<Item> items
) {
}
