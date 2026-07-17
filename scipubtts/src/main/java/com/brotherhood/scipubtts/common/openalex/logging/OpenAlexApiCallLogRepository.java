package com.brotherhood.scipubtts.common.openalex.logging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OpenAlexApiCallLogRepository extends JpaRepository<OpenAlexApiCallLog, UUID> {
}
