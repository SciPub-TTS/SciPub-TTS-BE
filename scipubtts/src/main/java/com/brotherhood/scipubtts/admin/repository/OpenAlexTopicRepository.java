package com.brotherhood.scipubtts.admin.repository;

import com.brotherhood.scipubtts.admin.entity.OpenAlexTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OpenAlexTopicRepository extends JpaRepository<OpenAlexTopic, Long> {

    Optional<OpenAlexTopic> findByOpenAlexId(String openAlexId);
}
