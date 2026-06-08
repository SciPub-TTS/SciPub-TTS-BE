package com.brotherhood.scipubtts.canvas.service;

import com.brotherhood.scipubtts.common.exception.BusinessException;
import com.brotherhood.scipubtts.common.exception.ErrorCode;

public enum CanvasEntityType {
    AUTHOR("author", "/authors", "authorships.author.id"),
    TOPIC("topic", "/topics", "topics.id"),
    INSTITUTION("institution", "/institutions", "authorships.institutions.id"),
    SOURCE("source", "/sources", "primary_location.source.id");

    private final String apiValue;
    private final String openAlexPath;
    private final String worksFilterKey;

    CanvasEntityType(String apiValue, String openAlexPath, String worksFilterKey) {
        this.apiValue = apiValue;
        this.openAlexPath = openAlexPath;
        this.worksFilterKey = worksFilterKey;
    }

    public static CanvasEntityType fromValue(String rawValue) {
        if (rawValue == null) {
            throw new BusinessException(ErrorCode.INVALID_CANVAS_ENTITY_TYPE);
        }

        for (CanvasEntityType entityType : values()) {
            if (entityType.apiValue.equalsIgnoreCase(rawValue.trim())) {
                return entityType;
            }
        }

        throw new BusinessException(ErrorCode.INVALID_CANVAS_ENTITY_TYPE);
    }

    public String buildEntityPath(String entityId) {
        return openAlexPath + "/" + entityId;
    }

    public String buildWorksFilter(String entityId) {
        return worksFilterKey + ":" + entityId;
    }
}
