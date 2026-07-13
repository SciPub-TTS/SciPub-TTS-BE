package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SearchEntityQueryRequest")
public record SearchEntityQueryRequest(
        @Schema(nullable = true, example = "john")
        String query,

        @Schema(nullable = true, example = "[\"I201448701\"]")
        List<String> institution,

        @Schema(nullable = true, example = "[\"VN\"]")
        List<String> country,

        @Schema(nullable = true, example = "[\"T10017\"]")
        List<String> primaryTopic,

        @Schema(nullable = true, example = "[\"2202\"]")
        List<String> subField,

        @Schema(nullable = true, example = "[\"17\"]")
        List<String> field,

        @Schema(nullable = true, allowableValues = {"relevance", "works", "alphabetical"}, example = "works")
        String sortBy,

        @Schema(nullable = true, allowableValues = {"asc", "desc"}, example = "desc")
        String sortDirection,

        @Schema(nullable = true, example = "1", defaultValue = "1")
        Integer page,

        @Schema(nullable = true, example = "20", defaultValue = "20")
        Integer perPage
) {
}
