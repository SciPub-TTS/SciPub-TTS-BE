package com.brotherhood.scipubtts.search.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SearchWorksQueryRequest",
        description = "Search parameters for works. Year range and year exact are mutually exclusive. Citation range and citation exact are also mutually exclusive."
)
public record SearchWorksQueryRequest(
        @Schema(nullable = true, example = "AI")
        String query,

        @Schema(
                nullable = true,
                allowableValues = {"range", "exact"},
                description = "Select one year mode. Use 'range' with yearFrom/yearTo, or 'exact' with yearExact. Do not combine both."
        )
        String yearMode,

        @Schema(
                nullable = true,
                example = "2018",
                description = "Start year for range mode. Leave empty when using yearExact."
        )
        Integer yearFrom,

        @Schema(
                nullable = true,
                example = "2026",
                description = "End year for range mode. Leave empty when using yearExact."
        )
        Integer yearTo,

        @Schema(
                nullable = true,
                example = "2006",
                description = "Exact year. Leave empty when using yearFrom/yearTo."
        )
        Integer yearExact,

        @Schema(nullable = true, example = "[\"article\"]", description = "Work types, for example article, preprint or book-chapter.")
        List<String> type,

        @Schema(nullable = true, example = "true", description = "Filter only open access works.")
        Boolean openAccess,

        @Schema(nullable = true, example = "[\"2202\"]", description = "OpenAlex subfield ids. Use /api/search/filters/subField/options to load valid values.")
        List<String> subField,

        @Schema(nullable = true, example = "[\"A5024995037\"]", description = "OpenAlex author ids. Use /api/search/filters/author/options to load valid values.")
        List<String> author,

        @Schema(nullable = true, example = "[\"I201448701\"]", description = "OpenAlex institution ids. Use /api/search/filters/institution/options to load valid values.")
        List<String> institution,

        @Schema(nullable = true, example = "true", description = "Filter works that have a PDF or full text content.")
        Boolean pdf,

        @Schema(nullable = true, example = "[\"VN\"]", description = "Institution country codes, for example VN, US, GB.")
        List<String> country,

        @Schema(
                nullable = true,
                allowableValues = {"range", "exact"},
                description = "Select one citation mode. Use 'range' with citationMin/citationMax, or 'exact' with citationExact. Do not combine both."
        )
        String citationMode,

        @Schema(
                nullable = true,
                example = "10",
                description = "Minimum citation count for range mode. Leave empty when using citationExact."
        )
        Integer citationMin,

        @Schema(
                nullable = true,
                example = "100",
                description = "Maximum citation count for range mode. Leave empty when using citationExact."
        )
        Integer citationMax,

        @Schema(
                nullable = true,
                example = "50",
                description = "Exact citation count. Leave empty when using citationMin/citationMax."
        )
        Integer citationExact,

        @Schema(nullable = true, example = "[\"S64187185\"]", description = "OpenAlex source ids. Use /api/search/filters/source/options to load valid values.")
        List<String> source,

        @Schema(nullable = true, example = "[\"G1204744554\"]", description = "OpenAlex award ids. Use /api/search/filters/award/options to load valid values.")
        List<String> award,

        @Schema(nullable = true, allowableValues = {"is", "is not"}, description = "Filter by ORCID indexing state.")
        String indexedByOrcid,

        @Schema(nullable = true, allowableValues = {"none", "keyword", "topic", "both"}, description = "Trending filter mode.")
        String trendingMode,

        @Schema(nullable = true, allowableValues = {"relevance", "citation", "published"}, description = "Primary sort field.")
        String sortBy,

        @Schema(nullable = true, allowableValues = {"asc", "desc"}, description = "Sort direction.")
        String sortDirection,

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
                null,
                null
        );
    }
}
