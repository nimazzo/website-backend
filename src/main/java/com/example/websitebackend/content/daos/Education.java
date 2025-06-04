package com.example.websitebackend.content.daos;

public record Education(
        String institution,
        String degree,
        String field,
        String startDate,
        String endDate,
        String description,
        String grade,
        String icon
) {
}