package com.example.githubsearcher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.dto.RepositoryResponse;
import com.example.githubsearcher.dto.SearchRequest;
import com.example.githubsearcher.dto.SearchResponse;
import com.example.githubsearcher.dto.github.GitHubRepository;
import com.example.githubsearcher.entity.RepositoryEntity;
import com.example.githubsearcher.mapper.RepositoryMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GithubSearchServiceTest {

    @Mock
    private GithubApiClient githubApiClient;

    @Mock
    private RepositoryMapper mapper;

    @Mock
    private RepositoryService repositoryService;

    @InjectMocks
    private GithubSearchService service;

    @Test
    void fetchesMapsAndStoresResults() {
        SearchRequest request = new SearchRequest("spring boot", "Java", SortOption.STARS);
        GitHubRepository apiRepo = org.mockito.Mockito.mock(GitHubRepository.class);
        RepositoryEntity entity = RepositoryEntity.builder().id(1L).name("repo").build();
        RepositoryEntity stored = RepositoryEntity.builder().id(1L).name("repo").build();
        RepositoryResponse response = new RepositoryResponse(
                1L, "repo", null, null, null, 0, 0, null, null, 0, 0);

        when(githubApiClient.search(request)).thenReturn(List.of(apiRepo));
        when(mapper.toEntities(List.of(apiRepo))).thenReturn(List.of(entity));
        when(repositoryService.upsertAll(List.of(entity))).thenReturn(List.of(stored));
        when(mapper.toResponses(List.of(stored))).thenReturn(List.of(response));

        SearchResponse result = service.searchAndStore(request);

        assertThat(result.message()).isEqualTo(GithubSearchService.SAVED_MESSAGE);
        assertThat(result.repositories()).containsExactly(response);
        verify(repositoryService).upsertAll(List.of(entity));
    }

    @Test
    void emptySearchResultIsNotStored() {
        SearchRequest request = new SearchRequest("nothing", null, null);
        when(githubApiClient.search(request)).thenReturn(List.of());

        SearchResponse result = service.searchAndStore(request);

        assertThat(result.message()).isEqualTo(GithubSearchService.EMPTY_MESSAGE);
        assertThat(result.repositories()).isEmpty();
        verify(repositoryService, never()).upsertAll(org.mockito.ArgumentMatchers.anyList());
    }
}
