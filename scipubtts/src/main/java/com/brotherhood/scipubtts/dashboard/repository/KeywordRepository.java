package com.brotherhood.scipubtts.dashboard.repository;

import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {

  Optional<Keyword> findByKeywordAndFieldIdAndStartTimeAndEndTime(
          String keyword, String fieldId, LocalDate startTime, LocalDate endTime
  );

  List<Keyword> findByFieldIdAndStartTimeAndEndTime(
          String fieldId, LocalDate startTime, LocalDate endTime
  );

  List<Keyword> findByKeyword(String keyword);

  List<Keyword> findByStartTimeAndEndTimeOrderByCagrDesc(
          LocalDate startTime, LocalDate endTime
  );

  List<Keyword> findByStartTimeAndEndTimeOrderByPsDesc(
          LocalDate startTime, LocalDate endTime
  );

  boolean existsByKeywordAndStartTimeAndEndTime(
          String keyword, LocalDate startTime, LocalDate endTime
  );
}