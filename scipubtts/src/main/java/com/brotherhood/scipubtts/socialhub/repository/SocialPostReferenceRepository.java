package com.brotherhood.scipubtts.socialhub.repository;

import com.brotherhood.scipubtts.socialhub.entity.SocialPostReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SocialPostReferenceRepository extends JpaRepository<SocialPostReference, UUID> {

    List<SocialPostReference> findByPostId(UUID postId);

    int countByPostId(UUID postId);
}
