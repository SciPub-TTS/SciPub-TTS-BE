package com.brotherhood.scipubtts.search.dto.response;

public record HotKeywordItemResponse(
        String keywordId,
        String name,
        Integer fieldId,
        Long works,
        Long citations
) {
}
