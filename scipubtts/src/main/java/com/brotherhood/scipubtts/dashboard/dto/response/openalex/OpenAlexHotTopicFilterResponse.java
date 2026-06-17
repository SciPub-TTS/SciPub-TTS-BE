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
public class OpenAlexHotTopicFilterResponse {
    private List<TopicItem> results;


    public List<TopicItem> results() {
        return results;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TopicItem {
      private String id;

      @JsonProperty("display_name")
      private String displayName;

      @JsonProperty("works_count")
      private Long worksCount;

      @JsonProperty("cited_by_count")
      private Long citedByCount;

      public String id() {
          return id;
      }

      public String displayName() {
          return displayName;
      }

      public Long worksCount() {
          return worksCount;
      }

      public Long citedByCount() {
          return citedByCount;
      }
  }
}
