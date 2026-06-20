package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SearchEntityQueryRequest",
        description = "Search parameters for author/topic entity lookups."
)
public record SearchEntityQueryRequest(
        @Schema(nullable = true, example = "john")
        String query,

        @Schema(nullable = true, example = "[\"I201448701\"]", description = "Institution ids for author search.")
        List<String> institution,

        @Schema(nullable = true, example = "[\"VN\"]", description = "Institution country codes for author search.")
        List<String> country,

        @Schema(nullable = true, example = "[\"T10017\"]", description = "Primary topic ids for author search.")
        List<String> primaryTopic,

        @Schema(nullable = true, example = "[\"2202\"]", description = "Subfield ids for topic search.")
        List<String> subField,

        @Schema(nullable = true, example = "[\"17\"]", description = "Field ids for topic search.")
        List<String> field,

        @Schema(nullable = true, allowableValues = {"relevance", "works", "alphabetical"}, description = "Primary sort field.")
        String sortBy,

        @Schema(nullable = true, allowableValues = {"asc", "desc"}, description = "Sort direction.")
        String sortDirection,

        @Schema(nullable = true, example = "1", defaultValue = "1")
        Integer page,

        @Schema(nullable = true, example = "20", defaultValue = "20")
        Integer perPage
) {
}
