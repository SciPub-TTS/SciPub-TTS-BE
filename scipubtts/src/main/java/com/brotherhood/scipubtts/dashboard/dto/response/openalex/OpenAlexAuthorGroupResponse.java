package com.brotherhood.scipubtts.dashboard.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAlexAuthorGroupResponse(
        @JsonProperty("meta")
        Meta meta,

        @JsonProperty("group_by")
        List<Group> groupBy
) {
    public record Meta(
            @JsonProperty("count")
            long count,

            @JsonProperty("groups_count")
            long groupsCount,

            @JsonProperty("next_cursor")
            String nextCursor
    ) {
    }

    public record Group(
            @JsonProperty("key")
            String key,

            @JsonProperty("key_display_name")
            String keyDisplayName,

            @JsonProperty("count")
            long count
    ) {
    }
}
