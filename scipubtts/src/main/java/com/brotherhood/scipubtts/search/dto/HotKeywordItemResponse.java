package com.brotherhood.scipubtts.search.dto;

public record HotKeywordItemResponse(
        String keywordId,
        String name,
        Integer fieldId,
        Long works,
        Long citations
) {
}
