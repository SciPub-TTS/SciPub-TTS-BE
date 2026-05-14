package com.swp.backend.repository;

import com.swp.backend.entity.Vehicle;
import com.swp.backend.dto.vehicle.response.FilterVehicleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface VehicleDAO extends JpaRepositoryImplementation<Vehicle, UUID> {
    Vehicle findById(String id);

    @Modifying
    @Transactional
    @Query("UPDATE Vehicle v SET v.state = :state WHERE v.id = :id AND v.state = 'không hoạt động'")
    int setVehicle(UUID id, String state);

    @Query("""
            SELECT new com.rescue.backend.view.dto.vehicle.response.FilterVehicleResponse(
                v.id,
                v.type,
                s.id,
                s.name
            )
            FROM Vehicle v
            JOIN v.staff s
            WHERE v.type = :type
            AND v.state = 'không hoạt động'
            """)
    List<FilterVehicleResponse> filterVehicleByType(String type);

    Page<Vehicle> findAllByType(String type, Pageable pageable);

    @Query("""
                SELECT v FROM Vehicle v
                WHERE LOWER(v.staff.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(v.type) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Vehicle> searchVehicle(@Param("keyword") String keyword, Pageable pageable);

    long countByState(String state);

    long countBy();

    @Query("""
    SELECT v FROM Vehicle v
    WHERE v.staff.id = :teamId
      AND v.state = 'không hoạt động'
      AND v.type = :type
""")
    List<Vehicle> findAvailableVehicle(UUID teamId, String type, Pageable pageable);
}
