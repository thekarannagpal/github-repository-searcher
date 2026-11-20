package com.example.githubsearcher.mapper;

import com.example.githubsearcher.dto.RepositoryResponse;
import com.example.githubsearcher.dto.github.GitHubRepository;
import com.example.githubsearcher.entity.RepositoryEntity;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMapper {

    public RepositoryEntity toEntity(GitHubRepository source) {
        return RepositoryEntity.builder()
                .id(source.id())
                .name(source.name())
                .description(source.description() == null ? "" : source.description())
                .owner(source.owner() == null ? null : source.owner().login())
                .language(source.language())
                .stars(source.stargazersCount())
                .forks(source.forksCount())
                .lastUpdated(source.updatedAt() == null
                        ? OffsetDateTime.now(ZoneOffset.UTC) : source.updatedAt())
                .createdAt(source.createdAt())
                .pushedAt(source.pushedAt())
                .htmlUrl(source.htmlUrl())
                .openIssues(source.openIssuesCount())
                .watchers(source.watchersCount())
                .build();
    }

    public RepositoryResponse toResponse(RepositoryEntity source) {
        return new RepositoryResponse(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getOwner(),
                source.getLanguage(),
                source.getStars(),
                source.getForks(),
                source.getLastUpdated(),
                source.getHtmlUrl(),
                source.getOpenIssues(),
                source.getWatchers());
    }

    public List<RepositoryEntity> toEntities(List<GitHubRepository> source) {
        return source.stream().map(this::toEntity).toList();
    }

    public List<RepositoryResponse> toResponses(List<RepositoryEntity> source) {
        return source.stream().map(this::toResponse).toList();
    }
}
