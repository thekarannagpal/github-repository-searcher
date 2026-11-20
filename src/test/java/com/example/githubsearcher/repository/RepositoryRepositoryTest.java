package com.example.githubsearcher.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.githubsearcher.domain.SortOption;
import com.example.githubsearcher.entity.RepositoryEntity;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryRepositoryTest {

    @Autowired
    private RepositoryRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(List.of(
                repo(1L, "spring-boot", "Java", 450, 120, "2024-01-01T12:00:00Z"),
                repo(2L, "hibernate-orm", "Java", 900, 300, "2024-03-01T12:00:00Z"),
                repo(3L, "django", "Python", 800, 400, "2024-02-01T12:00:00Z")));
        repository.flush();
    }

    @Test
    void filtersByLanguageAndMinimumStars() {
        List<RepositoryEntity> result = repository.findAll(
                RepositorySpecifications.withFilters("java", 500L), SortOption.STARS.toSort());

        assertThat(result).extracting(RepositoryEntity::getId).containsExactly(2L);
    }

    @Test
    void filtersByMinimumStarsOnly() {
        List<RepositoryEntity> result = repository.findAll(
                RepositorySpecifications.withFilters(null, 500L), SortOption.STARS.toSort());

        assertThat(result).extracting(RepositoryEntity::getId).containsExactly(2L, 3L);
    }

    @Test
    void sortsByUpdatedDateDescending() {
        List<RepositoryEntity> result = repository.findAll(
                RepositorySpecifications.withFilters(null, null), SortOption.UPDATED.toSort());

        assertThat(result).extracting(RepositoryEntity::getId).containsExactly(2L, 3L, 1L);
    }

    @Test
    void savingWithExistingIdUpdatesInsteadOfDuplicating() {
        repository.saveAndFlush(RepositoryEntity.builder()
                .id(1L)
                .name("spring-boot")
                .description("updated")
                .owner("user123")
                .language("Java")
                .stars(999)
                .lastUpdated(OffsetDateTime.parse("2024-01-01T12:00:00Z"))
                .build());

        assertThat(repository.count()).isEqualTo(3);
        assertThat(repository.findById(1L).orElseThrow().getStars()).isEqualTo(999);
    }

    private RepositoryEntity repo(long id, String name, String language, long stars,
                                  long forks, String lastUpdated) {
        return RepositoryEntity.builder()
                .id(id)
                .name(name)
                .description("description " + name)
                .owner("owner" + id)
                .language(language)
                .stars(stars)
                .forks(forks)
                .lastUpdated(OffsetDateTime.parse(lastUpdated))
                .build();
    }
}
