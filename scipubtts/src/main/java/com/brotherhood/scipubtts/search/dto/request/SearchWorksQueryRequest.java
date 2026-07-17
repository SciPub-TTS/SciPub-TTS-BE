package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SearchWorksQueryRequest")
public record SearchWorksQueryRequest(
        @Schema(nullable = true, example = "AI")
        String query,

        @Schema(
                nullable = true,
                allowableValues = {"range", "exact"},
                example = "range"
        )
        String yearMode,

        @Schema(nullable = true, example = "2018")
        Integer yearFrom,

        @Schema(nullable = true, example = "2026")
        Integer yearTo,

        @Schema(nullable = true, example = "2006")
        Integer yearExact,

        @Schema(nullable = true, example = "[\"article\"]")
        List<String> type,

        @Schema(nullable = true, example = "true")
        Boolean openAccess,

        @Schema(nullable = true, example = "[\"2202\"]")
        List<String> subField,

        @Schema(nullable = true, example = "[\"A5024995037\"]")
        List<String> author,

        @Schema(nullable = true, example = "[\"I201448701\"]")
        List<String> institution,

        @Schema(nullable = true, example = "true")
        Boolean pdf,

        @Schema(nullable = true, example = "[\"VN\"]")
        List<String> country,

        @Schema(
                nullable = true,
                allowableValues = {"range", "exact"},
                example = "range"
        )
        String citationMode,

        @Schema(nullable = true, example = "10")
        Integer citationMin,

        @Schema(nullable = true, example = "100")
        Integer citationMax,

        @Schema(nullable = true, example = "50")
        Integer citationExact,

        @Schema(nullable = true, example = "[\"S64187185\"]")
        List<String> source,

        @Schema(nullable = true, example = "[\"G1204744554\"]")
        List<String> award,

        @Schema(nullable = true, allowableValues = {"is", "is not"}, example = "is")
        String indexedByOrcid,

        @Schema(nullable = true, allowableValues = {"relevance", "citation", "published"}, example = "[\"citation\", \"published\"]")
        List<String> sortBy,

        @Schema(nullable = true, allowableValues = {"asc", "desc"}, example = "[\"desc\", \"asc\"]")
        List<String> sortDirection,

        @Schema(nullable = true, example = "1", defaultValue = "1")
        Integer page,

        @Schema(nullable = true, example = "20", defaultValue = "20")
        Integer perPage
) {
    public static SearchWorksQueryRequest empty() {
        return new SearchWorksQueryRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
