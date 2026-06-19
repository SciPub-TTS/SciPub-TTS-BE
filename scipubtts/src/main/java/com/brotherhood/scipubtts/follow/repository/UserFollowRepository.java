package com.brotherhood.scipubtts.follow.repository;

import com.brotherhood.scipubtts.feed.model.FollowTargetGroupView;
import com.brotherhood.scipubtts.follow.entity.FollowTargetType;
import com.brotherhood.scipubtts.follow.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    Optional<UserFollow> findByUserIdAndTargetTypeAndTargetOpenAlexId(UUID userId, FollowTargetType targetType,
            String targetOpenAlexId);

    List<UserFollow> findByUserIdAndTargetType(UUID userId, FollowTargetType targetType);

    boolean existsByUserIdAndTargetTypeAndTargetOpenAlexId(UUID userId, FollowTargetType targetType,
            String targetOpenAlexId);

    @Query("""
                    SELECT f
                    FROM UserFollow f
                    WHERE f.userId = :userId
                      AND (:targetType IS NULL OR f.targetType = :targetType)
                      AND (
                          :keyword IS NULL OR :keyword = ''
                          OR LOWER(f.displayNameSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                          OR LOWER(f.targetOpenAlexId) LIKE LOWER(CONCAT('%', :keyword, '%'))
                      )
            """)
    Page<UserFollow> searchMyFollows(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("targetType") FollowTargetType targetType,
            Pageable pageable);

    @Modifying
    @Query("""
                DELETE FROM UserFollow f
                WHERE f.userId = :userId
                  AND f.targetType = :targetType
                  AND f.targetOpenAlexId = :targetOpenAlexId
            """)
    int deleteByUserIdAndTargetTypeAndTargetOpenAlexId(
            @Param("userId") UUID userId,
            @Param("targetType") FollowTargetType targetType,
            @Param("targetOpenAlexId") String targetOpenAlexId);

    @Modifying
    @Query("""
                DELETE FROM UserFollow f
                WHERE f.id = :id
                  AND f.userId = :userId
            """)
    int deleteByIdAndUserId(
            @Param("id") UUID id,
            @Param("userId") UUID userId);

    @Query(value = """
            SELECT
                target_type AS targetType,
                target_openalex_id AS targetOpenalexId,
                MAX(display_name_snapshot) AS displayNameSnapshot,
                STRING_AGG(user_id::text, ',') AS userIds
            FROM user_follow
            WHERE target_type IN ('TOPIC', 'AUTHOR')
            GROUP BY target_type, target_openalex_id
            """, nativeQuery = true)
    List<FollowTargetGroupView> findFeedTargetGroups();
}
