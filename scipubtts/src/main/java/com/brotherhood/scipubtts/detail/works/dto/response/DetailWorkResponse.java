package com.brotherhood.scipubtts.detail.works.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DetailWorkResponse(
        String id,
        String entityType,
        String title,
        List<String> authors,
        List<EntityRef> authorRefs,
        String source,
        int citations,
        int year,
        @JsonProperty("abstract")
        String abstractText,
        String fullText,
        String doi,
        String pdfUrl,
        List<String> keywords,
        String field,
        String topic,
        EntityRef topicRef,
        String subField,
        int growthPercent,
        boolean saved
) {
    public record EntityRef(
            String id,
            String name
    ) {
    }
}
