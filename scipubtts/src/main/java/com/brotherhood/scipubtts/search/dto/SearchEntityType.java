package com.brotherhood.scipubtts.search.dto;

import org.springframework.util.StringUtils;

import java.util.Locale;

public enum SearchEntityType {
    WORKS("works",
            "/works",
            "id"),
    AUTHORS(
            "authors",
            "/authors",
            "id,display_name,last_known_institutions,topics,works_count"
    ),
    TOPICS(
            "topics",
            "/topics",
            "id,display_name,subfield,field,domain,works_count"
    );

    private final String parameterValue;
    private final String path;
    private final String selectFields;

    SearchEntityType(String parameterValue, String path, String selectFields) {
        this.parameterValue = parameterValue;
        this.path = path;
        this.selectFields = selectFields;
    }

    public static SearchEntityType fromParameter(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return WORKS;
        }

        String normalizedValue = rawValue.trim().toLowerCase(Locale.ROOT);

        for (SearchEntityType entityType : values()) {
            if (entityType.parameterValue.equals(normalizedValue)) {
                return entityType;
            }
        }

        return WORKS;
    }

    public String parameterValue() {
        return parameterValue;
    }

    public String path() {
        return path;
    }

    public String selectFields() {
        return selectFields;
    }
}
