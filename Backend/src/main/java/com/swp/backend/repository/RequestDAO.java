package com.swp.backend.repository;

import com.swp.backend.entity.Request;

import com.swp.backend.dto.coordinator.response.SpecificResponse;
import com.swp.backend.dto.coordinator.response.TakeListResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequestDAO extends JpaRepository<Request, UUID> {

    Optional<Request> findTopByStatusInAndCitizen_PhoneOrderByCreatedAtDesc(
            List<String> status, String citizenPhone
    );

    @Query("""
                SELECT new com.rescue.backend.view.dto.coordinator.response.TakeListResponse(
                    r.id,
                    c.phone,
                    c.name,
                    r.status,
                    r.createdAt
                )
                FROM Request r
                JOIN r.citizen c
                WHERE (:status IS NULL OR :status = '' OR r.status = :status)
                ORDER BY r.createdAt DESC
            """)
    Page<TakeListResponse> getRequestCitizen(String status, Pageable pageable);

    @Query("""
            SELECT new com.rescue.backend.view.dto.coordinator.response.SpecificResponse(
                r.id,
                r.type,
                r.description,
                r.address,
                r.latitude,
                r.longitude,
                r.additionalLink,
                r.status,
                r.createdAt,
                r.urgency,
                s.id,
                s.teamName,
                v.id,
                v.type
            )
            FROM Request r
            LEFT JOIN r.rescueTeam s
            LEFT JOIN r.vehicle v
            WHERE r.id = :id
            """)
    SpecificResponse findRequestDetail(UUID id);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Request r
            SET r.status = :status,
                r.urgency = :urgency,
                r.rescueTeam.id = :rescueTeamId,
                r.vehicle.id = :vehicleId
            WHERE r.id = :requestId
            AND r.status <> 'completed'
            """)
    int updateRequest(
            UUID requestId,
            String status,
            String urgency,
            UUID rescueTeamId,
            UUID vehicleId
    );

    Page<Request> findByRescueTeamId(UUID teamId, Pageable pageable);

    Page<Request> findByRescueTeamIdAndStatus(UUID teamId, String status, Pageable pageable);

    Optional<Request> findByRescueTeamIdAndId(UUID rescueTeamId, UUID id);

    Optional<Request> findByRescueTeamId(UUID rescueTeamId);

    @Query("""
                SELECT r.rescueTeam.id, COUNT(r)
                FROM Request r
                WHERE r.rescueTeam.id IN :ids
                GROUP BY r.rescueTeam.id
            """)
    List<Object[]> countRequestsByRescueTeamIds(@Param("ids") List<UUID> ids);

    long countBy();

    long countByStatus(String status);

    @Query("""
            SELECT r.rescueTeam.name, COUNT(r)
            FROM Request r
            WHERE r.status = :status
              AND r.rescueTeam IS NOT NULL
            GROUP BY r.rescueTeam.name
            ORDER BY COUNT(r) DESC
            LIMIT 4
            """)
    List<Object[]> findTop4TeamsByCompletedRequests(@Param("status") String status);

    @Query(value = """
    SELECT 
        TRIM(SUBSTRING_INDEX(r.address, ',', -1)) AS city,
        COUNT(*) AS requestCount
    FROM `Request` r
    WHERE r.address IS NOT NULL
        AND TRIM(r.address) <> ''
        AND r.address LIKE '%,%'
    GROUP BY TRIM(SUBSTRING_INDEX(r.address, ',', -1))
    ORDER BY requestCount DESC
    LIMIT 3
    """, nativeQuery = true)
    List<Object[]> findTop3CitiesByRequestCount();

    Page<Request> findAllByStatus(String status, Pageable pageable);
}