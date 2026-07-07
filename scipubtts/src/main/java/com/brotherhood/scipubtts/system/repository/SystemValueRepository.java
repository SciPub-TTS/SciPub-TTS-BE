package com.brotherhood.scipubtts.system.repository;

import com.brotherhood.scipubtts.system.entity.SystemValue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SystemValueRepository extends JpaRepository<SystemValue, Long> {
    Optional<SystemValue> findByConfigKey(String configKey);
}