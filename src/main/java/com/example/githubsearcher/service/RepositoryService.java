package com.example.githubsearcher.service;

import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.entity.RepositoryEntity;
import com.example.githubsearcher.repository.RepositoryRepository;
import com.example.githubsearcher.repository.RepositorySpecifications;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;

    /**
     * Saves using GitHub ids as primary keys, so repeated searches update existing rows
     * instead of creating duplicates (JPA upsert semantics).
     */
    @Transactional
    public List<RepositoryEntity> upsertAll(List<RepositoryEntity> repositories) {
        if (repositories.isEmpty()) {
            return List.of();
        }
        return repositoryRepository.saveAll(repositories);
    }

    @Transactional(readOnly = true)
    public List<RepositoryEntity> findAll(String language, Long minStars, SortOption sort) {
        Sort ordering = (sort == null ? SortOption.STARS : sort).toSort();
        return repositoryRepository.findAll(
                RepositorySpecifications.withFilters(language, minStars), ordering);
    }
}
