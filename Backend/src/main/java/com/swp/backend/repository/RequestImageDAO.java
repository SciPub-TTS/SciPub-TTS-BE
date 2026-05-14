package com.swp.backend.repository;

import com.swp.backend.entity.RequestImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequestImageDAO extends JpaRepository<RequestImage, UUID> {
}
