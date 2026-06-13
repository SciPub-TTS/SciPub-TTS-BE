package com.brotherhood.scipubtts.dashboard.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAlexGroupByResponse(
        MetaResponse meta,
        @JsonProperty("group_by")
        List<GroupByItem> groupBy
) {
  public record MetaResponse(
          long count,
          @JsonProperty("next_cursor")
          String nextCursor
  ) {}

  public record GroupByItem(
          String key,
          long count
  ) {}
}