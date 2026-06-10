package com.brotherhood.scipubtts.dashboard.repository;

import com.brotherhood.scipubtts.dashboard.entity.PublicationTrend;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationTrendRepository
        extends JpaRepository<PublicationTrend, Integer> {
}