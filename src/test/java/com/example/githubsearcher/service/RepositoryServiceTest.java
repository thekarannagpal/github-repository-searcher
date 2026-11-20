package com.example.githubsearcher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.entity.RepositoryEntity;
import com.example.githubsearcher.repository.RepositoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class RepositoryServiceTest {

    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private RepositoryService service;

    @Test
    void upsertSavesAllEntities() {
        RepositoryEntity entity = RepositoryEntity.builder().id(1L).build();
        when(repositoryRepository.saveAll(List.of(entity))).thenReturn(List.of(entity));

        assertThat(service.upsertAll(List.of(entity))).containsExactly(entity);
        verify(repositoryRepository).saveAll(List.of(entity));
    }

    @Test
    void upsertSkipsEmptyInput() {
        assertThat(service.upsertAll(List.of())).isEmpty();
        verify(repositoryRepository, never()).saveAll(any());
    }

    @Test
    void findAllPassesFiltersAndDescendingSort() {
        when(repositoryRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        service.findAll("Java", 100L, SortOption.UPDATED);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Specification<RepositoryEntity>> specCaptor =
                ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(repositoryRepository).findAll(specCaptor.capture(), sortCaptor.capture());

        assertThat(specCaptor.getValue()).isNotNull();
        Sort.Order order = sortCaptor.getValue().getOrderFor("lastUpdated");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void findAllDefaultsToStarsWhenSortIsMissing() {
        when(repositoryRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        service.findAll(null, null, null);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(repositoryRepository).findAll(any(Specification.class), sortCaptor.capture());
        assertThat(sortCaptor.getValue().getOrderFor("stars")).isNotNull();
    }
}
