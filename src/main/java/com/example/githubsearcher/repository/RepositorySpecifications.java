package com.example.githubsearcher.repository;

import com.example.githubsearcher.entity.RepositoryEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/** Reusable, composable filter predicates for stored repositories. */
public final class RepositorySpecifications {

    private RepositorySpecifications() {
    }

    public static Specification<RepositoryEntity> withFilters(String language, Long minStars) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(language)) {
                predicates.add(cb.equal(
                        cb.lower(root.get("language")), language.trim().toLowerCase(Locale.ROOT)));
            }
            if (minStars != null) {
                predicates.add(cb.ge(root.get("stars"), minStars));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
