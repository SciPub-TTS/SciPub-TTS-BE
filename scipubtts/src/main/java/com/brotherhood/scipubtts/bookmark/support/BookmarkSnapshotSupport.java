package com.brotherhood.scipubtts.bookmark.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BookmarkSnapshotSupport {

    private final ObjectMapper objectMapper;

    public String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    public String normalizeEntityId(String value) {
        String normalizedValue = normalizeText(value);

        if (normalizedValue == null) {
            return null;
        }

        if (normalizedValue.contains("/")) {
            normalizedValue = normalizedValue.substring(
                    normalizedValue.lastIndexOf('/') + 1
            );
        }

        return normalizedValue.toUpperCase();
    }

    public String firstNonBlank(String primaryValue, String fallbackValue) {
        String normalizedPrimary = normalizeText(primaryValue);

        if (normalizedPrimary != null) {
            return normalizedPrimary;
        }

        return normalizeText(fallbackValue);
    }

    public String serializeEntityIds(List<String> rawIds) {
        if (rawIds == null || rawIds.isEmpty()) {
            return null;
        }

        List<String> normalizedIds = rawIds.stream()
                .map(this::normalizeEntityId)
                .toList();

        boolean hasAtLeastOneId = normalizedIds.stream().anyMatch(Objects::nonNull);

        if (!hasAtLeastOneId) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(normalizedIds);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize bookmark snapshot ids.", ex);
        }
    }

    public List<String> deserializeEntityIds(String rawSnapshot) {
        if (!StringUtils.hasText(rawSnapshot)) {
            return List.of();
        }

        try {
            List<String> ids = objectMapper.readerForListOf(String.class).readValue(rawSnapshot);

            if (ids == null || ids.isEmpty()) {
                return List.of();
            }

            return ids.stream()
                    .map(this::normalizeEntityId)
                    .toList();
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }
}
