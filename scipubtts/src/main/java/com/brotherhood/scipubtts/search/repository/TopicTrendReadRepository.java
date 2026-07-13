package com.brotherhood.scipubtts.search.repository;

import com.brotherhood.scipubtts.dashboard.entity.Topic;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TopicTrendReadRepository extends JpaRepository<Topic, Long> {

    interface TopicTrendRow {
        String getTopicId();

        String getName();

        Integer getFieldId();

        Long getWorks();

        Long getCitations();
    }

    @Query("select max(topic.endTime) from Topic topic")
    LocalDate findLatestSnapshotDate();

    @Query(
            value = """
                    WITH latest_weekly_topics AS (
                        SELECT
                            id,
                            name,
                            ROW_NUMBER() OVER (
                                PARTITION BY LOWER(name)
                                ORDER BY acceleration DESC, velocity DESC, citations DESC, works DESC, end_time DESC, start_time DESC, id DESC
                            ) AS row_rank
                        FROM topics
                        WHERE end_time = :snapshotDate
                    )
                    SELECT COUNT(*)
                    FROM latest_weekly_topics
                    WHERE row_rank = 1
                    """,
            nativeQuery = true
    )
    long countTrendingTopics(@Param("snapshotDate") LocalDate snapshotDate);

    @Query(
            value = """
                    WITH latest_weekly_topics AS (
                        SELECT
                            id,
                            topic_id,
                            name,
                            field_id,
                            works,
                            citations,
                            velocity,
                            acceleration,
                            start_time,
                            end_time,
                            ROW_NUMBER() OVER (
                                PARTITION BY LOWER(name)
                                ORDER BY acceleration DESC, velocity DESC, citations DESC, works DESC, end_time DESC, start_time DESC, id DESC
                            ) AS row_rank
                        FROM topics
                        WHERE end_time = :snapshotDate
                    )
                    SELECT
                        topic_id AS topicId,
                        name AS name,
                        field_id AS fieldId,
                        works AS works,
                        citations AS citations
                    FROM latest_weekly_topics
                    WHERE row_rank = 1
                    ORDER BY acceleration DESC, velocity DESC, citations DESC, works DESC, name ASC
                    """,
            nativeQuery = true
    )
    List<TopicTrendRow> findTrendingTopics(
            @Param("snapshotDate") LocalDate snapshotDate,
            Pageable pageable
    );
}
