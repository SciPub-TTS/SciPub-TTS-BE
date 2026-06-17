package com.brotherhood.scipubtts.search.dto.request;

public record SearchEntityQueryRequest(
        String query,
        Integer page,
        Integer perPage
) {
}
