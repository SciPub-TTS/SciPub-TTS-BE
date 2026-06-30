package com.brotherhood.scipubtts.feed.repository;

import com.brotherhood.scipubtts.feed.entity.ResearchFeed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResearchFeedJpaRepository extends JpaRepository<ResearchFeed, UUID> {

    @Query(value = """
            SELECT * FROM research_feed_item r
            WHERE r.user_id = :userId
              AND r.dismissed_at IS NULL
              AND (
                :tabFilter = 'ALL'
                OR (:tabFilter = 'MATCHED_TOPIC' AND r.reason_json::text LIKE '%"TOPIC"%')
                OR (:tabFilter = 'MATCHED_AUTHOR' AND r.reason_json::text LIKE '%"AUTHOR"%')
              )
              AND (
                :exactMatchType IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM jsonb_array_elements(COALESCE(r.reason_json->'reasons', '[]'::jsonb)) reason
                    WHERE reason->>'type' = :exactMatchType
                      AND (
                        (
                          :exactMatchId IS NOT NULL
                          AND (
                            reason->>'targetOpenalexId' = :exactMatchId
                            OR regexp_replace(reason->>'targetOpenalexId', '^.*/', '') = :exactMatchId
                          )
                        )
                        OR (
                          :exactMatchName IS NOT NULL
                          AND LOWER(reason->>'displayName') = LOWER(:exactMatchName)
                        )
                      )
                )
              )
            """,
            countQuery = """
            SELECT count(*) FROM research_feed_item r
            WHERE r.user_id = :userId
              AND r.dismissed_at IS NULL
              AND (
                :tabFilter = 'ALL'
                OR (:tabFilter = 'MATCHED_TOPIC' AND r.reason_json::text LIKE '%"TOPIC"%')
                OR (:tabFilter = 'MATCHED_AUTHOR' AND r.reason_json::text LIKE '%"AUTHOR"%')
              )
              AND (
                :exactMatchType IS NULL
                OR EXISTS (
                    SELECT 1
                    FROM jsonb_array_elements(COALESCE(r.reason_json->'reasons', '[]'::jsonb)) reason
                    WHERE reason->>'type' = :exactMatchType
                      AND (
                        (
                          :exactMatchId IS NOT NULL
                          AND (
                            reason->>'targetOpenalexId' = :exactMatchId
                            OR regexp_replace(reason->>'targetOpenalexId', '^.*/', '') = :exactMatchId
                          )
                        )
                        OR (
                          :exactMatchName IS NOT NULL
                          AND LOWER(reason->>'displayName') = LOWER(:exactMatchName)
                        )
                      )
                )
              )
            """,
            nativeQuery = true)
    Page<ResearchFeed> findUserFeed(
            @Param("userId") UUID userId,
            @Param("tabFilter") String tabFilter,
            @Param("exactMatchType") String exactMatchType,
            @Param("exactMatchId") String exactMatchId,
            @Param("exactMatchName") String exactMatchName,
            Pageable pageable
    );
}
