package com.brotherhood.scipubtts.dashboard.dto.response.openalex;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAlexPublicationResponse {
    private Meta meta;

    @JsonProperty("group_by")
    private List<GroupBy> groupBy;


    public Meta meta() {
        return meta;
    }

    public List<GroupBy> groupBy() {
        return groupBy;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Meta {
      private Long count;

      @JsonProperty("db_response_time_ms")
      private Integer dbResponseTimeMs;

      private Integer page;

      @JsonProperty("per_page")
      private Integer perPage;

      @JsonProperty("groups_count")
      private Integer groupsCount;

      @JsonProperty("cost_usd")
      private Double costUsd;

      public Long count() {
          return count;
      }

      public Integer dbResponseTimeMs() {
          return dbResponseTimeMs;
      }

      public Integer page() {
          return page;
      }

      public Integer perPage() {
          return perPage;
      }

      public Integer groupsCount() {
          return groupsCount;
      }

      public Double costUsd() {
          return costUsd;
      }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupBy {
      private String key;

      @JsonProperty("key_display_name")
      private String keyDisplayName;

      private Long count;

      public String key() {
          return key;
      }

      public String keyDisplayName() {
          return keyDisplayName;
      }

      public Long count() {
          return count;
      }
  }
}
