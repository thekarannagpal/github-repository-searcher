package com.example.githubsearcher.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.data.domain.Sort;

/** Supported sort keys. The same enum drives GitHub queries, JPA ordering and request validation. */
public enum SortOption {
    STARS("stars", "stars"),
    FORKS("forks", "forks"),
    UPDATED("updated", "lastUpdated");

    private final String apiValue;
    private final String jpaProperty;

    SortOption(String apiValue, String jpaProperty) {
        this.apiValue = apiValue;
        this.jpaProperty = jpaProperty;
    }

    @JsonValue
    public String apiValue() {
        return apiValue;
    }

    public String jpaProperty() {
        return jpaProperty;
    }

    public Sort toSort() {
        return Sort.by(Sort.Order.desc(jpaProperty));
    }

    @JsonCreator
    public static SortOption from(String value) {
        if (value == null || value.isBlank()) {
            return STARS;
        }
        for (SortOption option : values()) {
            if (option.apiValue.equalsIgnoreCase(value) || option.name().equalsIgnoreCase(value)) {
                return option;
            }
        }
        throw new IllegalArgumentException(
                "Unsupported sort '" + value + "'. Allowed values: stars, forks, updated");
    }
}
