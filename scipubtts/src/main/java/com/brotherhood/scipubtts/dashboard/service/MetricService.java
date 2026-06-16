package com.brotherhood.scipubtts.dashboard.service;

import com.brotherhood.scipubtts.dashboard.dto.request.PeriodRequest;
import com.brotherhood.scipubtts.dashboard.dto.response.MetricsResponse;

public interface MetricService {
  MetricsResponse calculateAndSaveMetrics(
          PeriodRequest request
  );

  MetricsResponse getMetricsFromDb(
          PeriodRequest request
  );

}
