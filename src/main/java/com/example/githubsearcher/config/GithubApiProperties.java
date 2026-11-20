package com.example.githubsearcher.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "github.api")
public class GithubApiProperties {

    @NotBlank
    private String baseUrl = "https://api.github.com";

    @Min(1)
    private int perPage = 20;

    /** Optional GitHub token. When set, it is sent as a Bearer token. */
    private String token = "";

    @Min(1)
    private int connectTimeoutMs = 3000;

    @Min(1)
    private int readTimeoutMs = 5000;
}
