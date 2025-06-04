package com.example.websitebackend.content.daos;

import java.util.List;

public record Skill(
        String title,
        List<Item> items
) {
}
