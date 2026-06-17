package com.brotherhood.scipubtts.dashboard.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAlexHotTopicFilterResponse(
        List<TopicItem> results
) {
    public record TopicItem(
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
