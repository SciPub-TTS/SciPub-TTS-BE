package com.brotherhood.scipubtts.admin.repository;

import com.brotherhood.scipubtts.admin.entity.OpenAlexSubfield;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OpenAlexSubfieldRepository extends JpaRepository<OpenAlexSubfield, Long> {

    Optional<OpenAlexSubfield> findByOpenAlexId(String openAlexId);
}
