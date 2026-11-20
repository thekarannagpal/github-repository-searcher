package com.example.githubsearcher.service;

import com.example.githubsearcher.dto.RepositoryResponse;
import com.example.githubsearcher.dto.SearchRequest;
import com.example.githubsearcher.dto.SearchResponse;
import com.example.githubsearcher.dto.github.GitHubRepository;
import com.example.githubsearcher.entity.RepositoryEntity;
import com.example.githubsearcher.mapper.RepositoryMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GithubSearchService {

    static final String SAVED_MESSAGE = "Repositories fetched and saved successfully";
    static final String EMPTY_MESSAGE = "No repositories found for the given criteria.";

    private final GithubApiClient githubApiClient;
    private final RepositoryMapper mapper;
    private final RepositoryService repositoryService;

    public SearchResponse searchAndStore(SearchRequest request) {
        List<GitHubRepository> found = githubApiClient.search(request);

        if (found.isEmpty()) {
            return new SearchResponse(EMPTY_MESSAGE, List.of());
        }

        List<RepositoryEntity> entities = mapper.toEntities(found);
        List<RepositoryEntity> stored = repositoryService.upsertAll(entities);
        List<RepositoryResponse> response = mapper.toResponses(stored);

        return new SearchResponse(SAVED_MESSAGE, response);
    }
}
