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
public class OpenAlexGroupByResponse {
    private MetaResponse meta;

    @JsonProperty("group_by")
    private List<GroupByItem> groupBy;


    public MetaResponse meta() {
        return meta;
    }

    public List<GroupByItem> groupBy() {
        return groupBy;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetaResponse {
      private long count;

      @JsonProperty("next_cursor")
      private String nextCursor;

      public long count() {
          return count;
      }

      public String nextCursor() {
          return nextCursor;
      }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupByItem {
      private String key;

      private long count;

      public String key() {
          return key;
      }

      public long count() {
          return count;
      }
  }
}
