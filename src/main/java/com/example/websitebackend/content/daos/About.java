package com.example.websitebackend.content.daos;

import java.util.List;

public record About(
        String name,
        String domain,
        List<String> description,
        String photo,
        String country,
        GitHub github,
        CV cv,
        String email,
        String footer
) {
}
