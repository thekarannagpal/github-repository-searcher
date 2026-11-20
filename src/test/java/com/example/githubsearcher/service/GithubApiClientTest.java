package com.example.githubsearcher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.githubsearcher.config.GithubApiProperties;
import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.dto.SearchRequest;
import com.example.githubsearcher.dto.github.GitHubRepository;
import com.example.githubsearcher.exception.GitHubApiException;
import com.example.githubsearcher.exception.RateLimitExceededException;
import java.net.URI;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class GithubApiClientTest {

    private GithubApiClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        GithubApiProperties properties = new GithubApiProperties();
        properties.setBaseUrl("https://api.github.com");
        properties.setPerPage(20);

        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new GithubApiClient(restTemplate, properties);
    }

    @Test
    void mapsGitHubResponseAndBuildsQuery() {
        server.expect(once(), requestTo(Matchers.containsString("sort=stars")))
                .andRespond(withSuccess("""
                        {
                          "total_count": 1,
                          "incomplete_results": false,
                          "items": [{
                            "id": 123,
                            "name": "spring-boot-example",
                            "description": "Example",
                            "owner": {"login": "user123"},
                            "language": "Java",
                            "stargazers_count": 450,
                            "forks_count": 120,
                            "updated_at": "2024-01-01T12:00:00Z",
                            "html_url": "https://github.com/user123/spring-boot-example",
                            "unknown_field": "ignored"
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<GitHubRepository> result = client.search(
                new SearchRequest("spring boot", "Java", SortOption.STARS));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(123L);
        assertThat(result.get(0).stargazersCount()).isEqualTo(450L);
        assertThat(result.get(0).owner().login()).isEqualTo("user123");
        assertThat(result.get(0).updatedAt()).isEqualTo(
                java.time.OffsetDateTime.parse("2024-01-01T12:00:00Z"));
        server.verify();
    }

    @Test
    void returnsEmptyListForNoResults() {
        server.expect(once(), requestTo(Matchers.anything()))
                .andRespond(withSuccess(
                        "{\"total_count\":0,\"incomplete_results\":false,\"items\":[]}",
                        MediaType.APPLICATION_JSON));

        assertThat(client.search(new SearchRequest("nothing", null, null))).isEmpty();
        server.verify();
    }

    @Test
    void translatesRateLimitResponse() {
        server.expect(once(), requestTo(Matchers.anything()))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .header("X-RateLimit-Remaining", "0")
                        .header("Retry-After", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"API rate limit exceeded\"}"));

        assertThatThrownBy(() -> client.search(new SearchRequest("spring", null, SortOption.STARS)))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("rate limit")
                .satisfies(e -> assertThat(((RateLimitExceededException) e).getRetryAfterSeconds())
                        .isEqualTo(42L));
    }

    @Test
    void translatesServerError() {
        server.expect(once(), requestTo(Matchers.anything()))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.search(new SearchRequest("spring", null, SortOption.STARS)))
                .isInstanceOf(GitHubApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void buildUriIncludesLanguageFilterAndSort() {
        URI uri = client.buildUri(new SearchRequest("spring boot", "Java", SortOption.FORKS));

        String query = java.net.URLDecoder.decode(uri.getQuery(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(query)
                .contains("q=spring boot language:Java")
                .contains("sort=forks")
                .contains("per_page=20");
    }
}
