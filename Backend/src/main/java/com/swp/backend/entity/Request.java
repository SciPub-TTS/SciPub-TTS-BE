package com.swp.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Request")
@Getter
@Setter
@NoArgsConstructor
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR) // Đảm bảo dùng VARCHAR thay vì BINARY
    @Column(name = "id", length = 36)
    private UUID id;

    // Citizen gửi request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Citizen citizen;

    @Column(nullable = false, length = 20)
    private String type;

    @Lob
    private String description;

    @Column(length = 200)
    private String address;

    @Column(precision = 18, scale = 10, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 18, scale = 10, nullable = false)
    private BigDecimal longitude;

    // DB trigger sẽ tự cập nhật
    @Column(name = "geo_location", columnDefinition = "POINT",
            insertable = false, updatable = false)
    private Point geoLocation;

    @Column(name = "additional_link", length = 200)
    private String additionalLink;

    @Column(length = 20)
    private String status = "yêu cầu mới";

    @Column(length = 20)
    private String urgency;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Coordinator
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private Staff coordinator;

    // Rescue team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_team_id")
    private Staff rescueTeam;

    // Vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(columnDefinition = "TEXT")
    private String report;

    @OneToMany(mappedBy = "request")
    private List<RequestImage> images;

    @OneToMany(mappedBy = "request")
    private List<Message> messages;
}