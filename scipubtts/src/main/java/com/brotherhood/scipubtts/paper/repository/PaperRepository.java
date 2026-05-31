package com.brotherhood.scipubtts.paper.repository;

import com.brotherhood.scipubtts.paper.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaperRepository extends JpaRepository<Paper, UUID> {

    Optional<Paper> findByOpenalexId(String openalexId);

    Optional<Paper> findByDoi(String doi);
}
