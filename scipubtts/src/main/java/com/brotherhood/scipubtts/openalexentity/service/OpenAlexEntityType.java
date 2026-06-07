package com.brotherhood.scipubtts.openalexentity.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum OpenAlexEntityType {
    AUTHOR("author", "/authors", "authorships.author.id"),
    TOPIC("topic", "/topics", "topics.id"),
    INSTITUTION("institution", "/institutions", "authorships.institutions.id"),
    SOURCE("source", "/sources", "primary_location.source.id");

    private final String apiValue;
    private final String openAlexPath;
    private final String worksFilterKey;
    private static final Map<String, OpenAlexEntityType> LOOKUP = Arrays.stream(values())
            .collect(Collectors.toMap(
                    entityType -> entityType.apiValue.toLowerCase(Locale.ROOT),
                    Function.identity()
            ));

    OpenAlexEntityType(String apiValue, String openAlexPath, String worksFilterKey) {
        this.apiValue = apiValue;
        this.openAlexPath = openAlexPath;
        this.worksFilterKey = worksFilterKey;
    }

    public static OpenAlexEntityType fromValue(String rawValue) {
        if (rawValue == null) {
            throw new BusinessException(ErrorCode.INVALID_OPENALEX_ENTITY_TYPE);
        }

        OpenAlexEntityType entityType = LOOKUP.get(rawValue.trim().toLowerCase(Locale.ROOT));
        if (entityType != null) {
            return entityType;
        }

        throw new BusinessException(ErrorCode.INVALID_OPENALEX_ENTITY_TYPE);
    }

    public String buildEntityPath(String entityId) {
        return openAlexPath + "/" + entityId;
    }

    public String buildWorksFilter(String entityId) {
        return worksFilterKey + ":" + entityId;
    }
}
