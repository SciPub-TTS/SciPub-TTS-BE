package com.brotherhood.scipubtts.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAlexPublicationResponse (
        Meta meta,

        @JsonProperty("group_by")
        List<GroupBy> groupBy
) {

  public record Meta(
          Long count,

          @JsonProperty("db_response_time_ms")
          Integer dbResponseTimeMs,

          Integer page,

          @JsonProperty("per_page")
          Integer perPage,

          @JsonProperty("groups_count")
          Integer groupsCount,

          @JsonProperty("cost_usd")
          Double costUsd
  ) {}

  public record GroupBy(
          String key,

          @JsonProperty("key_display_name")
          String keyDisplayName,

          Long count
  ) {}
}