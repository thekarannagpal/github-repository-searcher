package com.example.githubsearcher.controller;

import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.dto.RepositoryListResponse;
import com.example.githubsearcher.dto.SearchRequest;
import com.example.githubsearcher.dto.SearchResponse;
import com.example.githubsearcher.entity.RepositoryEntity;
import com.example.githubsearcher.mapper.RepositoryMapper;
import com.example.githubsearcher.service.GithubSearchService;
import com.example.githubsearcher.service.RepositoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Validated
@Tag(name = "GitHub repositories", description = "Search GitHub and query locally stored results")
public class GithubController {

    private final GithubSearchService githubSearchService;
    private final RepositoryService repositoryService;
    private final RepositoryMapper mapper;

    @PostMapping("/search")
    @Operation(summary = "Search GitHub and persist the results")
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(githubSearchService.searchAndStore(request));
    }

    @GetMapping("/repositories")
    @Operation(summary = "Retrieve stored repositories with optional filters")
    public ResponseEntity<RepositoryListResponse> repositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @Min(value = 0, message = "minStars must be >= 0") Long minStars,
            @RequestParam(defaultValue = "stars") String sort) {

        List<RepositoryEntity> found = repositoryService.findAll(
                language, minStars, SortOption.from(sort));
        return ResponseEntity.ok(new RepositoryListResponse(mapper.toResponses(found)));
    }
}
