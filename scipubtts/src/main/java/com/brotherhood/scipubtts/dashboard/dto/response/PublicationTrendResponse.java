package com.brotherhood.scipubtts.dashboard.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublicationTrendResponse {
    private List<PublicationTrendItem> publicationTrends;


    public List<PublicationTrendItem> publicationTrends() {
        return publicationTrends;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PublicationTrendItem {
      private Long publications;

      private Integer year;

      public Long publications() {
          return publications;
      }

      public Integer year() {
          return year;
      }
  }
}
