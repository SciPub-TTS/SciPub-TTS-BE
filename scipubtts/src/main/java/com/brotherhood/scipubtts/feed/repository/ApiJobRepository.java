package com.brotherhood.scipubtts.feed.repository;

import com.brotherhood.scipubtts.feed.entity.ApiJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiJobRepository extends JpaRepository<ApiJob, UUID> {

    Optional<ApiJob> findTopByJobTypeAndStatusOrderByFinishedAtDesc(
            String jobType,
            String status
    );
}
