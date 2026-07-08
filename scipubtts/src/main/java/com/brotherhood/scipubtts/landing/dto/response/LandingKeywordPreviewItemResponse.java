package com.brotherhood.scipubtts.landing.dto.response;

public record LandingKeywordPreviewItemResponse(
        String keywordId,
        String name,
        Integer fieldId,
        Long works,
        Long citations
) {
}
