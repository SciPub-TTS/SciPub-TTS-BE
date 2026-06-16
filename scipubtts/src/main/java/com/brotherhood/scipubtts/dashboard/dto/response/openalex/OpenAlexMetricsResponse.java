package com.brotherhood.scipubtts.dashboard.dto.response.openalex;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAlexMetricsResponse {
    private Meta meta;


    public Meta meta() {
        return meta;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Meta {
      private Long count;

      @JsonProperty("cost_usd")
      private Double costUsd;

      public Long count() {
          return count;
      }

      public Double costUsd() {
          return costUsd;
      }
  }
}
