package com.example.githubsearcher.dto;

import java.time.OffsetDateTime;

public record RepositoryResponse(
        long id,
        String name,
        String description,
        String owner,
        String language,
        long stars,
        long forks,
        OffsetDateTime lastUpdated,
        String htmlUrl,
        long openIssues,
        long watchers
) {
}
