package com.swp.backend.repository;

import com.swp.backend.entity.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CitizenDAO extends JpaRepository<Citizen, UUID> {
    Optional<Citizen> findByPhone(String phone);
}