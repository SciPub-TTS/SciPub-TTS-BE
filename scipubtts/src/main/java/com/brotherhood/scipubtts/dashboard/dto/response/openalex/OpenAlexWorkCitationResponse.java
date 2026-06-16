package com.brotherhood.scipubtts.dashboard.dto.response.openalex;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.jdbc.Work;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAlexWorkCitationResponse {
    @JsonProperty("meta")
    private Meta meta;

    @JsonProperty("results")
    private List<WorkCitation> workCitationList;


    public Meta meta() {
        return meta;
    }

    public List<WorkCitation> workCitationList() {
        return workCitationList;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Meta {
      @JsonProperty("count")
      private long count;

      public long count() {
          return count;
      }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkCitation {
      @JsonProperty("cited_by_count")
      private long citedByCount;

      @JsonProperty("publication_date")
      private String publicationDate;

      public long citedByCount() {
          return citedByCount;
      }

      public String publicationDate() {
          return publicationDate;
      }
  }
}
