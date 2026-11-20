package com.example.githubsearcher.dto;

import com.example.githubsearcher.domain.SortOption;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Criteria used to search GitHub repositories")
public record SearchRequest(
        @NotBlank(message = "query must not be blank")
        @Schema(description = "Repository name or keyword", example = "spring boot")
        String query,

        @Schema(description = "Optional programming language filter", example = "Java")
        String language,

        @Schema(description = "Sort key: stars, forks or updated", example = "stars")
        SortOption sort
) {
    public SearchRequest {
        query = query == null ? null : query.trim();
        language = (language == null || language.isBlank()) ? null : language.trim();
        sort = sort == null ? SortOption.STARS : sort;
    }
}
