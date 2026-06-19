package com.brotherhood.scipubtts.socialhub.repository;

import com.brotherhood.scipubtts.socialhub.entity.SocialPostReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SocialPostReferenceRepository extends JpaRepository<SocialPostReference, UUID> {

    List<SocialPostReference> findByPostId(UUID postId);

    int countByPostId(UUID postId);

    @Query("SELECT r.openalexId FROM SocialPostReference  r WHERE r.post.id = :postId")
    List<String> findOpenalexIdByPostId(@Param("postId") UUID postId);
}
