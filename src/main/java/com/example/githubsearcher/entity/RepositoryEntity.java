package com.example.githubsearcher.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repositories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryEntity {

    /** GitHub's repository id is used as the primary key, which makes writes idempotent. */
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "owner_login")
    private String owner;

    private String language;

    private long stars;

    private long forks;

    @Column(name = "last_updated", nullable = false)
    private OffsetDateTime lastUpdated;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "pushed_at")
    private OffsetDateTime pushedAt;

    @Column(name = "html_url")
    private String htmlUrl;

    @Column(name = "open_issues")
    private long openIssues;

    private long watchers;
}
