package com.brotherhood.scipubtts.dashboard.dto.response.openalex;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.jdbc.Work;

import java.util.List;

public record OpenAlexWorkCitationResponse(
        @JsonProperty("meta")
        Meta meta,

        @JsonProperty("results")
        List<WorkCitation> workCitationList

) {
  public record Meta(

          @JsonProperty("count")
          long count,

          @JsonProperty("next_cursor")
          String nextCursor

  ) {
  }

  public record WorkCitation(

          @JsonProperty("cited_by_count")
          long citedByCount,

          @JsonProperty("publication_date")
          String publicationDate

  ) {
  }
}