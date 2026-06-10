package com.brotherhood.scipubtts.dashboard.repository;

import com.brotherhood.scipubtts.dashboard.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

  List<Topic> findByStartTimeAndEndTime(
          LocalDate startTime,
          LocalDate endTime
  );

  Optional<Topic> findByTopicIdAndStartTimeAndEndTime(
          String topicId,
          LocalDate startTime,
          LocalDate endTime
  );
}