package com.brotherhood.scipubtts.socialhub.repository;

import com.brotherhood.scipubtts.socialhub.entity.SocialPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SocialPostLikeRepository extends JpaRepository<SocialPostLike, UUID> {
    Optional<SocialPostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    // Lấy tập postId mà user đã like — dùng cho hybrid-view batch check
    @Query("SELECT l.post.id FROM SocialPostLike l WHERE l.user.id = :userId AND l.post.id IN :postIds")
    Set<UUID> findLikedPostIds(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);
}
