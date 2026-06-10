package com.brotherhood.scipubtts.dashboard.dto.request;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record KeywordCalculateAllRequest(
        String fieldId,
        LocalDate recentStart,
        LocalDate recentEnd
) {
  public LocalDate pastEnd() {
    return recentStart.minusDays(1);
  }

  public LocalDate pastStart() {
    long years = ChronoUnit.YEARS.between(recentStart, recentEnd);
    return pastEnd().minusYears(years);
  }

  public double k() {
    double recentMid = recentStart.getYear() + (recentEnd.getYear() - recentStart.getYear()) / 2.0;
    double pastMid   = pastStart().getYear() + (pastEnd().getYear() - pastStart().getYear()) / 2.0;
    return recentMid - pastMid;
  }
}