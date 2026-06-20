package com.brotherhood.scipubtts.dashboard.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAlexHotKeywordFilterResponse(
        @JsonProperty("results")
        List<KeywordItem> keywordItemList
) {
    public record KeywordItem(
            String id,

            @JsonProperty("display_name")
            String displayName,

            @JsonProperty("works_count")
            Long worksCount,

            @JsonProperty("cited_by_count")
            Long citedByCount
    ) {
    }
}
