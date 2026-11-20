package com.example.githubsearcher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.dto.RepositoryResponse;
import com.example.githubsearcher.entity.RepositoryEntity;
import com.example.githubsearcher.mapper.RepositoryMapper;
import com.example.githubsearcher.service.GithubSearchService;
import com.example.githubsearcher.service.RepositoryService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GithubController.class)
class GithubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GithubSearchService githubSearchService;

    @MockBean
    private RepositoryService repositoryService;

    @MockBean
    private RepositoryMapper mapper;

    @Test
    void searchReturnsSavedRepositories() throws Exception {
        RepositoryResponse repo = new RepositoryResponse(
                123L, "spring-boot-example", "Example", "user123", "Java",
                450L, 120L, OffsetDateTime.parse("2024-01-01T12:00:00Z"),
                "https://github.com/user123/spring-boot-example", 5L, 8L);
        when(githubSearchService.searchAndStore(any()))
                .thenReturn(new com.example.githubsearcher.dto.SearchResponse(
                        "Repositories fetched and saved successfully", List.of(repo)));

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"query":"spring boot","language":"Java","sort":"stars"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Repositories fetched and saved successfully"))
                .andExpect(jsonPath("$.repositories[0].id").value(123))
                .andExpect(jsonPath("$.repositories[0].owner").value("user123"));
    }

    @Test
    void searchRejectsBlankQuery() throws Exception {
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.query").exists());
    }

    @Test
    void getRepositoriesAppliesFilters() throws Exception {
        when(repositoryService.findAll("Java", 100L, SortOption.STARS))
                .thenReturn(List.of(RepositoryEntity.builder().id(1L).build()));
        when(mapper.toResponses(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100")
                        .param("sort", "stars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray());

        verify(repositoryService).findAll("Java", 100L, SortOption.STARS);
    }

    @Test
    void getRepositoriesUsesStarsAsDefaultSort() throws Exception {
        when(repositoryService.findAll(null, null, SortOption.STARS)).thenReturn(List.of());
        when(mapper.toResponses(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/github/repositories"))
                .andExpect(status().isOk());

        verify(repositoryService).findAll(null, null, SortOption.STARS);
    }

    @Test
    void getRepositoriesRejectsUnknownSort() throws Exception {
        mockMvc.perform(get("/api/github/repositories").param("sort", "bogus"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Unsupported sort")));
    }

    @Test
    void getRepositoriesRejectsNegativeMinStars() throws Exception {
        mockMvc.perform(get("/api/github/repositories").param("minStars", "-1"))
                .andExpect(status().isBadRequest());
    }
}
