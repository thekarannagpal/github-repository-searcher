package com.example.githubsearcher.service;

import com.example.githubsearcher.config.GithubApiProperties;
import com.example.githubsearcher.dto.SearchRequest;
import com.example.githubsearcher.dto.github.GitHubRepository;
import com.example.githubsearcher.dto.github.GitHubSearchResponse;
import com.example.githubsearcher.exception.GitHubApiException;
import com.example.githubsearcher.exception.RateLimitExceededException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Talks to the GitHub REST API and translates transport errors into domain exceptions. */
@Service
@RequiredArgsConstructor
public class GithubApiClient {

    static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";
    static final String RETRY_AFTER = HttpHeaders.RETRY_AFTER;

    private final RestTemplate githubRestTemplate;
    private final GithubApiProperties properties;

    public List<GitHubRepository> search(SearchRequest request) {
        URI uri = buildUri(request);
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(properties.getToken())) {
            headers.setBearerAuth(properties.getToken());
        }

        try {
            ResponseEntity<GitHubSearchResponse> response = githubRestTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubSearchResponse.class);

            GitHubSearchResponse body = response.getBody();
            return body == null ? List.of() : body.items();
        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw rateLimitException(ex);
        } catch (HttpClientErrorException.Forbidden ex) {
            String remaining = header(ex, RATE_LIMIT_REMAINING);
            if ("0".equals(remaining) || isRateLimitMessage(ex)) {
                throw rateLimitException(ex);
            }
            throw new GitHubApiException("GitHub API rejected the request: " + ex.getStatusText(), ex);
        } catch (HttpStatusCodeException ex) {
            throw new GitHubApiException(
                    "GitHub API returned " + ex.getStatusCode().value() + " " + ex.getStatusText(), ex);
        } catch (RestClientException ex) {
            throw new GitHubApiException("Unable to reach GitHub API: " + ex.getMessage(), ex);
        }
    }

    URI buildUri(SearchRequest request) {
        String query = request.query();
        if (StringUtils.hasText(request.language())) {
            query = query + " language:" + request.language();
        }

        return UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                .path("/search/repositories")
                .queryParam("q", query)
                .queryParam("sort", request.sort().apiValue())
                .queryParam("order", "desc")
                .queryParam("per_page", properties.getPerPage())
                .build()
                .encode()
                .toUri();
    }

    private boolean isRateLimitMessage(HttpStatusCodeException ex) {
        String body = ex.getResponseBodyAsString();
        return body != null && body.toLowerCase(Locale.ROOT).contains("rate limit");
    }

    private RateLimitExceededException rateLimitException(HttpStatusCodeException ex) {
        Long retryAfter = parseLong(header(ex, RETRY_AFTER));

        if (retryAfter == null) {
            Long resetEpochSeconds = parseLong(header(ex, RATE_LIMIT_RESET));
            if (resetEpochSeconds != null) {
                retryAfter = Math.max(0L, resetEpochSeconds - Instant.now().getEpochSecond());
            }
        }

        String message = "GitHub API rate limit exceeded"
                + (retryAfter == null ? "." : ". Retry in " + retryAfter + " seconds.");
        return new RateLimitExceededException(message, retryAfter);
    }

    private String header(HttpStatusCodeException ex, String name) {
        HttpHeaders headers = ex.getResponseHeaders();
        return headers == null ? null : headers.getFirst(name);
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
