package com.brotherhood.scipubtts.search.dto;

import java.util.List;

public record SearchFilterOptionsResponse(
        YearRange year,
        List<FacetOption> type,
        ToggleFilter openAccess,
        List<FacetOption> subField,
        List<EntityOption> author,
        List<EntityOption> institution,
        ToggleFilter pdf,
        CitationRange citation,
        List<FacetOption> country,
        List<EntityOption> source,
        List<EntityOption> award,
        EnumFilter indexedByOrcid
) {
    public record YearRange(int minimumYear, int currentYear) {
    }

    public record CitationRange(int minimumCitation, int maximumCitation) {
    }

    public record FacetOption(String value, String label, long count) {
    }

    public record EntityOption(String id, String label, Long count) {
    }

    public record ToggleFilter(String openAlexFilterKey, boolean defaultValue) {
    }

    public record EnumFilter(String openAlexFilterKey, List<String> options, String defaultValue) {
    }
}

/*
SEARCH_FILE_NOTE
Syntax su dung:
- Nested records de gom nhom filter options.
File nay lam gi:
- Dinh nghia schema options cho 12 filter tren UI.
Flow chay:
- Service lay option tu OpenAlex -> map vao record -> FE dung de do filter.
*/

