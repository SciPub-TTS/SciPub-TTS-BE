package com.brotherhood.scipubtts.search.repository;

import com.brotherhood.scipubtts.search.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    interface RecentSearchProjection {
        String getContent();

        OffsetDateTime getLatestCreatedAt();
    }

    @Query("""
            select sh.content as content, max(sh.createdAt) as latestCreatedAt
            from SearchHistory sh
            where (:userId is null or sh.userId = :userId)
            group by sh.content
            order by max(sh.createdAt) desc
            """)
    List<RecentSearchProjection> findRecentDistinctSearches(
            @Param("userId") UUID userId,
            Pageable pageable
    );

    long deleteByUserIdAndContentIgnoreCase(UUID userId, String content);
}

