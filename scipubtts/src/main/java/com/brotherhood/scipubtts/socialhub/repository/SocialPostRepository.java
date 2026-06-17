package com.brotherhood.scipubtts.socialhub.repository;

import com.brotherhood.scipubtts.socialhub.entity.SocialPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SocialPostRepository extends JpaRepository<SocialPost, UUID> {
    // Feed mới nhất (deleted_at IS NULL tự xử lý bởi @SQLRestriction trên entity)
    Page<SocialPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Feed hot nhất (like nhiều → mới nhất)
    Page<SocialPost> findAllByOrderByLikeCountDescCreatedAtDesc(Pageable pageable);

    // Bài viết của một tác giả
    Page<SocialPost> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    // Cộng like trực tiếp tại DB — tránh race condition
    @Modifying
    @Query("UPDATE SocialPost p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") UUID postId);

    // Trừ like trực tiếp tại DB
    @Modifying
    @Query("UPDATE SocialPost p SET p.likeCount = GREATEST(p.likeCount - 1, 0) WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") UUID postId);
    
}
