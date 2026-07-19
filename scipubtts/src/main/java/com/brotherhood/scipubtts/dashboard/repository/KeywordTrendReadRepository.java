package com.brotherhood.scipubtts.dashboard.repository;

import com.brotherhood.scipubtts.dashboard.entity.Keyword;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface KeywordTrendReadRepository extends JpaRepository<Keyword, Long> {

    interface KeywordTrendRow {
        String getKeywordId();

        String getKeyword();

        String getFieldId();

        Long getWorksCount();

        Long getCitedByCount();
    }

    @Query("select max(keyword.endTime) from Keyword keyword")
    LocalDate findLatestSnapshotDate();

    @Query(
            value = """
                    WITH latest_weekly_keywords AS (
                        SELECT
                            id,
                            keyword,
                            ROW_NUMBER() OVER (
                                PARTITION BY LOWER(keyword)
                                ORDER BY cagr DESC, pgr DESC, ps DESC, cited_by_count DESC, works_count DESC, end_time DESC, start_time DESC, created_at DESC, id DESC
                            ) AS row_rank
                        FROM keywords
                        WHERE end_time = :snapshotDate
                    )
                    SELECT COUNT(*)
                    FROM latest_weekly_keywords
                    WHERE row_rank = 1
                    """,
            nativeQuery = true
    )
    long countTrendingKeywords(@Param("snapshotDate") LocalDate snapshotDate);

    @Query(
            value = """
                    WITH latest_weekly_keywords AS (
                        SELECT
                            id,
                            keyword_id,
                            keyword,
                            field_id,
                            works_count,
                            cited_by_count,
                            pgr,
                            cagr,
                            ps,
                            start_time,
                            end_time,
                            created_at,
                            ROW_NUMBER() OVER (
                                PARTITION BY LOWER(keyword)
                                ORDER BY cagr DESC, pgr DESC, ps DESC, cited_by_count DESC, works_count DESC, end_time DESC, start_time DESC, created_at DESC, id DESC
                            ) AS row_rank
                        FROM keywords
                        WHERE end_time = :snapshotDate
                    )
                    SELECT
                        keyword_id AS keywordId,
                        keyword AS keyword,
                        field_id AS fieldId,
                        works_count AS worksCount,
                        cited_by_count AS citedByCount
                    FROM latest_weekly_keywords
                    WHERE row_rank = 1
                    ORDER BY cagr DESC, pgr DESC, ps DESC, cited_by_count DESC, works_count DESC, keyword ASC
                    """,
            nativeQuery = true
    )
    List<KeywordTrendRow> findTrendingKeywords(
            @Param("snapshotDate") LocalDate snapshotDate,
            Pageable pageable
    );
}
