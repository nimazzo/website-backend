package com.example.websitebackend.content.daos;

import java.util.List;

public record ContentData(
        About about,
        List<Education> education,
        List<Skill> skills,
        List<Project> projects
) {
}