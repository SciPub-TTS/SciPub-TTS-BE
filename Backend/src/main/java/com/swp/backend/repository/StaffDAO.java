package com.swp.backend.repository;

import com.swp.backend.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffDAO extends JpaRepository<Staff, UUID> {
    Optional<Staff> findByPhone(String phone);

    Page<Staff> findAllByRole(String role, Pageable pageable);

    @Query("""
                SELECT s FROM Staff s
                WHERE s.role = :role
                  AND (
                       LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    Page<Staff> searchByRoleAndKeyword(
            @Param("role") String role,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    boolean existsByPhone(String phone);

    @Query("""
                SELECT s FROM Staff s
                WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Staff> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("""
            SELECT s.id, s.name
            FROM Staff s
            WHERE s.role = :role
              AND (
                    :keyword IS NULL OR
                    LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    List<Object[]> findTeamOwners(
            @Param("role") String role,
            @Param("keyword") String keyword
    );

    long countByStaffState(String staffState);

    long countBy();

    @Query(value = """
             SELECT
                 s.id,
                 s.team_name
             FROM Staff s
             INNER JOIN Vehicle v ON v.rescue_team_id = s.id
             WHERE s.role        = 'cứu hộ'
               AND s.staff_state = 'hoạt động'
               AND v.type        = :vehicleType
               AND v.state       = 'không hoạt động'
               AND s.geo_location IS NOT NULL
               GROUP BY s.id, s.team_name, s.geo_location
             ORDER BY ST_Distance_Sphere(
                 s.geo_location,
                 ST_GeomFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326)
             ) ASC
             LIMIT 4
            \s""", nativeQuery = true)
    List<Object[]> findTop4NearbyTeams(
            @Param("latitude")    double latitude,
            @Param("longitude")   double longitude,
            @Param("vehicleType") String vehicleType
    );
}
