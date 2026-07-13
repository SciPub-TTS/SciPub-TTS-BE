package com.brotherhood.scipubtts.dashboard.repository;

import com.brotherhood.scipubtts.dashboard.entity.PublicationTrend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicationTrendRepository
        extends JpaRepository<PublicationTrend, Integer> {

  List<PublicationTrend> findByYearBetweenOrderByYear(
          Integer startYear,
          Integer endYear
  );
}