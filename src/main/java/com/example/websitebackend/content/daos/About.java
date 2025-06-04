package com.example.websitebackend.content.daos;

import java.util.List;

public record About(
        String name,
        List<String> description,
        String photo,
        String country,
        GitHub github,
        String email,
        String footer
) {
}
