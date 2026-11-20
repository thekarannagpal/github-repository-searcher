package com.example.githubsearcher.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/** Minimal projection of the GitHub search API repository item. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepository(
        long id,
        String name,
        String description,
        GitHubOwner owner,
        String language,
        @JsonProperty("stargazers_count") long stargazersCount,
        @JsonProperty("forks_count") long forksCount,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("pushed_at") OffsetDateTime pushedAt,
        @JsonProperty("watchers_count") long watchersCount,
        @JsonProperty("open_issues_count") long openIssuesCount,
        @JsonProperty("html_url") String htmlUrl
) {
}
