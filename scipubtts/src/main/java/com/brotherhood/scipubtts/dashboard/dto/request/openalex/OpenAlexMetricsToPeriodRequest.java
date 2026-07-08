package com.brotherhood.scipubtts.dashboard.dto.request.openalex;

import com.brotherhood.scipubtts.dashboard.constant.OpenAlexEntity;

import java.util.List;

public record OpenAlexMetricsToPeriodRequest(
        OpenAlexEntity entity,
        List<Integer> fieldIds,
        String startTime,
        String endTime
) {
  public OpenAlexMetricsToPeriodRequest(OpenAlexEntity entity) {
    this(entity, null, null, null);
  }

  public OpenAlexMetricsToPeriodRequest(OpenAlexEntity entity, List<Integer> fieldIds) {
    this(entity, fieldIds, null, null);
  }
}