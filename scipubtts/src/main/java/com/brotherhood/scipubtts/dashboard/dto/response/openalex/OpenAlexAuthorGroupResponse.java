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
public class OpenAlexAuthorGroupResponse {
    @JsonProperty("meta")
    private Meta meta;

    @JsonProperty("group_by")
    private List<Group> groupBy;


    public Meta meta() {
        return meta;
    }

    public List<Group> groupBy() {
        return groupBy;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Meta {
      @JsonProperty("count")
      private long count;

      @JsonProperty("groups_count")
      private long groupsCount;

      @JsonProperty("next_cursor")
      private String nextCursor;

      public long count() {
          return count;
      }

      public long groupsCount() {
          return groupsCount;
      }

      public String nextCursor() {
          return nextCursor;
      }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Group {
      @JsonProperty("key")
      private String key;

      @JsonProperty("key_display_name")
      private String keyDisplayName;

      @JsonProperty("count")
      private long count;

      public String key() {
          return key;
      }

      public String keyDisplayName() {
          return keyDisplayName;
      }

      public long count() {
          return count;
      }
  }
}
