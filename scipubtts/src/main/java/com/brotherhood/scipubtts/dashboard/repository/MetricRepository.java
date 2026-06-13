package com.brotherhood.scipubtts.dashboard.repository;

import com.brotherhood.scipubtts.dashboard.entity.Metric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MetricRepository
        extends JpaRepository<Metric, Long> {

  Optional<Metric> findByTitleAndStartTimeAndEndTime(
          String title,
          LocalDate startTime,
          LocalDate endTime
  );

  List<Metric> findByStartTimeAndEndTime(
          LocalDate startTime,
          LocalDate endTime
  );

  boolean existsByStartTimeAndEndTime(
          LocalDate startTime,
          LocalDate endTime
  );
}