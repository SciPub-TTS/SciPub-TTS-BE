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
public class MetricsResponse {
    private List<MetricItem> metricList;


    public List<MetricItem> metricList() {
        return metricList;
    }
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MetricItem {
      private String title;

      private double value;

      private double change;

      public String title() {
          return title;
      }

      public double value() {
          return value;
      }

      public double change() {
          return change;
      }
  }
}
